# Payment Processing System Database Design

## Overview
This document describes the database design for the Payment Processing System. The database is designed to support payment processing, user account management, and payment lifecycle tracking.

The database contains three main tables:

1. `users`
2. `payments`
3. `payment_status_history`

The design focuses on keeping the current state of a payment easy to query while also preserving a history of status changes for traceability.

Unless otherwise noted, identifier columns such as primary keys and cross-table reference IDs are stored as `BIGINT`.

## Design Goals
The database design aims to support:

- Storage of user account information
- Storage of payment transaction details
- Tracking of payment lifecycle changes over time
- Clear relationships between user accounts and payments
- Simple and maintainable data organization for a training project

## Table Descriptions

### 1. `users`
**Purpose:** Store user account information.

This table represents user-related account data that can be associated with payment activity. It stores account identifiers, account balances, and account status information.

#### Column Description

| Column | Description |
|---|---|
| `id` | Unique bigint identifier for the user record |
| `account_number` | Account number associated with the user |
| `balance` | Current balance of the account |
| `status` | Current user or account status |

### 2. `payments`
**Purpose:** Store payment transaction information.

This table stores the main payment record. Each row represents a payment transaction between a source account and a destination account. It contains the current payment status and key timestamps.

#### Column Description

| Column | Description |
|---|---|
| `id` | Unique bigint identifier for the payment record |
| `source_account_id` | Bigint reference to the sending user/account ID (`users.id`) |
| `destination_account_id` | Bigint reference to the receiving user/account ID (`users.id`) |
| `amount` | Payment amount |
| `currency` | Currency used for the payment |
| `status` | Current status of the payment |
| `created_at` | Timestamp when the payment was created |
| `updated_at` | Timestamp when the payment was last updated |

### 3. `payment_status_history`
**Purpose:** Store payment lifecycle changes.

This table stores the history of payment status transitions. Each row represents one change in payment status, including when the change happened and optional notes that explain the event.

#### Column Description

| Column | Description |
|---|---|
| `id` | Unique bigint identifier for the history record |
| `payment_id` | Bigint reference to the related payment (`payments.id`) |
| `previous_status` | The status before the change |
| `new_status` | The status after the change |
| `changed_at` | Timestamp when the status changed |
| `notes` | Optional notes about the status change |

## Relationship Explanation

### `users` and `payments`
- A user account can act as the source account for many payments.
- A user account can also act as the destination account for many payments.
- Each payment references one source account and one destination account.

This means the `payments` table is related to the `users` table through `users.id`, which is stored in `source_account_id` and `destination_account_id` as bigint foreign-key-style references.

### `payments` and `payment_status_history`
- One payment can have many status history records.
- Each status history record belongs to exactly one payment.

This relationship allows the system to store both:
- the **current payment status** in `payments`
- the **full status transition history** in `payment_status_history`

## Entity Relationship Overview

```text
users (1) -------- (many) payments [as source account]
users (1) -------- (many) payments [as destination account]
payments (1) ----- (many) payment_status_history
```

## Example Records

### Example `users` Record

| id | account_number | balance | status |
|---|---|---:|---|
| 1001 | ACC-10001 | 5000.00 | ACTIVE |
| 1002 | ACC-20001 | 3200.00 | ACTIVE |

### Example `payments` Record

| id | source_account_id | destination_account_id | amount | currency | status | created_at | updated_at |
|---|---|---|---:|---|---|---|---|
| 101 | 1001 | 1002 | 1500.00 | USD | SENT | 2026-07-27 10:30:00 | 2026-07-27 10:35:00 |

### Example `payment_status_history` Records

| id | payment_id | previous_status | new_status | changed_at | notes |
|---|---:|---|---|---|---|
| 1001 | 101 | NULL | CREATED | 2026-07-27 10:30:00 | Payment created successfully |
| 1002 | 101 | CREATED | VALIDATED | 2026-07-27 10:32:00 | Payment request validated |
| 1003 | 101 | VALIDATED | SENT | 2026-07-27 10:35:00 | Payment sent for processing |

