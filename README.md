# Payment Processing System

## Project Overview
The Payment Processing System is a banking training project that simulates the lifecycle of a payment as it moves through core processing stages.

The primary payment flow is:

`CREATED → VALIDATED → SENT → COMPLETED`

A payment may also transition to `FAILED` if validation or processing cannot be completed successfully.

This project is intended to help the team demonstrate how a payment platform can be designed in a clear, structured way for learning purposes. It focuses on service design, status tracking, backend organization, and the interaction between a backend API and a simple frontend interface.

## Features
- Simulate a payment lifecycle from creation through completion
- Track payment status changes, including failure scenarios
- Expose REST APIs for payment creation, retrieval, and monitoring
- Persist payment information in a relational database
- Maintain payment history for visibility and traceability
- Provide a simple frontend for interacting with the system
- Demonstrate a clean backend architecture suitable for training and review

## Technology Stack
- **Backend:** Spring Boot
- **Persistence:** Spring Data JPA
- **Database:** MySQL
- **Frontend:** Simple web-based user interface
- **Documentation:** Markdown-based project and design documents

## System Architecture Overview
The system is organized into three main parts:

- **Frontend**
  - Provides screens and forms for user interaction
  - Sends requests to the backend and displays payment results

- **Backend**
  - Exposes REST APIs
  - Applies payment-processing rules
  - Manages payment lifecycle transitions
  - Coordinates database operations

- **Database**
  - Stores payment records and related status information
  - Supports payment retrieval and history tracking

At a high level, the frontend communicates with the backend over HTTP, and the backend persists and retrieves payment data from MySQL.

## Team Members
This section can be updated by the team as contributors are confirmed.

- Richard
- Lyra
- Kylian
- Leon

## Project Structure

```text
payment-processing-system/
├── docs/
│   ├── architecture.md
│   ├── api-design.md
│   └── database-design.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

### Structure Summary
- `README.md` — project summary and repository introduction
- `docs/architecture.md` — high-level architecture and component responsibilities
- `docs/api-design.md` — planned API design and frontend/backend communication approach
- `docs/database-design.md` — planned database structure and data model overview
- `src/main/java/` — Spring Boot backend source code
- `src/main/resources/` — backend configuration files and future static resources
- `src/test/java/` — backend test source code
- `src/main/resources/application-local.properties.example` — local database configuration example for team members
- `pom.xml` — Maven build configuration

### Frontend Location Decision
The backend is already organized as a root-level Maven project.

Before frontend implementation starts, the team should keep the frontend location decision consistent. For this one-week training project, the recommended default is:

- `src/main/resources/static/` for a simple Spring Boot-served demo frontend

If the team later chooses a separately managed frontend, a top-level `frontend/` directory can be introduced deliberately rather than by accident.

### Local Database Configuration
The default backend configuration reads database credentials from environment variables:

- `DB_USERNAME`
- `DB_PASSWORD`

Current defaults are intended for local training use only.

An example local override template is provided in:

- `src/main/resources/application-local.properties.example`

Also note:

- `spring.jpa.hibernate.ddl-auto=none` means Hibernate will not create tables automatically
- the database schema must be created separately by the database owner or supplied through a future schema setup process

## Development Workflow
The project is expected to progress in a structured and collaborative way:

1. Define requirements and payment lifecycle expectations
2. Review architecture, API, and database design documents
3. Build backend APIs for payment operations
4. Integrate MySQL persistence and status history tracking
5. Develop the frontend interface for basic user interaction
6. Test end-to-end payment flows and failure scenarios
7. Refine documentation and prepare for project review

This workflow helps keep the team aligned while ensuring that design decisions are understood before implementation expands.

## Future Improvements
- Add authentication and role-based access control
- Expand payment scenarios beyond the initial lifecycle
- Introduce stronger audit and reporting capabilities
- Improve frontend usability and status visualizations
- Add automated testing for API and lifecycle validation
- Support additional operational views for instructors and reviewers
- Prepare the project for more advanced deployment and monitoring practices

---

# 支付处理系统

## 项目概述
支付处理系统是一个银行业务培训项目，用于模拟一笔支付在核心处理阶段中的生命周期流转。

主要支付流程如下：

`CREATED → VALIDATED → SENT → COMPLETED`

如果校验或处理未能成功完成，支付也可能转换为 `FAILED` 状态。

本项目旨在帮助团队以清晰、结构化的方式展示支付平台的设计思路，用于教学与学习。项目重点关注服务设计、状态跟踪、后端架构组织，以及后端 API 与简单前端界面之间的交互方式。

## 功能特性
- 模拟支付从创建到完成的生命周期
- 跟踪支付状态变化，包括失败场景
- 提供用于支付创建、查询和监控的 REST API
- 将支付信息持久化到关系型数据库中
- 维护支付历史记录，以支持可视化和可追溯性
- 提供一个简单的前端界面与系统交互
- 展示适合培训和评审的清晰后端架构

## 技术栈
- **后端：** Spring Boot
- **持久层：** Spring Data JPA
- **数据库：** MySQL
- **前端：** 简单的 Web 用户界面
- **文档：** 基于 Markdown 的项目与设计文档

## 系统架构概览
系统主要由三个部分组成：

- **前端**
  - 提供用户交互页面和表单
  - 向后端发送请求并展示支付结果

- **后端**
  - 提供 REST API
  - 应用支付处理规则
  - 管理支付生命周期状态流转
  - 协调数据库操作

- **数据库**
  - 存储支付记录及相关状态信息
  - 支持支付查询和历史跟踪

从整体上看，前端通过 HTTP 与后端通信，后端负责将支付数据持久化到 MySQL 中并进行读取。

## 团队成员
该部分可在团队成员确认后进行更新。

- Richard
- Lyra
- Kylian
- Leon

## 项目结构

```text
payment-processing-system/
├── docs/
│   ├── architecture.md
│   ├── api-design.md
│   └── database-design.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

