# Payment Service Contract

## 1. Service Responsibility

This document defines the fixed contract between the controller layer and the future payment service layer so both developers can work in parallel.

### Service layer responsibilities

The Service layer is responsible for:

- Business logic
- Validation
- Transaction management
- Entity and DTO conversion
- Payment lifecycle management
- Payment password verification
- Payment status transition rules

### Controller layer responsibilities

The Controller layer is responsible only for:

- Receiving HTTP requests
- Receiving DTO objects
- Calling Service methods
- Returning HTTP responses

### Controller layer restrictions

The Controller must **not**:

- Access Repository directly
- Query database directly
- Verify payment password
- Create Entity objects
- Handle transaction logic

This matches the current architecture document and the TODO notes in `PaymentController`, which indicate that controller-side temporary in-memory logic will later be replaced by `PaymentService` calls.

## 2. PaymentService Interface Contract

### Package

`com.example.paymentprocessing.service`

### Interface

`PaymentService`

### Fixed method signatures

Do not write implementation code in the controller. The future service interface contract is:

```java
package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse getPaymentById(Long paymentId);

    List<PaymentHistoryResponse> getPaymentHistory(Long paymentId);

    List<PaymentResponse> getPaymentsByUser(Long userId);

    PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request);
}
```

Method names, parameters, and return types are fixed by this document and should not be changed without team discussion.

### 2.1 Create Payment

**Method**

```java
PaymentResponse createPayment(CreatePaymentRequest request);
```

**Endpoint**

`POST /api/payments`

**Input**

`CreatePaymentRequest`

Current request DTO fields:

- `sourceAccountId`
- `destinationAccountId`
- `amount`
- `currency`
- `paymentPassword`

**Service responsibilities**

The Service should:

- Validate payment request.
- Verify source user exists.
- Verify destination user exists.
- Verify payment password.
- Compare `request.paymentPassword()` with `User.paymentPasswordHash`.
- Check account status.
- Check balance if required.
- Create `Payment` record.
- Create initial `PaymentStatusHistory` record.
- Return `PaymentResponse`.

**Controller responsibility**

The Controller should only call this method and translate the result into the HTTP response.

**Possible exceptions**

- `UserNotFoundException`
- `InvalidPaymentPasswordException`
- `InsufficientBalanceException`
- `InvalidPaymentAmountException`
- `InvalidAccountStatusException`

### 2.2 Get Payment By ID

**Method**

```java
PaymentResponse getPaymentById(Long paymentId);
```

**Endpoint**

`GET /api/payments/{paymentId}`

**Service responsibilities**

The Service should:

- Find payment by ID.
- Convert Entity to `PaymentResponse`.

**Possible exceptions**

- `PaymentNotFoundException`

### 2.3 Get Payment History

**Method**

```java
List<PaymentHistoryResponse> getPaymentHistory(Long paymentId);
```

**Endpoint**

`GET /api/payments/{paymentId}/history`

**Service responsibilities**

The Service should:

- Verify payment exists.
- Retrieve payment status history.
- Sort history by `changedAt` ascending.
- Convert Entity objects to DTO.

**Possible exceptions**

- `PaymentNotFoundException`

### 2.4 Get Payments By User

**Method**

```java
List<PaymentResponse> getPaymentsByUser(Long userId);
```

**Endpoint**

`GET /api/payments/user/{userId}`

**Business rule**

A payment belongs to a user when:

- `sourceAccountId = userId`
- **or**
- `destinationAccountId = userId`

**Return requirements**

Return:

- All sent payments.
- All received payments.

Important:

- If user exists but has no payments, return empty list.
- Do **not** throw exception for an existing user with zero payments.

**Possible exceptions**

- `UserNotFoundException` (if user verification is implemented)

### 2.5 Update Payment Status

**Method**

```java
PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request);
```

**Endpoint**

`PUT /api/payments/{paymentId}/status`

**Input**

