# 智慧医疗多院区挂号与号源调度系统：开发文档索引

## 目标与边界

本次建设的是“核心查询与调度基座”，服务于多院区挂号场景。数据库的 7 张既有表为唯一持久化模型，**不得修改表结构**。交付重点是：

1. 全工程依赖版本由统一基座管理；
2. 挂号详情返回有限、无环的数据树；
3. 医生排班查询具备缓存防洪能力，缓存内容为可读 JSON；
4. 排班停诊或容量变更后，患者端不可读取旧缓存。

## 模块清单与实现顺序

| 顺序 | 文档 | 模块 | 可独立验证的结果 |
| --- | --- | --- | --- |
| 前置 | [backend-conventions.md](backend-conventions.md) | 后端通用开发规范 | 统一分层、DTO/VO、`Result<T>`、异常、校验、事务和命名约定 |
| 1 | [01-project-baseline.md](01-project-baseline.md) | 工程基座与依赖治理 | 业务模块无版本号，应用无依赖冲突告警启动 |
| 2 | [02-data-access.md](02-data-access.md) | 数据访问与领域映射 | 能按既有表读写数据，查询不会加载无关关系 |
| 3 | [03-appointment-detail.md](03-appointment-detail.md) | 挂号单全链路详情 | 返回固定深度 DTO，序列化无循环 |
| 4 | [04-doctor-schedule-cache.md](04-doctor-schedule-cache.md) | 医生排班查询与缓存 | 连续请求仅首次访问数据库，缓存值可读 |
| 5 | [05-schedule-command-consistency.md](05-schedule-command-consistency.md) | 排班变更、停诊与缓存一致性 | 成功更新后立即清除对应医生缓存 |
| 6 | [06-api-and-uat.md](06-api-and-uat.md) | API 契约、测试及验收 | 所有 UAT 关卡可自动/手工复核 |
| 7 | [08-api-documentation.md](08-api-documentation.md) | 对外接口文档 | 前后端可按请求、响应、错误及缓存语义联调 |
| 8 | [09-http-status-code-mapping.md](09-http-status-code-mapping.md) | HTTP 状态码对照表 | 明确各接口成功和失败状态的 HTTP 语义 |
| 9 | [openapi.yaml](openapi.yaml) | OpenAPI 3.0.3 规范 | 可直接导入 Apifox Spec 项目，并作为接口定义源文件 |
| 附录 | [07-overall-uml-class-diagram.md](07-overall-uml-class-diagram.md) | 总体 UML 类图 | 明确实体关系、DTO 防环边界和服务协作 |

## 关键业务对象

```text
院区 Campus 1 ── * 科室 Department 1 ── * 医生 Doctor 1 ── * 排班 Schedule
                                 │                         
                                 └── 主任 Doctor             

患者 Patient 1 ── * 挂号单 Appointment * ── 1 排班 Schedule
                         │
                         └── 0..1 电子病历 MedicalRecord
```

上述关系仅用于服务内部查询。接口禁止直接序列化实体；必须按场景组装 DTO，避免双向对象导航形成循环。

## 全局约定

- 后端实现必须遵循 [backend-conventions.md](backend-conventions.md)：Controller 仅负责 DTO 参数接收与校验、调用 Service、返回 `Result<VO>`；不得直接调用 Mapper 或编写复杂业务逻辑。
- DTO 仅用于请求参数，按场景命名为 `XxxCreateDTO`、`XxxUpdateDTO` 或 `XxxQueryDTO`；接口响应使用 `XxxVO`，Entity 仅用于持久化，禁止直接作为请求或响应对象。
- 业务规则、Redis 访问和事务控制位于 Service；Mapper 只负责数据访问；业务异常由全局异常处理器统一转换为响应。固定状态使用 `enums/` 下的枚举，参数格式由 DTO 上的 Bean Validation 与 Controller 的 `@Valid` 校验。
- 新增代码应使用 `controller`、`service`、`service/impl`、`mapper`、`entity`、`dto`、`vo`、`config`、`common`、`exception`、`enums`、`util` 的目录边界；不得为形式完整额外引入 BO、DO、PO、Assembler 等层。
- 时间：`schedule.work_date` 为出诊日期，接口使用 `yyyy-MM-dd`；`time_slot` 沿用数据库中的中文时段值。
- 号源：`capacity` 表示当前剩余可预约数；值为 `0` 表示该时段无可预约号源。本期没有独立的排班状态字段，停诊以容量更新为 `0` 表达。
- 挂号单状态：使用 `BOOKED`、`CANCELLED`、`VISITED`。数据库虽未设枚举约束，应用层必须校验该集合。
- 缓存：以“医生维度的全部排班”作为缓存单元；缓存键、TTL、序列化和失效策略由模块 04、05 统一规定。
- 一致性：数据库写入成功后才允许删除缓存；写库失败时不得碰缓存。缓存删除失败不可对外报告为更新成功。

## 需在开发启动前确认的事项

1. 具体技术栈（Spring Boot / MyBatis / Redis 的版本与企业 BOM 来源）尚未指定；基座模块给出约束，不虚构版本号。
2. “修改排班状态”与现有表不完全一致；本文档按 UAT 明确要求，将停诊定义为将 `capacity` 更新为 `0`。若后续需要“停诊但保留可售余量”，必须由 DBA 增加状态列后再扩展。
3. 当前需求只涵盖查询和后台调整，不包含下单扣减、并发防超卖、患者鉴权或病历权限控制；这些不能在本期被默认实现。
