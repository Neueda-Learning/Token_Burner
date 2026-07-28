# Token_Burner 项目总结（用于生成 PPT）

## 1. 项目概述

**Token_Burner** 是一个基于 Spring Boot 的支付处理系统（Payment Processing System），支持以下核心能力：

- 支付创建（Create Payment）
- 支付状态流转与生命周期管理（CREATED → VALIDATED → SENT → COMPLETED / FAILED）
- 支付状态历史记录（Payment Status History）
- 统一异常处理（Global Exception Handling）
- 简单的 Web 前端交互界面
- Mock Service 支持前后端并行开发（无需依赖数据库即可联调）

项目采用经典分层架构：

```
Frontend
   ↓
Controller 层
   ↓
Service 层
   ↓
Repository 层
   ↓
Database
```

设计原则：职责分离（Separation of Concerns）、可维护性、可测试性、可复用性、一致性。

---

## 2. 团队分工（4 人）

| 成员 | 负责模块 | 主要职责 |
|------|----------|----------|
| **Lyra** | Frontend | 前端页面开发、API 联调（对接后端接口）、Demo 演示准备 |
| **Kylian** | Entity / Repository / Database | 实体建模（User、Payment、PaymentStatusHistory）、Spring Data JPA 仓储层、数据库表结构设计（SQL） |
| **Richard** | Service / Business Logic | 核心业务逻辑实现、支付生命周期状态机、事务管理（Transaction Handling）、密码校验、余额校验 |
| **Leon** | Controller / DTO / Exception Handling / Testing | HTTP 接口层（Controller）、请求响应 DTO 设计、全局异常处理、单元测试与集成测试 |

---

## 3. 各角色详细工作内容

### 3.1 Lyra — 前端与联调
- 提供支付相关的表单页面，收集用户输入（转账账户、金额、币种、支付密码等）
- 调用后端 REST API 发起请求
- 展示支付结果、状态及错误信息
- 负责最终 Demo 演示的准备工作

### 3.2 Kylian — 数据层
- 设计并维护实体类：`User`、`Payment`、`PaymentStatusHistory`
- 编写 Repository 接口（`UserRepository`、`PaymentRepository`、`PaymentStatusHistoryRepository`），基于 Spring Data JPA
- 设计数据库表结构（`docker/sql-code/sql_v1.sql`），并保证与实体字段（如 `sourceAccountId`/`destinationAccountId`）保持一致
- 通过 Docker Compose 提供本地数据库环境

### 3.3 Richard — 业务逻辑层
- 实现 `PaymentServiceImpl`，是系统的核心业务逻辑所在层
- 关键业务规则：
  - 校验来源/目标账户是否存在、账户状态是否为 `ACTIVE`
  - 校验支付金额是否合法（大于 0）
  - 校验支付密码是否匹配
  - 校验账户余额是否充足
  - 创建支付记录及初始状态历史
  - 管理支付状态流转规则（冻结的状态机）：
    - `CREATED → VALIDATED`
    - `CREATED → FAILED`
    - `VALIDATED → SENT`
    - `VALIDATED → FAILED`
    - `SENT → COMPLETED`
    - `SENT → FAILED`
    - `COMPLETED`、`FAILED` 为终态，不可再变更
  - 使用 `@Transactional` 保证数据一致性

### 3.4 Leon — 接口层与质量保障
- 实现 `PaymentController`，仅负责接收 HTTP 请求、参数校验、调用 Service、返回响应，不包含任何业务逻辑
- 设计请求/响应 DTO（`CreatePaymentRequest`、`UpdatePaymentStatusRequest`、`PaymentResponse`、`PaymentHistoryResponse`）
- 实现 `GlobalExceptionHandler`，统一处理各类自定义异常（如 `UserNotFoundException`、`InsufficientBalanceException`、`InvalidPaymentPasswordException` 等），返回统一格式的错误响应（`errorCode` + `message`）
- 编写 Controller 层、Repository 层测试用例，保障功能正确性

---

## 4. 团队协作机制

- **契约先行（Contract First）**：`docs/service-contract.md` 冻结了 `PaymentService` 接口的方法签名、DTO 字段与异常类型，Controller 与 Service 开发可并行进行
- **Mock Service 解耦**：通过 `MockPaymentServiceImpl`（`@Profile("mock")`）与真实实现 `PaymentServiceImpl`（`@Profile("!mock")`）互斥切换，Leon 无需等待数据库或 Richard 的真实实现即可独立开发调试 Controller
- **文档驱动**：`docs/` 目录下维护了架构设计（architecture.md）、API 设计（api-design.md）、数据库设计（database-design.md）、DTO/API 说明（dto-api.md）、仓储层要求（repository-requirements.md）等文档，确保团队对齐

---

## 5. 技术栈

- Java + Spring Boot
- Spring Data JPA（数据持久化）
- Spring MVC（Controller 层）
- Docker / Docker Compose（本地数据库环境）
- Maven（构建工具）
- JUnit（单元测试与集成测试）

---

## 6. 核心业务流程（以创建支付为例）

1. 用户在前端填写支付信息并提交
2. Controller 接收请求，校验参数格式
3. Controller 调用 Service 层方法
4. Service 层执行业务规则：账户校验 → 状态校验 → 金额校验 → 密码校验 → 余额校验
5. Service 层创建 `Payment` 记录，状态初始化为 `CREATED`
6. Service 层同步生成初始状态历史记录（`PaymentStatusHistory`）
7. Repository 层通过 JPA 将数据持久化到数据库
8. Service 层封装结果返回给 Controller
9. Controller 返回 HTTP 响应
10. 前端展示支付结果

---

## 7. 总结

Token_Burner 项目通过清晰的分层架构和明确的团队分工（前端 / 数据层 / 业务逻辑层 / 接口与测试层），实现了一个可维护、可测试、职责边界清晰的支付处理系统。契约先行与 Mock 服务机制保证了 4 位成员可以并行高效协作。
