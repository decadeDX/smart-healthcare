# 模块 06：API 契约、测试与 UAT

## API 总表

所有 Controller 按 [backend-conventions.md](backend-conventions.md) 返回 `Result<VO>`：成功响应至少包含 `code`、`message`、`data`，错误响应额外包含 `traceId`；请求使用 DTO 接收，响应使用 VO，禁止直接暴露 Entity。具体业务异常由全局异常处理器转换，Controller 不直接调用 Mapper 或处理 Redis。

| 场景 | 方法与路径 | 权限 | 成功结果 |
| --- | --- | --- | --- |
| 查看挂号单全链路详情 | `GET /api/appointments/{appointmentId}` | 患者本人/后台（鉴权待接入） | 有限详情树 |
| 查询医生排班号源 | `GET /api/doctors/{doctorId}/schedules` | 患者端 | 医生摘要 + 排班列表，缓存加速 |
| 调整排班余量/紧急停诊 | `PATCH /api/admin/schedules/{scheduleId}/capacity` | 院办后台 | 更新余量并失效缓存 |

统一错误响应应至少包含：`code`、`message`、`data`（失败时为 `null`）、`traceId`。不要在客户端错误消息中泄露 SQL、缓存地址、身份证号或病历内容。

## 测试分层

### 单元测试

- VO 映射：验证不产生任何反向嵌套字段，病历可空；Controller 返回 `Result<VO>`。
- DTO 校验：验证 `@Valid` 能拒绝负容量、非正 ID、非法状态等格式或值域错误；Service 继续校验资源存在性、关联完整性与缓存失效等业务规则。
- 缓存服务：命中时 Mapper 不被调用；未命中时写入 JSON；Redis 删除失败时更新 Service 失败。
- 排班命令：更新后删除的键必须是对应 `doctorId` 的键。

### 集成测试

- 使用真实 MySQL/Redis 测试容器或隔离测试实例验证 SQL、JSON 编码和事务边界。
- 统计 `findDoctorScheduleSnapshot` 调用数：清除键后连续 20 次接口调用仅一次。
- 先预热缓存再将容量改为 0，下一次查询必须为 0。
- 并发测试：同一医生的并发冷启动不重复回源；读重建与写命令并发时不得回填旧快照。

### 手工 UAT 脚本

| 关卡 | 操作 | 通过标准 |
| --- | --- | --- |
| 1：架构纯净 | 构建并启动服务，检查依赖树与日志 | 无依赖冲突/版本解析 WARN；业务模块无版本号 |
| 2：详情防环 | 调用 `GET /api/appointments/1` | 正确显示张三、钟南、呼吸内科、本部院区、病历；无 StackOverflowError、无无限层级 |
| 3：缓存防洪 | 清除医生 1 键后连续调用排班接口 20 次 | DB 仅一次查询；Redis 值是可读 JSON，含医生名和余量 |
| 4：停诊一致 | 预热缓存后将 schedule 1 容量改为 0，立刻查询 | 不返回容量 50 的旧数据，返回容量 0 |

## 最小验收数据

使用 PRD 提供的初始化数据：院区“本部院区”、科室“呼吸内科”、医生“钟南”、患者“张三”、2023-12-01 上午排班容量 50、挂号单 1 与病历 1。测试之间需重置 `schedule.id=1` 的容量并删除对应 Redis 键，防止缓存污染。

## 完成定义（Definition of Done）

1. 所有 API 有 DTO 请求校验、`Result<VO>` 响应和错误码测试；Controller 不返回 Entity、不直接调用 Mapper。
2. 缓存 JSON 在 Redis 客户端可读，且无对象循环。
3. 排班更新与缓存失效的失败路径有自动化测试和可观测日志。
4. UAT 四个关卡全部通过，并保留构建日志、接口响应、Redis 截图/导出和 DB 查询计数作为验收证据。
5. 未实现的认证、预约扣减、防超卖、正式病历权限必须在发布说明中列为范围外，不能被误标为已交付。
6. Service 命名为 `XxxService` / `XxxServiceImpl`，固定状态采用枚举；不得为了形式完整增加 BO、DO、PO、Assembler 等无业务价值的层。
