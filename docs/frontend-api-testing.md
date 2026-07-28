# Payment API Frontend Integration Guide

## 1. Base URL

Backend base URL:

`http://localhost:8080`

API prefix:

`/api/payments`

Full example:

`GET http://localhost:8080/api/payments/101`

Frontend development server currently runs on:

`http://localhost:5173`

CORS is configured to allow frontend-backend communication from the frontend development server.

> The current implementation uses in-memory mock data. Once Service and Repository layers are implemented, the response behavior will remain the same but data will come from the database.

---

## 2. GET Payment Details

### Endpoint

`GET /api/payments/{paymentId}`

### Purpose

Retrieve a single payment detail.

### Path parameter

- `paymentId` (`Long`)

### Available test IDs

- `101`
- `102`
- `103`

### Current sample payments

#### Payment 101

- `sourceAccountId`: `1001`
- `destinationAccountId`: `1002`
- `amount`: `1500.00`
- `currency`: `USD`
- `status`: `COMPLETED`

#### Payment 102

- `sourceAccountId`: `1003`
- `destinationAccountId`: `1001`
- `amount`: `275.50`
- `currency`: `USD`
- `status`: `SENT`

#### Payment 103

- `sourceAccountId`: `2001`
- `destinationAccountId`: `2002`
- `amount`: `999.99`
- `currency`: `EUR`
- `status`: `FAILED`

### Response format

The response is a single JSON object based on `PaymentResponse`:

```json
{
  "paymentId": 101,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "status": "COMPLETED",
  "createdAt": "2026-07-27T10:30:00",
  "updatedAt": "2026-07-27T10:40:00"
}
```

### Response fields

- `paymentId`
- `sourceAccountId`
- `destinationAccountId`
- `amount`
- `currency`
- `status`
- `createdAt`
- `updatedAt`

### Error case

If the payment does not exist:

- HTTP status: `404 Not Found`
- Response body: `ErrorResponse`
- `errorCode`: `PAYMENT_NOT_FOUND`

Example:

```json
{
  "errorCode": "PAYMENT_NOT_FOUND",
  "message": "Payment with id 999 was not found.",
  "timestamp": "2026-07-28T12:00:00",
  "path": "/api/payments/999"
}
```

---

## 3. GET Payment History

### Endpoint

`GET /api/payments/{paymentId}/history`

### Purpose

Retrieve payment status lifecycle history.

### Available test IDs

- `101`
- `102`
- `103`

### Response format

The response is a JSON array of `PaymentHistoryResponse` objects.

Each history record contains:

- `historyId`
- `paymentId`
- `previousStatus`
- `newStatus`
- `changedAt`
- `notes`

### Example response for payment 101

```json
[
  {
    "historyId": 1001,
    "paymentId": 101,
    "previousStatus": null,
    "newStatus": "CREATED",
    "changedAt": "2026-07-27T10:30:00",
    "notes": "Payment created"
  },
  {
    "historyId": 1002,
    "paymentId": 101,
    "previousStatus": "CREATED",
    "newStatus": "VALIDATED",
    "changedAt": "2026-07-27T10:32:00",
    "notes": "Payment request validated"
  },
  {
    "historyId": 1003,
    "paymentId": 101,
    "previousStatus": "VALIDATED",
    "newStatus": "SENT",
    "changedAt": "2026-07-27T10:35:00",
    "notes": "Payment sent for processing"
  },
  {
    "historyId": 1004,
    "paymentId": 101,
    "previousStatus": "SENT",
    "newStatus": "COMPLETED",
    "changedAt": "2026-07-27T10:40:00",
    "notes": "Payment completed successfully"
  }
]
```

### Sorting rule

The current controller sorts history records by `changedAt` in ascending order.

### Error case

If `paymentId` is unknown:

- HTTP status: `404 Not Found`
- Response body: `ErrorResponse`

Example:

```json
{
  "errorCode": "PAYMENT_NOT_FOUND",
  "message": "Payment with id 999 was not found.",
  "timestamp": "2026-07-28T12:00:00",
  "path": "/api/payments/999/history"
}
```

---

## 4. GET Payments By User

### Endpoint

`GET /api/payments/user/{userId}`

### Purpose

Retrieve payments where the user is either:

- `sourceAccountId`
- or `destinationAccountId`

### Available test user