`UpdatePaymentStatusRequest`

Current request DTO fields:

- `status`

**Service responsibilities**

The Service should:

- Find payment.
- Validate status transition.
- Update payment status.
- Create `PaymentStatusHistory` record.
- Return updated `PaymentResponse`.

**Status transition rules**

Allowed transitions:

- `CREATED -> VALIDATED`
- `CREATED -> FAILED`
- `VALIDATED -> SENT`
- `VALIDATED -> FAILED`
- `SENT -> COMPLETED`
- `SENT -> FAILED`

Terminal states:

- `COMPLETED`
- `FAILED`

No transition is allowed from terminal states.

**Possible exceptions**

- `PaymentNotFoundException`
- `InvalidPaymentStatusException`

## 3. Transaction Rules

The following methods require transactional behavior:

- `createPayment()`
- `updatePaymentStatus()`

These methods should be implemented with `@Transactional`.

### Reason

These operations modify multiple database records.

### Example: create payment

1. Save `Payment`
2. Save `PaymentStatusHistory`

Both operations must succeed together.

### Example: update payment status

1. Update payment status
2. Insert new history record

Both operations must succeed together.

## 4. DTO Conversion Rules

The Service returns DTOs.

The Controller never receives Entity objects.

### Conversion examples

`Payment` Entity

↓

`PaymentResponse`

`PaymentStatusHistory` Entity

↓

`PaymentHistoryResponse`

This keeps entity design internal to the service and persistence layers.

## 5. Security Rules

`paymentPassword`:

- Can only exist in `CreatePaymentRequest`.
- Must never be stored.
- Must never be logged.
- Must never appear in any Response DTO.

Stored value:

- `User.paymentPasswordHash`

Verification happens only in the Service layer.

The Controller must pass the request DTO through to the Service without inspecting or validating the payment password itself.

## 6. Implementation Notes For Service Developer

The Service implementation should be:

### Package

`com.example.paymentprocessing.service`

### Implementation class

`PaymentServiceImpl`

### Annotation

`@Service`

### Dependency injection rule

Use constructor injection.

### Team rule

Do not change method names or parameters without team discussion.

## 7. Notes Based on Current Codebase

- The current `PaymentController` contains temporary in-memory maps and sample data only for frontend-backend integration.
- When the service layer is implemented, controller map access should be replaced by `PaymentService` calls.
- The current controller already exposes these service-aligned operations for create payment, get payment by ID, get payment history, and get payments by user.
- The API design also defines an update-status endpoint, so the service contract includes `updatePaymentStatus(...)` even though the current controller has not implemented that endpoint yet.

## 8. Source References Used For This Contract

This contract was derived from:

- `src/main/java/com/example/paymentprocessing/controller/PaymentController.java`
- `docs/api-design.md`
- `docs/architecture.md`
- `src/main/java/com/example/paymentprocessing/dto/request/CreatePaymentRequest.java`
- `src/main/java/com/example/paymentprocessing/dto/request/UpdatePaymentStatusRequest.java`
- `src/main/java/com/example/paymentprocessing/dto/response/PaymentResponse.java`
- `src/main/java/com/example/paymentprocessing/dto/response/PaymentHistoryResponse.java`
- `src/main/java/com/example/paymentprocessing/dto/response/ErrorResponse.java`
- `src/main/java/com/example/paymentprocessing/enums/PaymentStatus.java`

---

# Payment Service Contract（中文版）

## 1. 服务层职责

本文档定义了控制器层与未来支付服务层之间的固定契约，以便两位开发者可以并行工作。

### 服务层负责

Service 层负责：

- 业务逻辑
- 校验
- 事务管理
- Entity 与 DTO 转换
- 支付生命周期管理
- 支付密码校验
- 支付状态流转规则校验

### 控制器层负责

Controller 层只负责：

- 接收 HTTP 请求
- 接收 DTO 对象
- 调用 Service 方法
- 返回 HTTP 响应

