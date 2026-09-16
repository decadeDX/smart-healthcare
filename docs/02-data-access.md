# 模块 02：数据访问与领域映射

## 职责

在不修改 `campus`、`department`、`doctor`、`schedule`、`patient`、`appointment`、`medical_record` 七张表的前提下，提供场景化读取和最小化更新能力。持久化对象（Entity）仅用于数据库交互，不能直接作为接口响应对象。请求参数使用 DTO，Service 将查询结果组装为 VO，并由 Controller 以 `Result<VO>` 返回；具体分层、命名与异常处理遵循 [backend-conventions.md](backend-conventions.md)。

## 表与关系映射

| 表 | 主键 | 关联字段 | 本期用途 |
| --- | --- | --- | --- |
| `campus` | `id` | — | 返回院区名称、地址 |
| `department` | `id` | `campus_id`、`director_id` | 返回科室与主任摘要 |
| `doctor` | `id` | `dept_id` | 返回医生信息，按医生查排班 |
| `schedule` | `id` | `doctor_id` | 返回排班；更新余量 |
| `patient` | `id` | — | 返回挂号患者摘要 |
| `appointment` | `id` | `patient_id`、`schedule_id` | 挂号详情根对象 |
| `medical_record` | `id` | `appointment_id` | 返回可选病历摘要 |

物理表未声明外键，服务层必须显式处理关联缺失：患者、排班、医生、科室、院区和科室主任均为挂号详情的关键关联。任一关联缺失时详情查询应返回“数据不完整”的服务端错误并记录告警，而非构造误导性的空对象。

## Mapper 边界

### 挂号详情读取

在 MyBatis Mapper 中提供专用查询，例如 `findAppointmentDetailSource(appointmentId)`：一次连接查询或受控的多次批量查询均可，但必须只取组装详情 VO 所需字段。Mapper 只负责取数，不组装 VO 或承载业务判断。

查询链路固定为：

```text
appointment → patient
appointment → schedule → doctor → department → campus
                                     └→ department.director
appointment → medical_record（左连接，允许为空）
```

禁止通过实体的 `List<...>` 属性进行懒加载递归；特别禁止在此查询中装载 `patient.appointments`、`doctor.schedules` 等集合。

### 医生排班读取

提供 `findDoctorScheduleSnapshot(doctorId)`，返回医生基本信息和该医生排班列表。需要明确排序，建议 `work_date ASC, time_slot ASC, id ASC`；同日时段的稳定排序不能依赖数据库未定义的默认返回顺序。Service 负责将结果组装为排班查询 VO。

医生不存在时返回空结果（由应用层转为 404）；医生存在但无排班时返回医生信息与空列表。

### 排班余量更新

提供 `updateCapacity(scheduleId, capacity)`。更新前应确认该排班存在并获得其 `doctor_id`，以生成精准失效的缓存键。`capacity` 必须为非负整数；不存在时返回 0 行更新，由应用层转为 404。

## 查询性能与索引建议

不得修改现有表结构，但应向 DBA 提交以下索引建议，由 DBA 审核后执行：

- `schedule(doctor_id, work_date, time_slot, id)`：支持医生排班快照与有序扫描；
- `appointment(id, patient_id, schedule_id)`：主键查询已覆盖 `id`，其余列可供覆盖索引评估；
- `medical_record(appointment_id)`：支持按挂号单读取病历。

是否创建索引属于 DBA 变更流程，不由应用迁移脚本擅自执行。

## 数据校验

- `appointment.status` 只能是 `BOOKED`、`CANCELLED`、`VISITED`。
- `schedule.capacity >= 0`；更新操作拒绝负值。
- `patient.id_card`、`medical_record` 属于敏感数据：本期详情 API 如需输出，必须先取得安全/合规确认；当前默认详情 VO 不输出身份证号，病历仅在要求的详情中按原文返回。

## 验证清单

1. 用初始化数据可正确读取 appointment 1 的完整链路。
2. Mapper 日志确认没有触发患者全部挂号单或医生全部关系的意外查询。
3. 医生无排班、病历为空、关联行缺失的行为符合上述约定。
4. 更新容量时，负值和不存在的 `scheduleId` 均得到明确失败结果。
