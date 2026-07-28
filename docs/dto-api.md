| API | Method | Request DTO | Response DTO |
|---|---|---|---|
| `/api/payments` | `POST` | `CreatePaymentRequest` | `PaymentResponse` |
| `/api/payments/{payment_id}` | `GET` | - | `PaymentResponse` |
| `/api/payments/{payment_id}/history` | `GET` | - | `PaymentHistoryResponse[]` |
| `/api/payments/user/{id}` | `GET` | - | `PaymentResponse[]` |
| `/api/payments/{payment_id}/status` | `PUT` | `UpdatePaymentStatusRequest` | `PaymentResponse` |
| Error Response | - | - | `ErrorResponse` |

| DTO | Field | Type | Required | Description | Notes |
|---|---|---|---|---|---|
| `CreatePaymentRequest` | `sourceAccountId` | `Long` | Yes | 付款账户 ID | `BIGINT` |
| `CreatePaymentRequest` | `destinationAccountId` | `Long` | Yes | 收款账户 ID | `BIGINT` |
| `CreatePaymentRequest` | `amount` | `BigDecimal` | Yes | 支付金额 | 应为正数 |
| `CreatePaymentRequest` | `currency` | `String` | Yes | 货币代码 | 例如 `USD` |
| `CreatePaymentRequest` | `paymentPassword` | `String` | Yes | 支付密码 | 仅存在于请求中；不写入数据库；不出现在响应中 |
| `UpdatePaymentStatusRequest` | `status` | `PaymentStatus` | Yes | 支付状态 | `CREATED` / `VALIDATED` / `SENT` / `COMPLETED` / `FAILED` |

| DTO | Field | Type | Description | Notes |
|---|---|---|---|---|
| `PaymentResponse` | `paymentId` | `Long` | 交易 ID | `BIGINT` |
| `PaymentResponse` | `sourceAccountId` | `Long` | 付款账户 ID | `BIGINT` |
| `PaymentResponse` | `destinationAccountId` | `Long` | 收款账户 ID | `BIGINT` |
| `PaymentResponse` | `amount` | `BigDecimal` | 支付金额 | - |
| `PaymentResponse` | `currency` | `String` | 货币代码 | 例如 `USD` |
| `PaymentResponse` | `status` | `PaymentStatus` | 当前支付状态 | 不返回密码或密码哈希 |
| `PaymentResponse` | `createdAt` | `LocalDateTime` | 创建时间 | ISO-8601 |
| `PaymentResponse` | `updatedAt` | `LocalDateTime` | 更新时间 | ISO-8601 |
| `PaymentHistoryResponse` | `historyId` | `Long` | 历史记录 ID | `BIGINT` |
| `PaymentHistoryResponse` | `previousStatus` | `PaymentStatus` | 变更前状态 | 首次创建时可为空 |
| `PaymentHistoryResponse` | `newStatus` | `PaymentStatus` | 变更后状态 | - |
| `PaymentHistoryResponse` | `changedAt` | `LocalDateTime` | 状态变更时间 | 不使用 `updatedAt` |
| `PaymentHistoryResponse` | `notes` | `String` | 状态变更说明 | 可为空 |
| `ErrorResponse` | `errorCode` | `String` | 错误代码 | 例如 `INVALID_STATUS_TRANSITION` |
| `ErrorResponse` | `message` | `String` | 错误信息 | 例如 `Cannot change COMPLETED to CREATED` |
| `ErrorResponse` | `timestamp` | `LocalDateTime` | 错误时间 | ISO-8601 |
| `ErrorResponse` | `path` | `String` | 请求路径 | 例如 `/api/payments/101/status` |

