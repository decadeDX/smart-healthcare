# 模块 01：工程基座与依赖治理

## 职责

建立单一依赖版本来源，保证 Web、数据库连接、ORM、缓存客户端、JSON 序列化、测试等第三方组件的版本相互兼容。业务模块只声明“使用什么依赖”，不声明“使用哪个版本”。

## 建议工程边界

```text
hospital-parent/                 聚合工程与统一版本清单
├── hospital-domain/             领域对象、枚举（不依赖 Web/Redis）
├── hospital-infrastructure/     Mapper、Redis 配置
└── hospital-application/        Controller、Service、启动类
```

若项目规模暂不需要多模块，可保留单个 Spring Boot 工程；但仍必须把依赖版本集中在父 `pom.xml` 的 Spring Boot 父版本、`dependencyManagement` 或导入 BOM 中，子模块/业务 `pom.xml` 不得出现 `<version>`。

## 后端分层与目录约束

项目代码必须遵循 [backend-conventions.md](backend-conventions.md)。单体工程或 `hospital-application` 模块中的业务包按 `controller`、`service`、`service/impl`、`mapper`、`entity`、`dto`、`vo`、`config`、`common`、`exception`、`enums`、`util` 划分职责。

- DTO 仅接收请求，VO 仅作为接口响应，Entity 仅承担持久化映射；Controller 返回统一 `Result<VO>`，不得直接暴露 Entity。
- Controller 不直接操作 Mapper；业务规则、Redis 调用和事务控制由 Service 承担；Mapper 不承载业务判断。
- 使用全局异常处理器将 Service 抛出的业务异常转换为统一响应；参数格式使用 Bean Validation 与 `@Valid` 校验。
- 固定状态使用 `enums/` 下的枚举；不因追求“架构完整”增加无业务价值的 BO、DO、PO 或 Assembler 层。

## Maven 设计要求

| 项目 | 规则 |
| --- | --- |
| 父工程 | 唯一维护 JDK、Spring Boot、Spring Cloud（若使用）、MyBatis、Redis 客户端等版本或 BOM |
| 业务模块 | 继承父 POM；依赖不写 `<version>` |
| 插件 | 由父工程 `pluginManagement` 统一配置；子模块仅按需声明插件 |
| 依赖收敛 | 同类基础库只能保留一条受控版本路径，避免手工引入冲突的 JSON、日志、连接池实现 |
| 版本升级 | 仅修改父工程/BOM；升级后执行完整测试和依赖树检查 |

不应为了满足“统一版本”额外手写一张版本常量表，或在每个模块重复版本属性。优先使用框架官方 BOM/父 POM，再以少量企业统一属性补足其未覆盖的组件。

## 启动配置职责

- 数据源：连接 `hospital_sys`；凭据、地址等环境差异项放在外部配置，不写入源码。
- Redis：设置连接地址、超时和应用命名空间；开发、测试、生产环境各自隔离。
- JSON：HTTP 响应与 Redis 缓存采用一致的 JSON 规则；缓存不得使用 JDK 二进制序列化。
- 日志：启动时输出必要的环境和端口信息，但不得输出数据库密码、身份证号、病历全文。

## 验证清单

1. 执行 Maven 依赖树，业务模块不出现自行声明的第三方版本号。
2. 检查日志、JSON、连接池、Web 容器等基础依赖没有多版本冲突。
3. 空库/初始化库下应用均能启动，控制台无 `WARN` 级别的依赖冲突或版本解析告警。
4. Redis 不可用时应有明确的启动或健康检查错误，不能静默降级为二进制本地缓存。
