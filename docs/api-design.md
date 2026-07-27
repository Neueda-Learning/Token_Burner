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

This path is the shared resource prefix, not the standalone create-payment endpoint.

Identifier rules used in this document:

- User-related APIs use `/api/payments/user/{id}` where `id` means the user ID
- Payment-related APIs use `/api/payments/{payment_id}` where `payment_id` means a specific transaction ID

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

## Payment Status Values
The allowed payment statuses are:

- `CREATED`
- `VALIDATED`
- `SENT`
- `COMPLETED`
- `FAILED`

## Common Response Style
Responses should be returned in JSON format. For successful responses, the API should return the requested payment data or confirmation of the requested action. For validation or business rule failures, the API should return a clear error message and appropriate HTTP status code.

Example error response:

```json
{
  "timestamp": "2026-07-27T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid payment request",
  "path": "/api/payments/user/{id}"
}
```

## Endpoints

### 1. Create Payment

**Purpose**  
Create a new payment record for the current user ID with an initial payment status.

**HTTP Method**  
`POST`

**URL**  
`/api/payments/user/{id}`

**Request JSON example**

```json
{
  "sourceAccountId": "ACC-10001",
  "destinationAccountId": "ACC-20001",
  "amount": 1500.00,
  "currency": "USD",
  "status": "CREATED"
}
```

**Response JSON example**

```json
{
  "paymentId": "PAY-10001",
  "sourceAccountId": "ACC-10001",
  "destinationAccountId": "ACC-20001",
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
  "paymentId": "PAY-10001",
  "sourceAccountId": "ACC-10001",
  "destinationAccountId": "ACC-20001",
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
  "paymentId": "PAY-10001",
  "history": [
    {
      "status": "CREATED",
      "updatedAt": "2026-07-27T10:30:00Z"
    },
    {
      "status": "VALIDATED",
      "updatedAt": "2026-07-27T10:32:00Z"
    },
    {
      "status": "SENT",
      "updatedAt": "2026-07-27T10:35:00Z"
    }
  ]
}
```

**Possible HTTP status codes**

- `200 OK` — payment history returned successfully
- `400 Bad Request` — invalid payment identifier format
- `404 Not Found` — payment or history record does not exist
- `500 Internal Server Error` — unexpected server-side failure

### 4. Get Payments By User

**Purpose**  
Retrieve all payments associated with a specific user.

**HTTP Method**  
`GET`

**URL**  
`/api/payments/user/{id}`

**Request JSON example**  
No request body is required.

**Response JSON example**

```json
{
  "userId": "USER-1001",
  "payments": [
    {
      "paymentId": "PAY-10001",
      "sourceAccountId": "ACC-10001",
      "destinationAccountId": "ACC-20001",
      "amount": 1500.00,
      "currency": "USD",
      "status": "COMPLETED",
      "createdAt": "2026-07-27T10:30:00Z",
      "updatedAt": "2026-07-27T10:40:00Z"
    },
    {
      "paymentId": "PAY-10002",
      "sourceAccountId": "ACC-10001",
      "destinationAccountId": "ACC-30001",
      "amount": 250.00,
      "currency": "USD",
      "status": "FAILED",
      "createdAt": "2026-07-27T11:00:00Z",
      "updatedAt": "2026-07-27T11:05:00Z"
    }
  ]
}
```

**Possible HTTP status codes**

- `200 OK` — user payments returned successfully
- `400 Bad Request` — invalid user identifier format
- `404 Not Found` — user does not exist or has no accessible records
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
  "paymentId": "PAY-10001",
  "sourceAccountId": "ACC-10001",
  "destinationAccountId": "ACC-20001",
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
The payment lifecycle is expected to follow this general path:

`CREATED → VALIDATED → SENT → COMPLETED`

A payment may also transition to `FAILED` if processing cannot continue successfully.

The API should reject invalid or inconsistent status updates. For example, an update that attempts to move directly from `CREATED` to `COMPLETED` may be rejected depending on the business rules defined in the service layer.

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

该路径表示统一的资源前缀，不表示可直接调用的创建支付接口。

