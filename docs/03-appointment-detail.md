# 模块 03：挂号单全链路详情

## 职责

实现患者端挂号单详情查询：`GET /api/appointments/{appointmentId}`。该接口展示业务所需链路，但以单向 VO 树输出，彻底隔离数据库实体的双向关系。Controller 仅校验 `appointmentId`、调用 Service 并返回 `Result<AppointmentDetailVO>`；具体分层遵循 [backend-conventions.md](backend-conventions.md)。

## 响应契约

Controller 的实际响应为 `Result<AppointmentDetailVO>`；下方 JSON 展示其 `data` 字段。

```json
{
  "id": 1,
  "status": "BOOKED",
  "patient": { "id": 1, "realName": "张三" },
  "schedule": {
    "id": 1,
    "workDate": "2023-12-01",
    "timeSlot": "上午",
    "capacity": 50,
    "doctor": {
      "id": 1,
      "name": "钟南",
      "title": "主任医师",
      "department": {
        "id": 1,
        "name": "呼吸内科",
        "campus": { "id": 1, "name": "本部院区", "address": "市中心中山路1号" },
        "director": { "id": 1, "name": "钟南", "title": "主任医师" }
      }
    }
  },
  "medicalRecord": {
    "id": 1,
    "diagnosis": "上呼吸道感染",
    "prescription": "阿莫西林"
  }
}
```

`medicalRecord` 可为 `null`。任何嵌套对象均不允许反向包含父对象或集合：例如 `patient` 不含 `appointments`，`director` 不含 `department`/`schedules`，`campus` 不含 `departments`。

## VO 设计

响应 VO 可以采用嵌套静态类或独立文件，类名以 `VO` 结尾；保持字段仅覆盖响应契约：

```text
AppointmentDetailVO
├── PatientSummaryVO
├── ScheduleDetailVO
│   └── DoctorDetailVO
│       └── DepartmentDetailVO
│           ├── CampusSummaryVO
│           └── DoctorSummaryVO (director)
└── MedicalRecordDetailVO?
```

不能以 Jackson 的 `@JsonIgnore`、`@JsonManagedReference` 等注解作为主要防环方案，因为实体未来增加字段时容易重新暴露循环；VO 的有向结构才是接口边界。

## 服务流程

1. 校验 `appointmentId` 为正整数。
2. 调用模块 02 的专用读取方法取得平铺的链路数据。
3. 若挂号单不存在，返回 HTTP 404；若关键关联缺失，记录关联 ID 并返回 HTTP 500（或项目统一的数据完整性错误码）。
4. Service 显式组装 `AppointmentDetailVO`，对 `medical_record` 使用左连接结果。
5. Controller 由框架序列化 `Result<AppointmentDetailVO>` 并返回 HTTP 200；Service 抛出的业务异常由全局异常处理器转换为统一错误响应。

## 错误契约

| 情况 | HTTP | 处理 |
| --- | --- | --- |
| `appointmentId` 非法 | 400 | 返回参数错误，不查询数据库 |
| 挂号单不存在 | 404 | 返回资源不存在 |
| 病历不存在 | 200 | `medicalRecord: null` |
| 关联患者/排班/医生/科室/院区缺失 | 500 | 返回数据完整性错误，日志记录缺失关系 |

## 验收用例

- `GET /api/appointments/1` 返回文档所示字段和初始化数据。
- 响应树最大为 `appointment → schedule → doctor → department → campus/director` 的有限深度，序列化不抛 `StackOverflowError`。
- 断言 JSON 中不存在 `appointments`、`schedules`、`departments` 等反向集合字段。
- 不存在的 ID 返回 404；未生成病历的挂号单返回 200 且病历为 `null`。
