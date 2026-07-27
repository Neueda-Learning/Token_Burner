# Payment Processing System Architecture

## Overview
This document describes the layered architecture for the Payment Processing System. The goal of this architecture is to keep responsibilities clearly separated so that the system is easier to understand, maintain, test, and extend.

The application follows this structure:

```text
Frontend
    ↓
Controller Layer
    ↓
Service Layer
    ↓
Repository Layer
    ↓
Database
```

This design is well suited for a training project because it gives the team a clear view of how requests move through the system and where each responsibility belongs.

## Layer Responsibilities

### Frontend
The frontend is the user-facing part of the system.

Responsibilities:
- Provide forms and pages for user interaction
- Collect payment-related input from users
- Send HTTP requests to backend endpoints
- Display payment results, statuses, and error messages

The frontend should focus on presentation and interaction, not payment-processing rules.

### Controller Layer
The controller layer is the entry point of the backend.

Responsibilities:
- Handle HTTP requests
- Validate request format
- Return HTTP responses
- Map incoming requests to backend operations

Important rule:
- The controller layer **should not contain business logic**

This layer is responsible for communication concerns such as request parsing, response formatting, and status codes.

### Service Layer
The service layer contains the core business behavior of the system.

Responsibilities:
- Contains business rules
- Handles payment lifecycle logic
- Validates status transitions
- Coordinates database operations

This is the layer where payment behavior is controlled. For example, it is responsible for ensuring that a payment moves through valid states such as:

`CREATED → VALIDATED → SENT → COMPLETED`

It also ensures that invalid transitions are rejected and that a payment may move to `FAILED` when necessary.

### Repository Layer
The repository layer manages persistence access between the service layer and the database.

Responsibilities:
- Handles database access
- Uses Spring Data JPA
- Provides methods for saving, retrieving, and querying data
- Keeps data-access logic separate from business logic

This layer should focus only on persistence operations and should not make business decisions.

### Database
The database is the persistent storage layer of the system.

Contains:
- Users
- Payments
- Payment status history

The database is responsible for storing core records reliably so the system can retrieve payment details, track payment progress, and maintain historical status information.

## Data Flow Example for Creating a Payment
The following example shows how a payment creation request moves through the system.

### Step-by-Step Flow
1. A user enters payment details in the frontend.
2. The frontend sends an HTTP request to the backend.
3. The controller layer receives the request.
4. The controller validates the request format and required fields.
5. The controller passes the request to the service layer.
6. The service layer applies business rules for payment creation.
7. The service layer sets the initial payment status, such as `CREATED`.
8. The service layer coordinates with the repository layer to save the payment.
9. The repository layer stores the payment data in the database using Spring Data JPA.
10. The database stores the payment record and an initial status history entry.
11. The repository returns the stored data to the service layer.
12. The service layer prepares the business result.
13. The controller returns an HTTP response to the frontend.
14. The frontend displays the result to the user.

### Simplified Flow Diagram

```text
User Action
   ↓
Frontend
   ↓
Controller Layer
   ↓
Service Layer
   ↓
Repository Layer
   ↓
Database
```

## Design Principles
The architecture is based on the following design principles:

### Separation of Concerns
Each layer has a distinct responsibility. This reduces overlap and helps the team understand where specific logic belongs.

### Maintainability
Clear layer boundaries make the system easier to update as requirements grow or change.

### Testability
When responsibilities are separated, each layer can be tested more easily and more independently.

### Reusability
Business logic in the service layer can be reused by different controllers or future application features.

### Simplicity
For a training project, a layered design provides structure without introducing unnecessary complexity.

### Consistency
A consistent request flow makes it easier for team members to collaborate and review the system design.

## Why Separation of Layers Is Important
Separating layers is important because it keeps the system organized and prevents responsibilities from becoming mixed together.

### 1. Better Readability
Developers can quickly understand where to find request handling, business rules, or database access logic.

### 2. Easier Maintenance
Changes in one layer are less likely to cause unintended side effects in other layers.

### 3. Reduced Complexity
When controllers, services, and repositories each do one type of work, the codebase stays simpler and more predictable.

### 4. Stronger Design Discipline
Keeping business logic out of controllers and repository logic out of services encourages cleaner architecture decisions.

### 5. Easier Team Collaboration
Different team members can work on frontend, business logic, and data access with clearer boundaries.

### 6. Improved Future Growth
A well-separated design is easier to extend with new features such as additional payment states, reporting, or security controls.

## Summary
The Payment Processing System uses a layered architecture in which:

- The **frontend** manages user interaction
- The **controller layer** handles HTTP communication and request format validation
- The **service layer** applies business rules and payment lifecycle logic
- The **repository layer** manages data access through Spring Data JPA
- The **database** stores users, payments, and payment status history

This structure provides a clean foundation for a training project and supports clarity, maintainability, and collaborative development.

---

# 支付处理系统架构设计

