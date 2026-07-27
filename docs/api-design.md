# Payment Processing System API Design

## Overview
This document defines the REST API design for the Payment Processing System. The API is centered around payment creation, retrieval, history tracking, user-based lookup, and payment status updates.

The API follows REST principles:

- Resource-oriented URLs
- Standard HTTP methods
- JSON request and response bodies
- Clear and consistent HTTP status codes
- Separation between transport concerns and business logic

## Base Path

```text
/api/payments
```

Identifier rules used in this document:

- User-related APIs use `/api/payments/user/{id}` where `id` means a bigint user ID
- Payment-related APIs use `/api/payments/{payment_id}` where `payment_id` means a bigint transaction ID
- Identifier fields in JSON request and response bodies also use bigint values

## Payment Fields
The following fields are used throughout the API design:

- `paymentId`
- `sourceAccountId`
- `destinationAccountId`
- `amount`
- `currency`
- `status`
- `createdAt`
- `updatedAt`

ID-related fields such as `paymentId`, `sourceAccountId`, and `destinationAccountId` are represented as bigint values in the API.

Request-only field used by payment creation:

- `paymentPassword`

`paymentPassword` is accepted only in the create-payment request. It must never be stored in the `payments` table, must never be stored in the payment history table, and must never appear in any API response.

## Payment Status Values
The allowed payment statuses are:

- `CREATED`
- `VALIDATED`
- `SENT`
- `COMPLETED`
- `FAILED`

## Common Response Style
Responses should be returned in JSON format. For successful responses, the API should return the requested payment data or confirmation of the requested action. For validation or business rule failures, the API should return a clear `errorCode`, a readable `message`, and an appropriate HTTP status code so that the frontend and test cases can identify the exact error type.

Example error response:

```json
{
  "timestamp": "2026-07-27T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "errorCode": "INVALID_STATUS_TRANSITION",
  "message": "Cannot change COMPLETED to CREATED",
  "path": "/api/payments/101/status"
}
```

## Endpoints

### 1. Create Payment

**Purpose**  
Create a new payment record in the system. The initial payment status is assigned by the server.

**HTTP Method**  
`POST`

**URL**  
`/api/payments`

**Request JSON example**

```json
{
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "paymentPassword": "123456"
}
```

`paymentPassword` is a request-only field. It is used for payment password verification during payment creation. It must not be stored in `payments`, must not be stored in payment history, and must not appear in any response payload.

**Response JSON example**

```json
{
  "paymentId": 101,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "status": "CREATED",
  "createdAt": "2026-07-27T10:30:00Z",
  "updatedAt": "2026-07-27T10:30:00Z"
}
```

**Possible HTTP status codes**

- `201 Created` — payment created successfully
- `400 Bad Request` — invalid request format or missing required data
- `403 Forbidden` — payment password verification failed
- `409 Conflict` — payment creation conflicts with an existing business rule
- `500 Internal Server Error` — unexpected server-side failure

### 2. Get Payment By Payment ID

**Purpose**  
Retrieve a single payment by its `payment_id`.

**HTTP Method**  
`GET`

**URL**  
`/api/payments/{payment_id}`

**Request JSON example**  
No request body is required.

**Response JSON example**

```json
{
  "paymentId": 101,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "status": "VALIDATED",
  "createdAt": "2026-07-27T10:30:00Z",
  "updatedAt": "2026-07-27T10:32:00Z"
}
```

**Possible HTTP status codes**

- `200 OK` — payment found successfully
- `400 Bad Request` — invalid payment identifier format
- `404 Not Found` — payment does not exist
- `500 Internal Server Error` — unexpected server-side failure

### 3. Get Payment History

**Purpose**  
Retrieve the status history for a specific payment.

**HTTP Method**  
`GET`

**URL**  
`/api/payments/{payment_id}/history`

**Request JSON example**  
No request body is required.

**Response JSON example**