## Why `payment_status_history` Is Separated from `payments`
The `payment_status_history` table is separated from `payments` for several important reasons.

### 1. Preserve Full Lifecycle History
The `payments` table should store the current state of a payment. If status changes were stored only in the `payments` table, previous states would be lost. A separate history table preserves the full lifecycle of each payment.

### 2. Improve Traceability
Payment systems need visibility into how and when a payment changed state. A dedicated history table makes it easier to review transitions such as:

`CREATED → VALIDATED → SENT → COMPLETED`

or transitions to `FAILED`.

### 3. Support Auditing and Troubleshooting
When something goes wrong, the team needs to understand what happened and when it happened. A separate history table provides a clear record for troubleshooting, support, and review.

### 4. Keep the `payments` Table Focused
The `payments` table should represent the latest state of a payment. Mixing current-state data with all historical transitions in the same structure would make the design less clear and harder to manage.

### 5. Simplify Queries for Current Status
Most day-to-day operations need the current payment status quickly. Keeping the latest status directly in `payments` makes these queries simpler, while the history table remains available for detailed tracking.

## Data Usage Summary
- `users` stores account-related user information
- `payments` stores the current state of each payment transaction
- `payment_status_history` stores the record of lifecycle changes for each payment

Together, these tables provide both operational data and historical visibility, which is important for a payment processing system used in a banking training context.

## Summary
The database design is centered around three clearly separated tables:

- `users` for account information
- `payments` for payment transaction records
- `payment_status_history` for payment lifecycle tracking

This structure keeps the database easy to understand while supporting both current payment operations and historical status analysis.

---

# 支付处理系统数据库设计

## 概述
本文档描述了 Payment Processing System 的数据库设计。该数据库用于支持支付处理、用户账户管理以及支付生命周期跟踪。

数据库包含三张主要表：

1. `users`
2. `payments`
3. `payment_status_history`

该设计重点在于：一方面让支付当前状态易于查询，另一方面保留完整的状态变更历史以支持追踪。

除非特别说明，主键以及跨表关联的标识字段统一使用 `BIGINT` 存储。

## 设计目标
数据库设计旨在支持：

- 存储用户账户信息
- 存储支付交易数据
- 跟踪支付生命周期中的状态变化
- 建立用户账户与支付之间清晰的关系
- 为培训项目提供简单且易维护的数据组织方式

## 表说明

### 1. `users`
**Purpose:** Store user account information.

该表表示与支付活动相关的用户账户数据，存储账户标识、账户余额以及账户状态信息。

#### 字段说明

| Column | Description |
|---|---|
| `id` | 用户记录的唯一 bigint 标识 |
| `account_number` | 与用户关联的账户号码 |
| `balance` | 账户当前余额 |
| `status` | 用户或账户当前状态 |

### 2. `payments`
**Purpose:** Store payment transaction information.

该表存储主要的支付记录。每一行表示一笔从源账户到目标账户的支付交易，包含当前支付状态以及关键时间戳。

#### 字段说明

| Column | Description |
|---|---|
| `id` | 支付记录的唯一 bigint 标识 |
| `source_account_id` | 指向付款用户/账户 ID（`users.id`）的 bigint 引用 |
| `destination_account_id` | 指向收款用户/账户 ID（`users.id`）的 bigint 引用 |
| `amount` | 支付金额 |
| `currency` | 支付所使用的货币 |
| `status` | 支付当前状态 |
| `created_at` | 支付创建时间 |
| `updated_at` | 支付最后更新时间 |

### 3. `payment_status_history`
**Purpose:** Store payment lifecycle changes.

该表存储支付状态流转历史。每一行表示一次状态变化，包括变化发生的时间以及可选说明信息。

#### 字段说明

