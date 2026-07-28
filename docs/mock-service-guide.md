# Mock Service 使用指南（给 Leon）

## 背景

`docs/service-contract.md` 中定义的 `PaymentService` 契约现在**已经冻结**
（方法签名、DTO 字段、异常类型、状态流转规则都不能再改，除非团队重新讨论）。

为了让 Controller 层（Leon）不用等 MySQL 数据库配置好、也不用依赖 Richard 写的
真实 Service 实现（`PaymentServiceImpl`），现在新增了一个纯内存的
Mock 实现：`MockPaymentServiceImpl`。它实现了和真实实现完全一样的
`PaymentService` 接口，Controller 代码完全不需要修改就能切换使用。

## 如何启用 Mock 模式

`MockPaymentServiceImpl` 只在 Spring 的 `mock` profile 被激活时才会生效；
真实的 `PaymentServiceImpl` 则只在 `mock` profile **未**激活时生效
（两者用 `@Profile` 互斥，不会同时注册导致 Bean 冲突）.

启用方式（三选一）：

**方式一：运行参数**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mock
```

**方式二：JVM 参数**

```bash
java -Dspring.profiles.active=mock -jar target/payment-processing-system.jar
```

**方式三：环境变量**

```bash
set SPRING_PROFILES_ACTIVE=mock
```

启用后不需要任何数据库连接，所有数据都保存在内存中，重启应用后会重置为
初始种子数据（见下方）。

## 内置的种子数据

启动时会自动创建 2 条支付记录，可以直接用来测试 GET 接口：

| paymentId | sourceAccountId | destinationAccountId | amount | status |
|---|---|---|---|---|
| 101 | 1001 | 1002 | 1500.00 | `COMPLETED` |
| 102 | 1003 | 1001 | 275.50 | `SENT` |

## 用来触发各种异常的测试数据

| 想触发的异常 | 怎么构造请求 |
|---|---|
| `UserNotFoundException` | `sourceAccountId` 或 `destinationAccountId` 不是 `1001`、`1002`、`1003`、`99` 中的任何一个 |
| `InvalidAccountStatusException` | `sourceAccountId` 或 `destinationAccountId` 使用保留的"未激活"用户 ID `99` |
| `InvalidPaymentAmountException` | `amount` 小于等于 `0` |
| `InvalidPaymentPasswordException` | `paymentPassword` 不等于固定的 mock 密码 `888888` |
| `InsufficientBalanceException` | `amount` 大于固定的 mock 余额上限 `100000.00` |
| `PaymentNotFoundException` | `getPaymentById` / `getPaymentHistory` / `updatePaymentStatus` 使用一个不存在的 `paymentId`（种子数据之外的 ID） |
| `InvalidPaymentStatusException` | `updatePaymentStatus` 传入一个不允许的状态跳转，例如把 `CREATED` 直接改成 `COMPLETED` |

合法的支付密码是固定值 `888888`，账号 `1001`、`1002`、`1003` 视为已激活账户，
账号 `99` 视为未激活账户，用来测试异常分支。

## 状态流转规则（未变化）

和 `docs/service-contract.md` 完全一致：

- `CREATED -> VALIDATED`
- `CREATED -> FAILED`
- `VALIDATED -> SENT`
- `VALIDATED -> FAILED`
- `SENT -> COMPLETED`
- `SENT -> FAILED`

`COMPLETED` 和 `FAILED` 是终态，不能再流转。

## 使用示例

创建一笔成功的支付：

```http
POST /api/payments
```

```json
{
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 500.00,
  "currency": "USD",
  "paymentPassword": "888888"
}
```

触发 `InvalidPaymentPasswordException`：

```http
POST /api/payments
```

```json
{
  "sourceAccountId": 1001,
  "destinationAccountId": 1002,
  "amount": 500.00,
  "currency": "USD",
  "paymentPassword": "wrong-password"
}
```

## 注意事项

- Mock 实现是纯内存存储，重启应用数据会重置，仅用于 Controller 层开发和联调，
  不能用于验证数据库层面的行为（比如唯一约束、外键、事务回滚等），这些仍然需要
  用真实实现 + MySQL 来验证。
- 一旦 Kylian 的数据库/Repository 联调完成，只需要**不带** `mock` profile
  启动应用，就会自动切回真实的 `PaymentServiceImpl`，Controller 代码不需要
  做任何改动。
- 如果后续契约（`docs/service-contract.md`）确实需要修改，请先在团队内讨论，
  并同步更新 `PaymentService` 接口、`PaymentServiceImpl`、`MockPaymentServiceImpl`
  三处，避免两个实现的行为出现分歧。
