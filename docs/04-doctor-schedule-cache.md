# 模块 04：医生排班查询与可读缓存

## 职责

实现患者端医生排班接口 `GET /api/doctors/{doctorId}/schedules`，以 Redis 缓存承接早晨 8 点高并发读流量。缓存命中时不访问数据库；缓存中保存结构清晰的 JSON 文本。Controller 仅校验 `doctorId`、调用 Service 并返回 `Result<DoctorScheduleVO>`；Redis 访问和缓存重建由 Service 负责，具体分层遵循 [backend-conventions.md](backend-conventions.md)。

## 响应契约

Controller 的实际响应为 `Result<DoctorScheduleVO>`；下方 JSON 展示其 `data` 字段。

```json
{
  "doctor": { "id": 1, "name": "钟南", "title": "主任医师" },
  "schedules": [
    { "id": 1, "workDate": "2023-12-01", "timeSlot": "上午", "capacity": 50 }
  ]
}
```

接口只展示该医生与其排班；不携带医生所属科室、患者、挂号单等无关对象，既控制报文大小，也消除循环风险。

## 缓存键和值

| 项目 | 约定 |
| --- | --- |
| Key | `hospital:doctor:schedules:{doctorId}`，例如 `hospital:doctor:schedules:1` |
| Value | 上述 `DoctorScheduleVO` 的 UTF-8 标准 JSON 文本 |
| 序列化 | Redis value serializer 使用 JSON 字符串序列化；禁止 JDK 默认二进制序列化 |
| TTL | 由运维配置，例如 24 小时；TTL 是兜底，不能替代变更时的主动失效 |
| 空值策略 | 医生不存在不写长期空缓存；正常医生的空排班可短 TTL 缓存，具体时长由压测确定 |

缓存值示例（运维应能在客户端直接阅读）：

```json
{"doctor":{"id":1,"name":"钟南","title":"主任医师"},"schedules":[{"id":1,"workDate":"2023-12-01","timeSlot":"上午","capacity":50}]}
```

不要缓存 ORM Entity：它们可能含代理、反向关联或 JDK 序列化字节，均不符合审计和防环要求。缓存值仅为 `DoctorScheduleVO` 的 JSON 数据部分，不能包含请求 DTO 或统一响应包装的瞬态信息。

## 读流程（Cache-Aside）

```text
请求 → 读取 Redis 键
  ├─ 命中：反序列化 DoctorScheduleVO → 200（不查询数据库）
  └─ 未命中：查询医生与排班 → Service 组装 DoctorScheduleVO → 写入 JSON 缓存 → 200
```

首次并发未命中可能造成缓存击穿。至少应使用“按 `doctorId` 粒度的单飞/互斥重建”策略：同一键同一时刻仅一个请求查询数据库，其余请求短暂等待后再读缓存。锁必须有过期时间，且只能在获取成功后由持有者释放，避免异常导致永久锁。

缓存写入失败时，可返回本次数据库查询结果，但必须记录错误并触发监控；不能写入二进制替代数据。

## 性能与观测

- 指标：缓存命中率、Redis 读写耗时、DB 回源次数、单飞等待次数/时长、接口 P95/P99。
- 日志：记录 `doctorId`、缓存命中/未命中、失效原因；不记录病历或身份证号。
- UAT 观察窗口内，对已存在且未变更的医生连续请求 20 次，数据库只允许出现 1 次快照读取。

## 验收用例

1. 清空该医生缓存后请求一次：数据库查询一次，Redis 出现 JSON 字符串值。
2. 再连续请求 20 次：响应一致，数据库查询总数仍为一次。
3. 用 Redis 客户端查看值：可读到“钟南”和 `capacity`，没有二进制乱码。
4. 两个并发首请求：断言数据库快照查询最多一次（需要测试替身或 SQL 计数器）。