| Column | Description |
|---|---|
| `id` | 历史记录的唯一 bigint 标识 |
| `payment_id` | 指向支付记录（`payments.id`）的 bigint 引用 |
| `previous_status` | 变更前的状态 |
| `new_status` | 变更后的状态 |
| `changed_at` | 状态变更发生的时间 |
| `notes` | 关于本次状态变更的可选备注 |

## 关系说明

### `users` 与 `payments`
- 一个用户账户可以作为多笔支付的付款账户。
- 一个用户账户也可以作为多笔支付的收款账户。
- 每一笔支付都引用一个源账户和一个目标账户。

这意味着 `payments` 表通过 `source_account_id` 和 `destination_account_id` 中保存的 `users.id` bigint 引用与 `users` 表建立关联。

### `payments` 与 `payment_status_history`
- 一笔支付可以拥有多条状态历史记录。
- 每一条状态历史记录只属于一笔支付。

这种关系使系统能够同时存储：
- `payments` 中的**当前支付状态**
- `payment_status_history` 中的**完整状态流转历史**

## 实体关系概览

```text
users (1) -------- (many) payments [as source account]
users (1) -------- (many) payments [as destination account]
payments (1) ----- (many) payment_status_history
```

## 示例记录

### `users` 示例记录

| id | account_number | balance | status |
|---|---|---:|---|
| 1001 | ACC-10001 | 5000.00 | ACTIVE |
| 1002 | ACC-20001 | 3200.00 | ACTIVE |

### `payments` 示例记录

| id | source_account_id | destination_account_id | amount | currency | status | created_at | updated_at |
|---|---|---|---:|---|---|---|---|
| 101 | 1001 | 1002 | 1500.00 | USD | SENT | 2026-07-27 10:30:00 | 2026-07-27 10:35:00 |

### `payment_status_history` 示例记录

| id | payment_id | previous_status | new_status | changed_at | notes |
|---|---:|---|---|---|---|
| 1001 | 101 | NULL | CREATED | 2026-07-27 10:30:00 | Payment created successfully |
| 1002 | 101 | CREATED | VALIDATED | 2026-07-27 10:32:00 | Payment request validated |
| 1003 | 101 | VALIDATED | SENT | 2026-07-27 10:35:00 | Payment sent for processing |

## 为什么将 `payment_status_history` 与 `payments` 分开
将 `payment_status_history` 表从 `payments` 中单独拆分出来，有以下几个重要原因。

### 1. 保留完整生命周期历史
`payments` 表应保存支付的当前状态。如果所有状态变化都只保存在 `payments` 表中，之前的状态就会丢失。单独的历史表可以保留每笔支付完整的生命周期。

### 2. 提高可追踪性
支付系统需要清楚知道支付何时、如何发生状态变化。独立的历史表可以更容易地回顾如下流转：

`CREATED → VALIDATED → SENT → COMPLETED`

以及转为 `FAILED` 的情况。

### 3. 支持审计与问题排查
当支付处理出现问题时，团队需要知道发生了什么以及发生的时间。独立的历史表可以为排障、支持和评审提供清晰记录。

### 4. 保持 `payments` 表职责单一
`payments` 表应表示支付的最新状态。如果把当前状态与全部历史变更混在同一结构中，会使设计更不清晰，也更难管理。

### 5. 简化当前状态查询
大多数日常操作都需要快速获取支付当前状态。将最新状态直接保存在 `payments` 表中可以使查询更简单，而历史表则专门用于详细追踪。

## 数据使用总结
- `users` 存储账户相关的用户信息
- `payments` 存储每笔支付交易的当前状态
- `payment_status_history` 存储每笔支付生命周期变化的记录

三张表结合起来，同时提供了操作性数据和历史可见性，这对于银行培训场景下的支付处理系统非常重要。

## 总结
该数据库设计围绕三张职责清晰分离的表展开：

- `users` 用于账户信息
- `payments` 用于支付交易记录
- `payment_status_history` 用于支付生命周期跟踪

这一结构既保证了数据库易于理解，也支持当前支付操作和历史状态分析。