- `1001`

### Expected result for user 1001

Calling:

`GET /api/payments/user/1001`

returns:

- Payment `101`, because `sourceAccountId = 1001`
- Payment `102`, because `destinationAccountId = 1001`

### Example response

```json
[
  {
    "paymentId": 101,
    "sourceAccountId": 1001,
    "destinationAccountId": 1002,
    "amount": 1500.00,
    "currency": "USD",
    "status": "COMPLETED",
    "createdAt": "2026-07-27T10:30:00",
    "updatedAt": "2026-07-27T10:40:00"
  },
  {
    "paymentId": 102,
    "sourceAccountId": 1003,
    "destinationAccountId": 1001,
    "amount": 275.50,
    "currency": "USD",
    "status": "SENT",
    "createdAt": "2026-07-27T11:05:00",
    "updatedAt": "2026-07-27T11:12:00"
  }
]
```

### No-result case

If no matching payment exists:

- HTTP status: `200 OK`
- Response body: empty array

```json
[]
```

### Current behavior note

The current controller does not verify whether the user exists. It only filters the in-memory payment list by `sourceAccountId` or `destinationAccountId`.

---

## 5. POST Create Payment

### Endpoint

`POST /api/payments`

### Purpose

Create a new payment.

### Request body

The request JSON is based on `CreatePaymentRequest`.

Fields:

- `sourceAccountId`
- `destinationAccountId`
- `amount`
- `currency`
- `paymentPassword`

Example:

```json
{
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 500.00,
  "currency": "USD",
  "paymentPassword": "123456"
}
```

### Security behavior in current controller

`paymentPassword`:

- is accepted only from the request body.
- is not stored in the response object.
- is not returned to the frontend.
- should not be displayed in frontend logs.

### Current response

- HTTP status: `201 Created`
- Response body: `PaymentResponse`

Example response:

```json
{
  "paymentId": 104,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 500.00,
  "currency": "USD",
  "status": "CREATED",
  "createdAt": "2026-07-28T12:00:00",
  "updatedAt": "2026-07-28T12:00:00"
}
```

### Current creation behavior

A newly created payment:

- receives a generated `paymentId`
- starts with status `CREATED`
- receives current timestamps for `createdAt` and `updatedAt`
- also gets an initial in-memory history record with `newStatus = CREATED`

### Important current limitation

The current controller accepts the request and creates the payment in temporary memory only. It does not perform password verification, balance verification, or service-level validation.

---

## 6. Current Limitations

### Current implementation details

The current implementation:

- does not use MySQL.
- does not use Repository.
- does not use Service layer.
- uses `ConcurrentHashMap` as temporary storage.

### Current controller behavior

- Data resets after application restart.
- Password verification is not implemented.
- Balance checking is not implemented.
- Status transition validation is not implemented.
- There is currently no payment status update endpoint implemented in `PaymentController`.

### Temporary architecture

Current flow:

`Controller -> In-memory Map`

Future internal flow:

`Controller -> Service -> Repository -> Database`

The current implementation uses in-memory mock data. Once Service and Repository layers are implemented, the response behavior will remain the same but data will come from the database.

---

## 7. Frontend Testing Checklist

- [ ] Frontend can load payment detail.
- [ ] Frontend can display payment history timeline.
- [ ] Frontend can search payments by user.
- [ ] Frontend can create payment.
- [ ] Frontend handles 404 error response.
- [ ] Frontend does not display `paymentPassword`.

---

## 8. Quick API Summary

Currently available APIs:

- `GET /api/payments/{paymentId}`
- `GET /api/payments/{paymentId}/history`
- `GET /api/payments/user/{userId}`
- `POST /api/payments`

Current mock payment IDs:

- `101`
- `102`
- `103`

Useful current mock user IDs:

- `1001`
- `1002`
- `1003`
- `2001`
- `2002`

Available payment statuses in current responses:

- `CREATED`
- `VALIDATED`
- `SENT`
- `COMPLETED`
- `FAILED`

---

# Payment API Frontend Integration Guide（中文版）

## 1. 基础地址

后端基础地址：

`http://localhost:8080`

API 前缀：

`/api/payments`

完整示例：

`GET http://localhost:8080/api/payments/101`

当前前端开发服务器运行地址：

`http://localhost:5173`