```json
{
  "paymentId": 101,
  "history": [
    {
      "historyId": 1001,
      "previousStatus": null,
      "newStatus": "CREATED",
      "changedAt": "2026-07-27T10:30:00Z",
      "notes": "Payment created successfully"
    },
    {
      "historyId": 1002,
      "previousStatus": "CREATED",
      "newStatus": "VALIDATED",
      "changedAt": "2026-07-27T10:32:00Z",
      "notes": "Payment request validated"
    },
    {
      "historyId": 1003,
      "previousStatus": "VALIDATED",
      "newStatus": "SENT",
      "changedAt": "2026-07-27T10:35:00Z",
      "notes": "Payment sent for processing"
    }
  ]
}
```

**Possible HTTP status codes**

- `200 OK` — payment history returned successfully
- `400 Bad Request` — invalid payment identifier format
- `404 Not Found` — payment does not exist
- `500 Internal Server Error` — unexpected server-side failure

### 4. Get Payments By User

**Purpose**  
Retrieve all payments associated with a specific user, including payments sent by the user and payments received by the user.

**HTTP Method**  
`GET`

**URL**  
`/api/payments/user/{id}`

**Request JSON example**  
No request body is required.

**Response JSON example**

```json
{
  "userId": 1001,
  "payments": [
    {
      "paymentId": 101,
      "sourceAccountId": 1001,
      "destinationAccountId": 1002,
      "amount": 1500.00,
      "currency": "USD",
      "status": "COMPLETED",
      "createdAt": "2026-07-27T10:30:00Z",
      "updatedAt": "2026-07-27T10:40:00Z"
    },
    {
      "paymentId": 102,
      "sourceAccountId": 1001,
      "destinationAccountId": 1003,
      "amount": 250.00,
      "currency": "USD",
      "status": "FAILED",
      "createdAt": "2026-07-27T11:00:00Z",
      "updatedAt": "2026-07-27T11:05:00Z"
    }
  ]
}
```

If the user exists but has no related payment records, the API should return `200 OK` with an empty `payments` array.

**Possible HTTP status codes**

- `200 OK` — user payments returned successfully
- `400 Bad Request` — invalid user identifier format
- `404 Not Found` — user does not exist
- `500 Internal Server Error` — unexpected server-side failure

### 5. Update Payment Status

**Purpose**  
Update the status of an existing payment as it moves through its lifecycle.

**HTTP Method**  
`PUT`

**URL**  
`/api/payments/{payment_id}/status`

**Request JSON example**

```json
{
  "status": "SENT"
}
```

**Response JSON example**

```json
{
  "paymentId": 101,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "status": "SENT",
  "createdAt": "2026-07-27T10:30:00Z",
  "updatedAt": "2026-07-27T10:35:00Z"
}
```

**Possible HTTP status codes**

- `200 OK` — payment status updated successfully
- `400 Bad Request` — invalid request format or invalid status value
- `404 Not Found` — payment does not exist
- `409 Conflict` — invalid status transition
- `500 Internal Server Error` — unexpected server-side failure

## Status Transition Considerations
The supported payment statuses are frozen as:

`CREATED`, `VALIDATED`, `SENT`, `COMPLETED`, `FAILED`

The allowed status transitions are:

- `CREATED → VALIDATED`
- `CREATED → FAILED`
- `VALIDATED → SENT`
- `VALIDATED → FAILED`
- `SENT → COMPLETED`
- `SENT → FAILED`

Terminal states:

- `COMPLETED` — no further transitions allowed
- `FAILED` — no further transitions allowed

The API should reject invalid or inconsistent status updates, such as `CREATED → COMPLETED`.

## REST Design Notes
This API design follows REST principles in the following ways:

- Payments are treated as resources
- Endpoints use nouns rather than action-based names
- HTTP methods reflect the type of operation being performed
- Nested resource paths are used for related data such as payment history
- Response payloads are designed to be predictable and easy for the frontend to consume

## Summary
The Payment Processing System API provides a focused REST interface for:

- Creating payments
- Retrieving payment details
- Viewing payment history
- Listing payments by user
- Updating payment status

This design supports the training goals of demonstrating clean API structure, payment lifecycle handling, and clear communication between frontend and backend systems.

---

# 支付处理系统 API 设计

## 概述
本文档定义了 Payment Processing System 的 REST API 设计。该 API 主要围绕支付创建、支付查询、历史记录跟踪、按用户查询支付以及支付状态更新展开。