### 控制器层禁止事项

Controller **不能**：

- 直接访问 Repository
- 直接查询数据库
- 校验支付密码
- 创建 Entity 对象
- 处理事务逻辑

以上内容与当前架构文档以及 `PaymentController` 中的 TODO 说明保持一致。当前控制器中的临时内存逻辑，后续应替换为对 `PaymentService` 的调用。

## 2. PaymentService 接口契约

### 包路径

`com.example.paymentprocessing.service`

### 接口名

`PaymentService`

### 固定方法签名

不要在 Controller 中编写实现代码。未来服务接口契约如下：

```java
package com.example.paymentprocessing.service;

import com.example.paymentprocessing.dto.request.CreatePaymentRequest;
import com.example.paymentprocessing.dto.request.UpdatePaymentStatusRequest;
import com.example.paymentprocessing.dto.response.PaymentHistoryResponse;
import com.example.paymentprocessing.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse getPaymentById(Long paymentId);

    List<PaymentHistoryResponse> getPaymentHistory(Long paymentId);

    List<PaymentResponse> getPaymentsByUser(Long userId);

    PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request);
}
```

本文档中定义的方法名、参数和返回类型均为固定契约，未经团队讨论不得修改。

### 2.1 创建支付

**方法**

```java
PaymentResponse createPayment(CreatePaymentRequest request);
```

**对应接口**

`POST /api/payments`

**输入**

`CreatePaymentRequest`

当前请求 DTO 字段：

- `sourceAccountId`
- `destinationAccountId`
- `amount`
- `currency`
- `paymentPassword`

**服务层职责**

Service 应当：

- 校验支付请求。
- 校验付款方用户是否存在。
- 校验收款方用户是否存在。
- 校验支付密码。
- 将 `request.paymentPassword()` 与 `User.paymentPasswordHash` 进行比较。
- 校验账户状态。
- 在需要时校验余额。
- 创建 `Payment` 记录。
- 创建初始 `PaymentStatusHistory` 记录。
- 返回 `PaymentResponse`。

**控制器职责**

Controller 只应调用该方法，并将结果转换为 HTTP 响应。

**可能抛出的异常**

- `UserNotFoundException`
- `InvalidPaymentPasswordException`
- `InsufficientBalanceException`
- `InvalidPaymentAmountException`
- `InvalidAccountStatusException`

### 2.2 根据支付 ID 查询支付

**方法**

```java
PaymentResponse getPaymentById(Long paymentId);
```

**对应接口**

`GET /api/payments/{paymentId}`

**服务层职责**

Service 应当：

- 根据 ID 查找支付记录。
- 将 Entity 转换为 `PaymentResponse`。

**可能抛出的异常**

- `PaymentNotFoundException`

### 2.3 查询支付历史

**方法**

```java
List<PaymentHistoryResponse> getPaymentHistory(Long paymentId);
```

**对应接口**

`GET /api/payments/{paymentId}/history`

**服务层职责**

Service 应当：

- 校验支付是否存在。
- 获取支付状态历史记录。
- 按 `changedAt` 升序排序。
- 将 Entity 对象转换为 DTO。

**可能抛出的异常**

- `PaymentNotFoundException`

### 2.4 根据用户查询支付列表

**方法**

```java
List<PaymentResponse> getPaymentsByUser(Long userId);
```

**对应接口**

`GET /api/payments/user/{userId}`

**业务规则**

在以下任一条件满足时，该支付属于该用户：

- `sourceAccountId = userId`
- **或**
- `destinationAccountId = userId`

**返回要求**

返回：

- 该用户发出的所有支付。
- 该用户收到的所有支付。

重要说明：

- 如果用户存在但没有任何支付记录，返回空列表。
- 对于“存在但无支付记录”的用户，**不要**抛出异常。

**可能抛出的异常**

- `UserNotFoundException`（如果实现了用户存在性校验）

