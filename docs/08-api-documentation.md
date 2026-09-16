# 智慧医疗多院区挂号与号源调度系统：接口文档

## 1. 文档范围

本文档依据模块 03～06 的已确定契约整理，面向患者端、院办后台及联调测试使用。本期仅包含挂号详情查询、医生排班查询与排班余量调整三个接口。后端实现必须遵循 [backend-conventions.md](backend-conventions.md)：请求使用 DTO，响应使用 VO，Controller 只负责参数校验和调用 Service，Controller API 统一返回 `Result<VO>`。

以下能力不在本期接口范围内：预约下单与扣减、防超卖、患者身份认证、病历的正式权限控制，以及“停诊但保留可售余量”。

## 2. 通用约定

| 项目 | 约定 |
| --- | --- |
| 基础路径 | `/api` |
| 请求与响应编码 | UTF-8 |
| JSON 媒体类型 | `application/json` |
| 日期格式 | `yyyy-MM-dd`，例如 `2023-12-01` |
| 标识符 | 路径中的 ID 必须为正整数 |
| 号源余量 | `capacity` 为非负整数，表示当前剩余可预约数；`0` 表示无可预约号源 |
| 挂号单状态 | `BOOKED`、`CANCELLED`、`VISITED` |

### 2.1 鉴权边界

当前文档未指定认证协议、令牌格式或角色编码，接入认证后按下表执行授权：

| 接口 | 访问边界 |
| --- | --- |
| 查询挂号详情 | 挂号患者本人或院办后台 |
| 查询医生排班 | 患者端公开查询能力 |
| 调整排班余量 | 仅院办后台；不得以匿名患者身份访问 |

未完成鉴权接入前，后端不得将后台调整接口暴露给匿名调用方。

### 2.2 统一响应结构

所有 Controller API 均返回统一 `Result<T>` 结构。成功时 `data` 为对应 VO；失败时 `data` 为 `null` 并携带 `traceId`。`code` 的具体枚举值由项目统一错误码注册表确定；本文档不虚构未定义的编码值。

成功响应示例：

```json
{
  "code": "<项目统一成功码>",
  "message": "success",
  "data": { "<对应接口 VO 字段>": "..." }
}
```

错误响应示例：

```json
{
  "code": "<项目统一错误码>",
  "message": "面向调用方的错误说明",
  "data": null,
  "traceId": "请求链路追踪标识"
}
```

下文各接口的“成功响应”JSON 仅展示 `data` 内的 VO 字段，实际 HTTP 响应必须按上述 `Result<T>` 包装。错误消息不得泄露 SQL、Redis 地址、身份证号、病历内容或其他内部实现细节。

## 3. 接口概览

| 编号 | 名称 | 方法与路径 | 权限 | 成功响应 |
| --- | --- | --- | --- | --- |
| API-01 | 查询挂号单全链路详情 | `GET /api/appointments/{appointmentId}` | 患者本人/院办后台 | `200`，`Result<AppointmentDetailVO>` |
| API-02 | 查询医生排班号源 | `GET /api/doctors/{doctorId}/schedules` | 患者端 | `200`，`Result<DoctorScheduleVO>` |
| API-03 | 调整排班余量/紧急停诊 | `PATCH /api/admin/schedules/{scheduleId}/capacity` | 院办后台 | `200`，`Result<ScheduleCapacityUpdateVO>` |

## 4. API-01：查询挂号单全链路详情

### 请求

```http
GET /api/appointments/{appointmentId}
Accept: application/json
```

| 参数位置 | 名称 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| path | `appointmentId` | integer | 是 | 挂号单 ID，必须为正整数 |

请求示例：

```http
GET /api/appointments/1
```

### 成功响应

状态码：`200 OK`。以下为 `Result<AppointmentDetailVO>.data`：