API 遵循 REST 原则：

- 面向资源的 URL 设计
- 使用标准 HTTP 方法
- 使用 JSON 请求与响应体
- 使用清晰且一致的 HTTP 状态码
- 将传输层关注点与业务逻辑分离

## 基础路径

```text
/api/payments
```

本文档中的标识符命名规则如下：

- 与用户 ID 相关的接口统一使用 `/api/payments/user/{id}`，其中 `id` 表示 bigint 类型的用户 ID
- 与支付交易相关的接口统一使用 `/api/payments/{payment_id}`，其中 `payment_id` 表示 bigint 类型的某一笔具体交易 ID
- JSON 请求体与响应体中的各类标识字段也统一使用 bigint 值

## 支付字段
以下字段会在 API 设计中使用：

- `paymentId`
- `sourceAccountId`
- `destinationAccountId`
- `amount`
- `currency`
- `status`
- `createdAt`
- `updatedAt`

其中 `paymentId`、`sourceAccountId`、`destinationAccountId` 等标识字段在 API 中统一表示为 bigint 类型。

创建支付时使用的请求专属字段：

- `paymentPassword`

`paymentPassword` 只允许出现在创建支付请求中。它不能写入 `payments` 表，不能写入支付历史表，也不能出现在任何 API 响应中。

## 支付状态值
允许的支付状态如下：

- `CREATED`
- `VALIDATED`
- `SENT`
- `COMPLETED`
- `FAILED`

## 通用响应风格
响应应采用 JSON 格式返回。对于成功响应，API 应返回请求的数据或操作成功确认。对于校验失败或业务规则失败，API 应返回清晰的 `errorCode`、可读的 `message` 以及适当的 HTTP 状态码，以便前端和测试代码判断具体错误类型。

错误响应示例：

```json
{
  "timestamp": "2026-07-27T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "errorCode": "INVALID_STATUS_TRANSITION",
  "message": "Cannot change COMPLETED to CREATED",
  "path": "/api/payments/101/status"
}
```

## 接口列表

### 1. Create Payment

**Purpose**  
在系统中创建一条新的支付记录。初始支付状态由服务端设置。

**HTTP Method**  
`POST`

**URL**  
`/api/payments`

**Request JSON example**

```json
{
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "paymentPassword": "123456"
}
```

`paymentPassword` 是一个仅用于请求的临时字段，用于在创建支付时进行支付密码校验。它不能写入 `payments`，不能写入支付历史，也不能出现在任何响应体中。

**Response JSON example**

```json
{
  "paymentId": 101,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "status": "CREATED",
  "createdAt": "2026-07-27T10:30:00Z",
  "updatedAt": "2026-07-27T10:30:00Z"
}
```

**Possible HTTP status codes**

- `201 Created` — payment created successfully
- `400 Bad Request` — invalid request format or missing required data
- `403 Forbidden` — payment password verification failed
- `409 Conflict` — payment creation conflicts with an existing business rule
- `500 Internal Server Error` — unexpected server-side failure

### 2. Get Payment By Payment ID

**Purpose**  
根据支付标识获取单笔支付详情。

**HTTP Method**  
`GET`

**URL**  
`/api/payments/{payment_id}`

**Request JSON example**  
该接口无需请求体。

**Response JSON example**

```json
{
  "paymentId": 101,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "status": "VALIDATED",
  "createdAt": "2026-07-27T10:30:00Z",
  "updatedAt": "2026-07-27T10:32:00Z"
}
```

**Possible HTTP status codes**

- `200 OK` — payment found successfully
- `400 Bad Request` — invalid payment identifier format
- `404 Not Found` — payment does not exist
- `500 Internal Server Error` — unexpected server-side failure

### 3. Get Payment History

**Purpose**  
获取指定支付的状态变更历史。

**HTTP Method**  
`GET`

**URL**  
`/api/payments/{payment_id}/history`

**Request JSON example**  
该接口无需请求体。

**Response JSON example**

