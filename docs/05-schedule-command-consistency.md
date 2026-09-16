# 模块 05：排班变更、停诊与缓存一致性

## 职责

实现院办后台对排班余量的更新。UAT 所称“紧急停诊”在当前不允许改表的约束下，固定表示将 `schedule.capacity` 更新为 `0`。命令成功后必须立即使该医生的排班缓存失效。Controller 使用 `ScheduleCapacityUpdateDTO` 接收并通过 `@Valid` 校验请求，调用 Service 后返回 `Result<ScheduleCapacityUpdateVO>`；事务、Redis 和业务异常处理遵循 [backend-conventions.md](backend-conventions.md)。

## 接口契约

`PATCH /api/admin/schedules/{scheduleId}/capacity`

请求：

```json
{ "capacity": 0 }
```

`ScheduleCapacityUpdateDTO.capacity` 必须使用 Bean Validation 声明非空、整数及非负约束；排班是否存在、缓存协调和失效结果属于 Service 的业务校验，不放在 Controller 或 Mapper。

成功响应的 `Result<ScheduleCapacityUpdateVO>.data` 建议返回更新后的最小快照：

```json
{ "id": 1, "doctorId": 1, "workDate": "2023-12-01", "timeSlot": "上午", "capacity": 0 }
```

接口须受后台权限保护；具体认证/授权机制不在本期范围，但不得将此接口暴露为匿名患者接口。

## 一致性流程

```text
Controller 校验 DTO
  → Service 查询排班（取得 doctorId，确认存在）
  → Service 事务中更新 capacity
  → 提交成功后删除 hospital:doctor:schedules:{doctorId}
  → 返回成功
```

### 强制规则

1. **先库后缓存**：事务尚未提交或更新失败时不得删除缓存，否则会出现无意义回源。
2. **精准失效**：只删除被更新排班对应的医生键，不能清空全部 Redis 数据，也不能误删其他医生。
3. **删除必须成功**：缓存删除失败时，接口不可对外返回成功。应记录最高级别告警，并执行有限次数同步重试；最终失败返回可识别的 5xx 错误，以便院办重试。
4. **不直接回写旧快照**：本期采用删除后懒加载，下一次读会从已提交数据库重建完整快照，避免并发下部分字段覆盖。

在单数据库 + 单 Redis 的前提下，无法形成真正跨资源原子事务。上述“提交后删除、删除失败即失败”的策略符合患者端不应读旧号源的业务优先级。事务必须定义在 Service 方法，不能放在 Controller。若系统需要严格保证命令响应后绝无旧读，应在后续架构中增加事务消息/Outbox、版本号或读写屏障；这属于本期之外的架构演进，不能伪称已由普通缓存注解解决。

## 并发场景处理

- 缓存重建与更新并发：更新成功后删除缓存；若一个旧数据回源请求在删除后才写回缓存，可能产生陈旧回填。缓存写入前应二次确认变更版本，或使用 `doctorId` 缓存版本号/分布式锁协调读写。
- 最简可行方案：排班更新与该 `doctorId` 的缓存重建共用同一把短租约分布式锁；更新持锁完成“写库→提交→删缓存”，读路径持锁重建快照。
- 锁超时或 Redis 不可用：更新不得承诺成功；提示后台稍后重试，并保留审计日志。

## 参数与错误处理

| 情况 | HTTP | 结果 |
| --- | --- | --- |
| `scheduleId` 非法或容量非整数/负数 | 400 | 拒绝请求 |
| 排班不存在 | 404 | 不写库、不删缓存 |
| 数据库更新失败 | 500 | 缓存不变 |
| 缓存删除/协调失败 | 503/500 | 不返回成功，触发告警 |
| 成功 | 200 | 下次读必从数据库重建新快照 |

## 验收用例

1. 先调用医生排班查询生成缓存，确认缓存中 `capacity=50`。
2. 调用容量更新接口传入 `0`，确认数据库值为 0，且 `hospital:doctor:schedules:1` 不存在。
3. 立即再查医生排班，返回 `capacity=0`；此请求发生一次数据库回源并写入新 JSON。
4. 人为模拟 Redis 删除失败：接口不得返回 200，且告警日志包含 `scheduleId`、`doctorId` 与失败原因。