```json
{
  "id": 1,
  "status": "BOOKED",
  "patient": {
    "id": 1,
    "realName": "张三"
  },
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
        "campus": {
          "id": 1,
          "name": "本部院区",
          "address": "市中心中山路1号"
        },
        "director": {
          "id": 1,
          "name": "钟南",
          "title": "主任医师"
        }
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

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `id` | integer | 否 | 挂号单 ID |
| `status` | string | 否 | 挂号状态：`BOOKED`、`CANCELLED` 或 `VISITED` |
| `patient.id` | integer | 否 | 患者 ID |
| `patient.realName` | string | 否 | 患者真实姓名；不返回身份证号 |
| `schedule.id` | integer | 否 | 排班 ID |
| `schedule.workDate` | string(date) | 否 | 出诊日期 |
| `schedule.timeSlot` | string | 否 | 出诊时段，例如“上午” |
| `schedule.capacity` | integer | 否 | 当前剩余号源数，非负整数 |
| `schedule.doctor` | object | 否 | 出诊医生及其科室信息 |
| `schedule.doctor.department.campus` | object | 否 | 所属院区摘要 |
| `schedule.doctor.department.director` | object | 否 | 科室主任摘要 |
| `medicalRecord` | object | 是 | 尚未产生病历时为 `null` |
| `medicalRecord.id` | integer | 否 | 病历 ID |
| `medicalRecord.diagnosis` | string | 是 | 诊断结果 |
| `medicalRecord.prescription` | string | 是 | 处方信息 |

响应固定为从挂号单向下展开的有限树。不得出现 `appointments`、`schedules`、`departments` 等反向集合字段，也不得在嵌套对象中回填父对象。

未产生病历时：

```json
{ "medicalRecord": null }
```

该字段为 `null` 不影响其余成功响应字段；其他嵌套对象仍必须完整符合上表结构。

### 异常响应

| HTTP 状态 | 触发条件 |
| --- | --- |
| `400 Bad Request` | `appointmentId` 非正整数或格式非法；不得查询数据库 |
| `404 Not Found` | 挂号单不存在 |
| `500 Internal Server Error` | 患者、排班、医生、科室、院区或科室主任等关键关联数据缺失；服务端记录关联 ID 和告警 |

病历不存在不是异常，仍返回 `200`，并令 `medicalRecord` 为 `null`。

## 5. API-02：查询医生排班号源

### 请求

```http
GET /api/doctors/{doctorId}/schedules
Accept: application/json
```

| 参数位置 | 名称 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| path | `doctorId` | integer | 是 | 医生 ID，必须为正整数 |

请求示例：

```http
GET /api/doctors/1/schedules
```

### 成功响应

状态码：`200 OK`。以下为 `Result<DoctorScheduleVO>.data`：

```json
{
  "doctor": {
    "id": 1,
    "name": "钟南",
    "title": "主任医师"
  },
  "schedules": [
    {
      "id": 1,
      "workDate": "2023-12-01",
      "timeSlot": "上午",
      "capacity": 50
    }
  ]
}
```

| 字段 | 类型 | 可空 | 说明 |
| --- | --- | --- | --- |
| `doctor.id` | integer | 否 | 医生 ID |
| `doctor.name` | string | 否 | 医生姓名 |
| `doctor.title` | string | 否 | 医生职称 |
| `schedules` | array | 否 | 该医生的排班列表；医生存在但无排班时返回 `[]` |
| `schedules[].id` | integer | 否 | 排班 ID |
| `schedules[].workDate` | string(date) | 否 | 出诊日期 |
| `schedules[].timeSlot` | string | 否 | 出诊时段 |
| `schedules[].capacity` | integer | 否 | 当前剩余号源数 |

排班列表按 `workDate ASC, timeSlot ASC, id ASC` 稳定排序。该接口不返回医生所属科室、患者或挂号单。

### 缓存语义

本接口采用 Cache-Aside：缓存命中时不得访问数据库；未命中时查询数据库、组装响应后写入缓存。缓存单元和审计要求如下：

| 项目 | 约定 |
| --- | --- |
| Redis Key | `hospital:doctor:schedules:{doctorId}`，例如 `hospital:doctor:schedules:1` |
| Redis Value | 当前响应对象的 UTF-8 标准 JSON 文本 |
| TTL | 由运维配置；仅为兜底，排班变更必须主动失效 |
| 医生不存在 | 返回 `404`，不写长期空缓存 |
| 医生存在但无排班 | 返回空数组；可使用短 TTL 缓存 |

同一医生的并发冷启动请求必须由单飞/互斥重建控制，避免重复回源。缓存写入失败时可返回本次数据库读取结果，但服务端必须记录错误并触发监控。

### 异常响应

| HTTP 状态 | 触发条件 |
| --- | --- |
| `400 Bad Request` | `doctorId` 非正整数或格式非法 |
| `404 Not Found` | 医生不存在 |
| `500 Internal Server Error` | 数据库读取、响应组装等服务端异常 |

## 6. API-03：调整排班余量/紧急停诊

在现有表结构中没有独立排班状态字段，因此紧急停诊的标准操作是将目标排班的 `capacity` 更新为 `0`。

### 请求

```http
PATCH /api/admin/schedules/{scheduleId}/capacity
Content-Type: application/json
Accept: application/json