本文档中的标识符命名规则如下：

- 与用户 ID 相关的接口统一使用 `/api/payments/user/{id}`，其中 `id` 表示用户 ID
- 与支付交易相关的接口统一使用 `/api/payments/{payment_id}`，其中 `payment_id` 表示某一笔具体交易的 ID

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

## 支付状态值
允许的支付状态如下：

- `CREATED`
- `VALIDATED`
- `SENT`
- `COMPLETED`
- `FAILED`

## 通用响应风格
响应应采用 JSON 格式返回。对于成功响应，API 应返回请求的数据或操作成功确认。对于校验失败或业务规则失败，API 应返回清晰的错误信息以及适当的 HTTP 状态码。

错误响应示例：

```json
{
  "timestamp": "2026-07-27T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid payment request",
  "path": "/api/payments/user/{id}"
}
```

## 接口列表

### 1. Create Payment

**Purpose**  
为当前用户 ID 创建一条新的支付记录，并为其设置初始支付状态。

**HTTP Method**  
`POST`

**URL**  
`/api/payments/user/{id}`

**Request JSON example**

```json
{
  "sourceAccountId": "ACC-10001",
  "destinationAccountId": "ACC-20001",
  "amount": 1500.00,
  "currency": "USD",
  "status": "CREATED"
}
```

**Response JSON example**

```json
{
  "paymentId": "PAY-10001",
  "sourceAccountId": "ACC-10001",
  "destinationAccountId": "ACC-20001",
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
  "paymentId": "PAY-10001",
  "sourceAccountId": "ACC-10001",
  "destinationAccountId": "ACC-20001",
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
  "paymentId": "PAY-10001",
  "history": [
    {
      "status": "CREATED",
      "updatedAt": "2026-07-27T10:30:00Z"
    },
    {
      "status": "VALIDATED",
      "updatedAt": "2026-07-27T10:32:00Z"
    },
    {
      "status": "SENT",
      "updatedAt": "2026-07-27T10:35:00Z"
    }
  ]
}
```

**Possible HTTP status codes**

- `200 OK` — payment history returned successfully
- `400 Bad Request` — invalid payment identifier format
- `404 Not Found` — payment or history record does not exist
- `500 Internal Server Error` — unexpected server-side failure

### 4. Get Payments By User

**Purpose**  
获取与指定用户关联的所有支付记录。

**HTTP Method**  
`GET`

**URL**  
`/api/payments/user/{id}`

**Request JSON example**  
该接口无需请求体。

**Response JSON example**

```json
{
  "userId": "USER-1001",
  "payments": [
    {
      "paymentId": "PAY-10001",
      "sourceAccountId": "ACC-10001",
      "destinationAccountId": "ACC-20001",
      "amount": 1500.00,
      "currency": "USD",
      "status": "COMPLETED",
      "createdAt": "2026-07-27T10:30:00Z",
      "updatedAt": "2026-07-27T10:40:00Z"
    },
    {
      "paymentId": "PAY-10002",
      "sourceAccountId": "ACC-10001",
      "destinationAccountId": "ACC-30001",
      "amount": 250.00,
      "currency": "USD",
      "status": "FAILED",
      "createdAt": "2026-07-27T11:00:00Z",
      "updatedAt": "2026-07-27T11:05:00Z"
    }
  ]
}
```

**Possible HTTP status codes**

- `200 OK` — user payments returned successfully
- `400 Bad Request` — invalid user identifier format
- `404 Not Found` — user does not exist or has no accessible records
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
  "paymentId": "PAY-10001",
  "sourceAccountId": "ACC-10001",
  "destinationAccountId": "ACC-20001",
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
支付生命周期预期遵循如下路径：

`CREATED → VALIDATED → SENT → COMPLETED`

如果处理过程中无法继续，支付也可以转为 `FAILED` 状态。

API 应拒绝不合法或不一致的状态更新。例如，若业务规则不允许，则尝试从 `CREATED` 直接跳转到 `COMPLETED` 的请求应被拒绝。具体规则由服务层定义。

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