## 概述
本文档描述了 Payment Processing System 的分层架构。该架构的目标是让各部分职责清晰分离，从而使系统更易于理解、维护、测试和扩展。

应用采用如下结构：

```text
Frontend
    ↓
Controller Layer
    ↓
Service Layer
    ↓
Repository Layer
    ↓
Database
```

这种设计非常适合培训项目，因为它能够让团队清楚地看到请求如何在系统中流转，以及每一层分别负责什么。

## 各层职责

### Frontend
前端是系统中面向用户的部分。

职责：
- 提供用户交互页面和表单
- 收集用户输入的支付相关信息
- 向后端接口发送 HTTP 请求
- 展示支付结果、状态和错误信息

前端应专注于展示和交互，而不是支付处理规则本身。

### Controller Layer
控制器层是后端的入口层。

职责：
- 处理 HTTP 请求
- 校验请求格式
- 返回 HTTP 响应
- 将接收到的请求映射到后端操作

重要规则：
- 控制器层**不应包含业务逻辑**

这一层主要负责通信相关工作，例如请求解析、响应格式化和状态码返回。

### Service Layer
服务层包含系统的核心业务行为。

职责：
- 包含业务规则
- 处理支付生命周期逻辑
- 校验状态流转是否合法
- 协调数据库操作

这一层负责控制支付行为。例如，它需要确保支付按照如下合法状态进行流转：

`CREATED → VALIDATED → SENT → COMPLETED`

它还要保证非法状态流转会被拒绝，并在必要时允许支付进入 `FAILED` 状态。

### Repository Layer
仓储层负责服务层与数据库之间的持久化访问。

职责：
- 处理数据库访问
- 使用 Spring Data JPA
- 提供保存、查询和读取数据的方法
- 将数据访问逻辑与业务逻辑分离

该层应只关注持久化操作，不应做业务判断。

### Database
数据库是系统的持久化存储层。

包含：
- Users
- Payments
- Payment status history

数据库负责可靠地存储核心记录，使系统能够查询支付详情、跟踪支付进度，并保留状态历史信息。

## 创建支付的数据流示例
以下示例展示了一次创建支付请求如何在系统中流转。

### 分步骤流程
1. 用户在前端输入支付信息。
2. 前端向后端发送 HTTP 请求。
3. 控制器层接收请求。
4. 控制器校验请求格式和必填字段。
5. 控制器将请求传递给服务层。
6. 服务层应用创建支付所需的业务规则。
7. 服务层设置支付初始状态，例如 `CREATED`。
8. 服务层协调仓储层保存该支付。
9. 仓储层通过 Spring Data JPA 将支付数据写入数据库。
10. 数据库存储支付记录以及一条初始状态历史记录。
11. 仓储层将保存后的数据返回给服务层。
12. 服务层整理业务处理结果。
13. 控制器向前端返回 HTTP 响应。
14. 前端将结果展示给用户。

### 简化流程图

```text
User Action
   ↓
Frontend
   ↓
Controller Layer
   ↓
Service Layer
   ↓
Repository Layer
   ↓
Database
```

## 设计原则
该架构基于以下设计原则：

### 职责分离
每一层都有明确职责。这样可以减少功能重叠，并帮助团队理解不同逻辑应放在哪里。

### 可维护性
清晰的分层边界使系统在需求变化或功能扩展时更容易修改。

### 可测试性
当职责分离后，每一层都可以更容易、也更独立地进行测试。

### 可复用性
服务层中的业务逻辑可以被不同控制器或未来的新功能复用。

### 简单性
对于培训项目来说，分层设计可以提供足够的结构，同时不会引入不必要的复杂度。

### 一致性
一致的请求处理流程有助于团队协作，也便于进行设计评审。

## 为什么分层分离很重要
分层之所以重要，是因为它能保持系统结构清晰，避免不同职责混杂在一起。

### 1. 更好的可读性
开发者可以快速定位请求处理、业务规则或数据库访问逻辑所在的位置。

### 2. 更容易维护
某一层的修改不太容易对其他层造成意外影响。

### 3. 降低复杂度
当控制器、服务和仓储各自只负责一种工作时，代码库会更简单、更可预测。

### 4. 更强的设计纪律
将业务逻辑排除在控制器之外、将数据访问逻辑排除在服务之外，有助于保持更干净的架构设计。

### 5. 更利于团队协作
不同成员可以更清晰地围绕前端、业务逻辑和数据访问进行协作。

### 6. 更利于未来扩展
良好的分层设计更容易扩展出新特性，例如更多支付状态、报表能力或安全控制。

## 总结
Payment Processing System 使用如下分层架构：

- **frontend** 负责用户交互
- **controller layer** 负责 HTTP 通信和请求格式校验
- **service layer** 负责业务规则和支付生命周期逻辑
- **repository layer** 通过 Spring Data JPA 管理数据访问
- **database** 存储 users、payments 和 payment status history

该结构为培训项目提供了清晰的基础，支持良好的可读性、可维护性和协作开发。