### 结构说明
- `README.md` — 项目摘要与仓库介绍
- `docs/architecture.md` — 高层架构与组件职责说明
- `docs/api-design.md` — 计划中的 API 设计与前后端通信方式
- `docs/database-design.md` — 计划中的数据库结构与数据模型概览
- `src/main/java/` — Spring Boot 后端源码
- `src/main/resources/` — 后端配置文件以及未来可能的静态资源位置
- `src/test/java/` — 后端测试源码
- `src/main/resources/application-local.properties.example` — 提供给组员参考的本地数据库配置示例
- `pom.xml` — Maven 构建配置

### 前端放置位置说明
当前后端已经按照根目录 Maven 项目结构组织。

在前端正式开始开发前，团队应统一前端放置位置。对于当前这一周的培训项目，推荐优先采用：

- `src/main/resources/static/`，用于放置由 Spring Boot 直接提供的简单演示前端

如果后续决定使用独立管理的前端，再有计划地引入顶层 `frontend/` 目录，而不是在开发过程中临时分叉。

### 本地数据库配置说明
当前后端配置默认通过环境变量读取数据库凭据：

- `DB_USERNAME`
- `DB_PASSWORD`

当前默认值仅适用于本地培训演示环境。

仓库中还提供了一个本地覆盖配置模板：

- `src/main/resources/application-local.properties.example`

同时请注意：

- `spring.jpa.hibernate.ddl-auto=none` 表示 Hibernate 不会自动建表
- 数据库结构需要由数据库负责同学单独提供，或由后续的 schema 初始化方案提供

## 开发流程
项目预期将以结构化、协作化的方式推进：

1. 明确需求和支付生命周期预期
2. 评审架构、API 和数据库设计文档
3. 构建支付操作相关的后端 API
4. 集成 MySQL 持久化与状态历史跟踪
5. 开发用于基础交互的前端界面
6. 测试端到端支付流程及失败场景
7. 完善文档并准备项目评审

该流程有助于团队保持一致，并确保在实现逐步扩展之前，关键设计决策已经被理解和确认。

## 未来改进方向
- 增加身份认证与基于角色的访问控制
- 扩展初始生命周期之外的支付场景
- 引入更完善的审计与报表能力
- 优化前端可用性和状态可视化展示
- 增加 API 与生命周期校验的自动化测试
- 为教师和评审人员提供更多运行与观察视图
- 为更高级的部署与监控实践做好准备