### 2.5 更新支付状态

**方法**

```java
PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request);
```

**对应接口**

`PUT /api/payments/{paymentId}/status`

**输入**

`UpdatePaymentStatusRequest`

当前请求 DTO 字段：

- `status`

**服务层职责**

Service 应当：

- 查找支付记录。
- 校验状态流转是否合法。
- 更新支付状态。
- 创建 `PaymentStatusHistory` 记录。
- 返回更新后的 `PaymentResponse`。

**状态流转规则**

允许的流转：

- `CREATED -> VALIDATED`
- `CREATED -> FAILED`
- `VALIDATED -> SENT`
- `VALIDATED -> FAILED`
- `SENT -> COMPLETED`
- `SENT -> FAILED`

终态：

- `COMPLETED`
- `FAILED`

一旦进入终态，不允许再发生任何状态流转。

**可能抛出的异常**

- `PaymentNotFoundException`
- `InvalidPaymentStatusException`

## 3. 事务规则

以下方法需要事务支持：

- `createPayment()`
- `updatePaymentStatus()`

这些方法应使用 `@Transactional` 实现。

### 原因

这些操作会同时修改多条数据库记录。

### 示例：创建支付

1. 保存 `Payment`
2. 保存 `PaymentStatusHistory`

这两个操作必须同时成功。

### 示例：更新支付状态

1. 更新支付状态
2. 插入一条新的历史记录

这两个操作必须同时成功。

## 4. DTO 转换规则

Service 返回 DTO。

Controller 不应接收 Entity 对象。

### 转换示例

`Payment` Entity

↓

`PaymentResponse`

`PaymentStatusHistory` Entity

↓

`PaymentHistoryResponse`

这样可以将实体设计限制在服务层和持久化层内部，避免泄漏到控制器层。

## 5. 安全规则

`paymentPassword`：

- 只能存在于 `CreatePaymentRequest` 中。
- 绝不能被持久化存储。
- 绝不能被写入日志。
- 绝不能出现在任何 Response DTO 中。

数据库中存储的值：

- `User.paymentPasswordHash`

密码校验只能发生在 Service 层。

Controller 必须将请求 DTO 直接传递给 Service，而不能自行检查或校验支付密码。

## 6. 给服务开发者的实现说明

服务实现类应为：

### 包路径

`com.example.paymentprocessing.service`

### 实现类名

`PaymentServiceImpl`

### 注解

`@Service`

### 依赖注入规则

使用构造器注入。

### 团队约定

未经团队讨论，不要修改方法名或参数。

## 7. 基于当前代码库的说明

- 当前 `PaymentController` 仅包含用于前后端联调的临时内存 Map 和示例数据。
- 当服务层实现完成后，控制器中的 Map 访问应替换为对 `PaymentService` 的调用。
- 当前控制器已经暴露了以下与服务契约一致的操作：创建支付、按 ID 查询支付、查询支付历史、按用户查询支付。
- API 设计文档还定义了“更新支付状态”接口，因此本契约中包含 `updatePaymentStatus(...)`，即使当前控制器尚未实现该端点。

## 8. 本契约参考的源码与文档

本契约基于以下内容整理：

- `src/main/java/com/example/paymentprocessing/controller/PaymentController.java`
- `docs/api-design.md`
- `docs/architecture.md`
- `src/main/java/com/example/paymentprocessing/dto/request/CreatePaymentRequest.java`
- `src/main/java/com/example/paymentprocessing/dto/request/UpdatePaymentStatusRequest.java`
- `src/main/java/com/example/paymentprocessing/dto/response/PaymentResponse.java`
- `src/main/java/com/example/paymentprocessing/dto/response/PaymentHistoryResponse.java`
- `src/main/java/com/example/paymentprocessing/dto/response/ErrorResponse.java`
- `src/main/java/com/example/paymentprocessing/enums/PaymentStatus.java`