当前已配置 CORS，允许前端开发服务器与后端进行联调。

> 当前实现使用的是内存中的 mock 数据。等 Service 层和 Repository 层实现后，接口的响应行为将保持不变，但数据来源将改为数据库。

---

## 2. 获取支付详情（GET Payment Details）

### 接口地址

`GET /api/payments/{paymentId}`

### 接口用途

获取单条支付详情。

### 路径参数

- `paymentId`（`Long`）

### 可用于测试的支付 ID

- `101`
- `102`
- `103`

### 当前内置示例支付数据

#### Payment 101

- `sourceAccountId`: `1001`
- `destinationAccountId`: `1002`
- `amount`: `1500.00`
- `currency`: `USD`
- `status`: `COMPLETED`

#### Payment 102

- `sourceAccountId`: `1003`
- `destinationAccountId`: `1001`
- `amount`: `275.50`
- `currency`: `USD`
- `status`: `SENT`

#### Payment 103

- `sourceAccountId`: `2001`
- `destinationAccountId`: `2002`
- `amount`: `999.99`
- `currency`: `EUR`
- `status`: `FAILED`

### 响应格式

响应体是一个基于 `PaymentResponse` 的 JSON 对象：

```json
{
  "paymentId": 101,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 1500.00,
  "currency": "USD",
  "status": "COMPLETED",
  "createdAt": "2026-07-27T10:30:00",
  "updatedAt": "2026-07-27T10:40:00"
}
```

### 响应字段

- `paymentId`
- `sourceAccountId`
- `destinationAccountId`
- `amount`
- `currency`
- `status`
- `createdAt`
- `updatedAt`

### 错误情况

如果支付记录不存在：

- HTTP 状态码：`404 Not Found`
- 响应体：`ErrorResponse`
- `errorCode`：`PAYMENT_NOT_FOUND`

示例：

```json
{
  "errorCode": "PAYMENT_NOT_FOUND",
  "message": "Payment with id 999 was not found.",
  "timestamp": "2026-07-28T12:00:00",
  "path": "/api/payments/999"
}
```

---

## 3. 获取支付历史（GET Payment History）

### 接口地址

`GET /api/payments/{paymentId}/history`

### 接口用途

获取支付状态生命周期历史记录。

### 可用于测试的支付 ID

- `101`
- `102`
- `103`

### 响应格式

响应体是一个 `PaymentHistoryResponse` JSON 数组。

每条历史记录包含以下字段：

- `historyId`
- `paymentId`
- `previousStatus`
- `newStatus`
- `changedAt`
- `notes`

### Payment 101 的示例响应

```json
[
  {
    "historyId": 1001,
    "paymentId": 101,
    "previousStatus": null,
    "newStatus": "CREATED",
    "changedAt": "2026-07-27T10:30:00",
    "notes": "Payment created"
  },
  {
    "historyId": 1002,
    "paymentId": 101,
    "previousStatus": "CREATED",
    "newStatus": "VALIDATED",
    "changedAt": "2026-07-27T10:32:00",
    "notes": "Payment request validated"
  },
  {
    "historyId": 1003,
    "paymentId": 101,
    "previousStatus": "VALIDATED",
    "newStatus": "SENT",
    "changedAt": "2026-07-27T10:35:00",
    "notes": "Payment sent for processing"
  },
  {
    "historyId": 1004,
    "paymentId": 101,
    "previousStatus": "SENT",
    "newStatus": "COMPLETED",
    "changedAt": "2026-07-27T10:40:00",
    "notes": "Payment completed successfully"
  }
]
```

### 排序规则

当前控制器会按照 `changedAt` 升序返回历史记录。

### 错误情况

如果 `paymentId` 不存在：

- HTTP 状态码：`404 Not Found`
- 响应体：`ErrorResponse`

示例：

```json
{
  "errorCode": "PAYMENT_NOT_FOUND",
  "message": "Payment with id 999 was not found.",
  "timestamp": "2026-07-28T12:00:00",
  "path": "/api/payments/999/history"
}
```

---

## 4. 按用户查询支付（GET Payments By User）

### 接口地址

`GET /api/payments/user/{userId}`

### 接口用途

查询该用户作为以下任一角色参与的支付：

- `sourceAccountId`
- `destinationAccountId`

### 可用于测试的用户 ID

- `1001`

