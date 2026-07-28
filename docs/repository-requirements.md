# PaymentServiceImpl 所需的 Repository 方法说明

## 目的

本文档列出 `com.example.paymentprocessing.service.PaymentServiceImpl` 为了实现
`docs/service-contract.md` 中定义的 5 个方法，所需要用到的具体 Repository 方法。
编写本文档的目的是让 Kylian 不需要阅读完整的 Service 层代码，也能直接实现
`PaymentRepository`、`PaymentStatusHistoryRepository` 和 `UserRepository`
这三个接口。

这三个接口都应该继承 Spring Data JPA 的 `JpaRepository`，这样会自动获得
`save(...)`、`findById(...)`、`existsById(...)`、`deleteById(...)` 等方法，
不需要手写。下面只列出还需要额外手动添加的查询方法。

## 1. `UserRepository`

```java
package com.example.paymentprocessing.repository;

import com.example.paymentprocessing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Needed to resolve a destination account number back into a User,
    // both when validating the destination user on createPayment and when
    // mapping Payment.destinationAccountNumber back to
    // PaymentResponse.destinationAccountId.
    Optional<User> findByAccountNumber(String accountNumber);
}
```

**为什么需要这些方法：**

- `findById(Long)`（继承自 `JpaRepository`）：在 `createPayment` 中根据 ID
  加载付款用户和收款用户。
- `existsById(Long)`（继承自 `JpaRepository`）：`getPaymentsByUser` 中可选的
  用户存在性校验。
- `findByAccountNumber(String)`：`Payment.destinationAccountNumber` 存储的是
  纯账号字符串（没有外键约束，详见 `docs/database-design.md`），但
  `CreatePaymentRequest.destinationAccountId` 和
  `PaymentResponse.destinationAccountId` 用的是 `Long` 类型的 ID。Service
  层需要这个方法在"账号字符串"和"用户 ID"这两种表示形式之间做双向转换。

## 2. `PaymentRepository`

```java
package com.example.paymentprocessing.repository;

import com.example.paymentprocessing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Needed by getPaymentsByUser: a payment belongs to a user when the user
    // is either the source account or the destination account.
    List<Payment> findBySourceAccount_IdOrDestinationAccountNumber(
            Long sourceAccountId,
            String destinationAccountNumber
    );
}
```

**为什么需要这些方法：**

- `save(Payment)`（继承）：在 `createPayment` 中保存新支付记录，在
  `updatePaymentStatus` 中保存更新后的支付记录。
- `findById(Long)`（继承）：`getPaymentById`、`getPaymentHistory`、
  `updatePaymentStatus` 都需要用到。
- `findBySourceAccount_IdOrDestinationAccountNumber(Long, String)`：用于
  `getPaymentsByUser`。Service 层会先通过 `UserRepository.findById(...)`
  查出目标用户的 `accountNumber`，然后把该用户自己的 ID（用于匹配付款方）
  和 `accountNumber`（用于匹配收款方）一起传入这一个查询方法。

## 3. `PaymentStatusHistoryRepository`

```java
package com.example.paymentprocessing.repository;

import com.example.paymentprocessing.entity.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, Long> {

    // Needed by getPaymentHistory: the contract requires the result to be
    // sorted by changedAt ascending (docs/service-contract.md section 2.3).
    List<PaymentStatusHistory> findByPayment_IdOrderByChangedAtAsc(Long paymentId);
}
```

**为什么需要这个方法：**

- `save(PaymentStatusHistory)`（继承）：在 `createPayment` 中保存第一条历史
  记录，在 `updatePaymentStatus` 中保存状态变更记录。
- `findByPayment_IdOrderByChangedAtAsc(Long)`：用于 `getPaymentHistory`，
  加载并按时间升序排列某笔支付的全部历史记录。

## 命名说明

Spring Data JPA 会根据方法名中的属性路径自动生成查询语句，所以
`sourceAccount.id` 在方法名里要写成 `SourceAccount_Id`（中间的下划线是
为了避免 `sourceAccount` 本身被误解析成 `source.account`）。Kylian 不需要
手写任何 SQL 或 JPQL，Spring Data 会根据方法签名自动生成查询。

## 这些方法补齐之后

`PaymentServiceImpl` 就可以立即把全部 5 个 `UnsupportedOperationException`
占位实现替换成真正的业务逻辑——补齐这些方法之后，Service 层不再需要任何
额外的依赖变更。