{ "capacity": 0 }
```

| 参数位置 | 名称 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- | --- |
| path | `scheduleId` | integer | 是 | 排班 ID，必须为正整数 |
| body | `capacity` | integer | 是 | 非负整数；`0` 表示紧急停诊/无可预约号源 |

### 成功响应

状态码：`200 OK`。以下为 `Result<ScheduleCapacityUpdateVO>.data`：

```json
{
  "id": 1,
  "doctorId": 1,
  "workDate": "2023-12-01",
  "timeSlot": "上午",
  "capacity": 0
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | integer | 已更新的排班 ID |
| `doctorId` | integer | 该排班所属医生 ID |
| `workDate` | string(date) | 出诊日期 |
| `timeSlot` | string | 出诊时段 |
| `capacity` | integer | 更新后的剩余号源数 |

### 一致性要求

成功响应的前提是以下流程全部完成：

```text
校验请求 → 查询排班并取得 doctorId → 事务内更新数据库
→ 数据库事务提交成功 → 删除 hospital:doctor:schedules:{doctorId} → 返回 200
```

- 必须先更新数据库、后失效缓存；写库失败时不得删除缓存。
- 仅删除该排班所属医生的缓存键，禁止清空所有 Redis 数据。
- 缓存删除需有限次数重试；最终删除失败时不得返回 `200`，并记录最高级别告警。
- 读缓存重建与写命令应使用同一医生维度的短租约协调，或采用缓存版本校验，避免旧快照在删除后回填。

### 异常响应

| HTTP 状态 | 触发条件 | 副作用 |
| --- | --- | --- |
| `400 Bad Request` | `scheduleId` 非法，或 `capacity` 缺失、非整数、负数 | 不写库、不删缓存 |
| `404 Not Found` | 排班不存在 | 不写库、不删缓存 |
| `500 Internal Server Error` | 数据库更新失败 | 缓存保持不变 |
| `500 Internal Server Error` 或 `503 Service Unavailable` | 缓存失效或读写协调最终失败 | 不得报告更新成功；记录告警，院办可重试 |

## 7. 联调验收要点

| 场景 | 操作 | 预期结果 |
| --- | --- | --- |
| 详情防环 | 调用 `GET /api/appointments/1` | 返回张三、钟南、呼吸内科、本部院区及病历；无无限嵌套或 `StackOverflowError` |
| 缓存命中 | 删除 `hospital:doctor:schedules:1` 后，连续调用排班接口 20 次 | 数据库快照读取仅 1 次；Redis 中为可读 JSON |
| 停诊一致性 | 预热医生 1 缓存，调用容量更新传入 `0`，随后再次查询排班 | 更新接口后旧缓存键不存在；后续读取返回 `capacity: 0` |

## 8. 待确认事项

1. 认证协议、令牌传递方式、患者本人判定规则与院办角色编码。
2. 统一错误码注册表中 `code` 的具体值。
3. 医生存在但无排班时的短 TTL 具体时长，以及常规排班缓存 TTL。
4. 病历字段在正式患者端上线前的合规授权范围与脱敏策略。