### user 1001 的预期结果

调用：

`GET /api/payments/user/1001`

会返回：

- Payment `101`，因为 `sourceAccountId = 1001`
- Payment `102`，因为 `destinationAccountId = 1001`

### 示例响应

```json
[
  {
    "paymentId": 101,
    "sourceAccountId": 1001,
    "destinationAccountId": 1002,
    "amount": 1500.00,
    "currency": "USD",
    "status": "COMPLETED",
    "createdAt": "2026-07-27T10:30:00",
    "updatedAt": "2026-07-27T10:40:00"
  },
  {
    "paymentId": 102,
    "sourceAccountId": 1003,
    "destinationAccountId": 1001,
    "amount": 275.50,
    "currency": "USD",
    "status": "SENT",
    "createdAt": "2026-07-27T11:05:00",
    "updatedAt": "2026-07-27T11:12:00"
  }
]
```

### 无结果情况

如果没有匹配的支付记录：

- HTTP 状态码：`200 OK`
- 响应体：空数组

```json
[]
```

### 当前行为说明

当前控制器不会校验用户是否真实存在。它只是在内存中的支付列表里，按 `sourceAccountId` 或 `destinationAccountId` 做过滤。

---

## 5. 创建支付（POST Create Payment）

### 接口地址

`POST /api/payments`

### 接口用途

创建一条新的支付记录。

### 请求体

请求 JSON 基于 `CreatePaymentRequest`。

字段包括：

- `sourceAccountId`
- `destinationAccountId`
- `amount`
- `currency`
- `paymentPassword`

示例：

```json
{
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 500.00,
  "currency": "USD",
  "paymentPassword": "123456"
}
```

### 当前控制器中的安全行为

`paymentPassword`：

- 只会从请求体中接收。
- 不会存入响应对象。
- 不会返回给前端。
- 前端也不应在日志中展示该字段。

### 当前响应

- HTTP 状态码：`201 Created`
- 响应体：`PaymentResponse`

示例响应：

```json
{
  "paymentId": 104,
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 500.00,
  "currency": "USD",
  "status": "CREATED",
  "createdAt": "2026-07-28T12:00:00",
  "updatedAt": "2026-07-28T12:00:00"
}
```

### 当前创建行为说明

新创建的支付会：

- 自动生成 `paymentId`
- 初始状态固定为 `CREATED`
- 将当前时间写入 `createdAt` 和 `updatedAt`
- 同时创建一条初始的内存历史记录，其中 `newStatus = CREATED`

### 当前实现的重要限制

当前控制器只会把请求保存到临时内存中，不会执行密码校验、余额校验或 Service 层业务校验。

---

## 6. 当前限制

### 当前实现细节

当前实现：

- 不使用 MySQL。
- 不使用 Repository。
- 不使用 Service 层。
- 使用 `ConcurrentHashMap` 作为临时存储。

### 当前控制器行为

- 应用重启后数据会重置。
- 尚未实现支付密码校验。
- 尚未实现余额校验。
- 尚未实现状态流转校验。
- `PaymentController` 目前还没有实现支付状态更新接口。

### 临时架构

当前流程：

`Controller -> In-memory Map`

未来内部流程：

`Controller -> Service -> Repository -> Database`

当前实现使用的是内存中的 mock 数据。等 Service 层和 Repository 层实现后，接口的响应行为将保持不变，但数据来源将改为数据库。

---

## 7. 前端测试检查清单

- [ ] 前端可以加载支付详情。
- [ ] 前端可以展示支付历史时间线。
- [ ] 前端可以按用户查询支付记录。
- [ ] 前端可以创建支付。
- [ ] 前端可以正确处理 404 错误响应。
- [ ] 前端不会展示 `paymentPassword`。

---

## 8. API 快速摘要

当前已可用接口：

- `GET /api/payments/{paymentId}`
- `GET /api/payments/{paymentId}/history`
- `GET /api/payments/user/{userId}`
- `POST /api/payments`

当前可用于测试的 mock 支付 ID：

- `101`
- `102`
- `103`

当前可用于测试的 mock 用户 ID：

- `1001`
- `1002`
- `1003`
- `2001`
- `2002`

当前响应中可能出现的支付状态：

- `CREATED`
- `VALIDATED`
- `SENT`
- `COMPLETED`
- `FAILED`