```json
{
  "paymentId": 101,
  "history": [
    {
      "historyId": 1001,
      "previousStatus": null,
      "newStatus": "CREATED",
      "changedAt": "2026-07-27T10:30:00Z",
      "notes": "Payment created successfully"
    },
    {
      "historyId": 1002,
      "previousStatus": "CREATED",
      "newStatus": "VALIDATED",
      "changedAt": "2026-07-27T10:32:00Z",
      "notes": "Payment request validated"
    },
    {
      "historyId": 1003,
      "previousStatus": "VALIDATED",
      "newStatus": "SENT",
      "changedAt": "2026-07-27T10:35:00Z",
      "notes": "Payment sent for processing"
    }
  ]
}
```

**Possible HTTP status codes**

- `200 OK` — payment history returned successfully
- `400 Bad Request` — invalid payment identifier format
- `404 Not Found` — payment does not exist
- `500 Internal Server Error` — unexpected server-side failure

### 4. Get Payments By User

**Purpose**  
获取与指定用户关联的所有支付记录，包括该用户发出的支付以及该用户收到的支付。

**HTTP Method**  
`GET`

**URL**  
`/api/payments/user/{id}`

**Request JSON example**  
该接口无需请求体。

**Response JSON example**

```json
{
  "userId": 1001,
  "payments": [
    {
      "paymentId": 101,
      "sourceAccountId": 1001,
      "destinationAccountId": 1002,
      "amount": 1500.00,
      "currency": "USD",
      "status": "COMPLETED",
      "createdAt": "2026-07-27T10:30:00Z",
      "updatedAt": "2026-07-27T10:40:00Z"
    },
    {
      "paymentId": 102,
      "sourceAccountId": 1001,
      "destinationAccountId": 1003,
      "amount": 250.00,
      "currency": "USD",
      "status": "FAILED",
      "createdAt": "2026-07-27T11:00:00Z",
      "updatedAt": "2026-07-27T11:05:00Z"
    }
  ]
}
```

如果用户存在，但当前没有任何关联支付记录，接口应返回 `200 OK`，并且 `payments` 数组为空。

**Possible HTTP status codes**

- `200 OK` — user payments returned successfully
- `400 Bad Request` — invalid user identifier format
- `404 Not Found` — user does not exist
- `500 Internal Server Error` — unexpected server-side failure

### 5. Update Payment Status

**Purpose**  
更新已有支付的状态，使其沿着生命周期继续流转。

**HTTP Method**  
`PUT`

**URL**  
`/api/payments/{payment_id}/status`

**Request JSON example**

```json
{
  "status": "SENT"
}
```

**Response JSON example**

```json
{
  "paymentId": 101,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "status": "SENT",
  "createdAt": "2026-07-27T10:30:00Z",
  "updatedAt": "2026-07-27T10:35:00Z"
}
```

**Possible HTTP status codes**

- `200 OK` — payment status updated successfully
- `400 Bad Request` — invalid request format or invalid status value
- `404 Not Found` — payment does not exist
- `409 Conflict` — invalid status transition
- `500 Internal Server Error` — unexpected server-side failure

## 状态流转说明
当前已冻结的支付状态如下：

`CREATED`、`VALIDATED`、`SENT`、`COMPLETED`、`FAILED`

允许的状态流转如下：

- `CREATED → VALIDATED`
- `CREATED → FAILED`
- `VALIDATED → SENT`
- `VALIDATED → FAILED`
- `SENT → COMPLETED`
- `SENT → FAILED`

终态说明：

- `COMPLETED` —— 不允许继续流转
- `FAILED` —— 不允许继续流转

API 应拒绝不合法或不一致的状态更新，例如 `CREATED → COMPLETED`。具体规则由服务层实现。

## REST 设计说明
本 API 在以下方面遵循 REST 原则：

- 将 payments 视为资源
- 使用名词而不是动作式命名
- HTTP 方法与操作类型相对应
- 对于支付历史等关联数据使用嵌套资源路径
- 响应体设计尽量稳定且便于前端消费

## 总结
Payment Processing System API 提供了一组聚焦的 REST 接口，用于：

- 创建支付
- 查询支付详情
- 查看支付历史
- 按用户列出支付
- 更新支付状态

该设计支持培训项目目标，能够展示清晰的 API 结构、支付生命周期处理过程，以及前后端之间的明确通信方式。

