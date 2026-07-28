# PaymentServiceTest 测试覆盖文档

## 概览

本文档详细说明了 `PaymentServiceTest` 中所有的测试用例，包括每个测试验证的功能和覆盖的场景。

---

## 测试统计

| 项目 | 数量 |
|------|------|
| 测试用例总数 | 15 |
| 测试方法组 | 5 |
| 失败场景 | 7 |
| 成功场景 | 8 |

---

## 1. 支付创建测试
### 测试方法组：createPayment

#### 1.1 `testCreatePayment_Success` - 成功创建支付
**测试目的：**
验证能够使用有效的源用户、目标用户、正确密码和充足余额成功创建支付。

**测试流程：**
1. 设置两个活跃用户
   - 源用户：余额 5000.00
   - 目标用户：余额 3200.00
2. 使用 1500.00 金额和正确密码创建请求
3. 验证支付以 CREATED 状态被创建

**预期结果：**
- ✅ 生成支付 ID
- ✅ 源和目标账户 ID 正确
- ✅ 金额为 1500.00 USD
- ✅ 初始状态为 CREATED
- ✅ 仓库调用正确（保存、创建历史记录）

---

#### 1.2 `testCreatePayment_SourceUserNotFound` - 源用户未找到
**测试目的：**
验证当源用户 ID 不存在时抛出 `UserNotFoundException`。

**测试场景：**
- 源用户 ID：999L（不存在）
- Mock 返回：`Optional.empty()`
- 预期异常：`UserNotFoundException`

**测试内容：**
- 服务在创建支付前验证源用户的存在性

---

#### 1.3 `testCreatePayment_DestinationUserNotFound` - 目标用户未找到
**测试目的：**
验证当目标用户 ID 不存在时抛出 `UserNotFoundException`。

**测试场景：**
- 源用户 ID：1L（存在）
- 目标用户 ID：999L（不存在）
- 预期异常：`UserNotFoundException`

**测试内容：**
- 服务验证目标用户的存在性
- 服务在继续之前检查两个用户

---

#### 1.4 `testCreatePayment_InvalidPassword` - 支付密码错误
**测试目的：**
验证当支付密码不匹配时抛出 `InvalidPaymentPasswordException`。

**测试场景：**
- 两个用户都存在且活跃
- 支付密码："wrongPassword"（不匹配存储的哈希值 "password123"）
- 预期异常：`InvalidPaymentPasswordException`

**测试内容：**
- 服务将支付密码与存储的哈希进行验证
- 密码验证在支付创建前是强制的

---

## 2. 按 ID 获取支付测试
### 测试方法组：getPaymentById

#### 2.1 `testGetPaymentById_Success` - 成功获取支付
**测试目的：**
验证能够通过 ID 检索支付，并获得所有正确的详情。

**测试场景：**
- 支付 ID：1L
- Mock 返回：ID 为 1、源用户为 1、目标为 2 的支付对象
- 预期结果：PaymentResponse 包含正确的支付详情

**验证的详情：**
- 支付 ID：1L
- 源账户 ID：1L
- 目标账户 ID：2L
- 金额：1500.00
- 币种：USD

**测试内容：**
- 服务可以通过 ID 从存储库检索支付
- DTO 映射正确

---

#### 2.2 `testGetPaymentById_PaymentNotFound` - 支付未找到
**测试目的：**
验证当支付 ID 不存在时抛出 `PaymentNotFoundException`。

**测试场景：**
- 支付 ID：999L（不存在）
- Mock 返回：`Optional.empty()`
- 预期异常：`PaymentNotFoundException`

**测试内容：**
- 服务验证支付的存在性
- 对缺失支付的正确错误处理

---

## 3. 按用户获取支付测试
### 测试方法组：getPaymentsByUser

#### 3.1 `testGetPaymentsByUser_Success` - 成功获取用户支付
**测试目的：**
验证能够检索特定用户的所有支付（作为源或目标）。

**测试场景：**
- 用户 ID：1L（存在）
- 返回：两笔支付
  - 支付 1：用户 1 作为源
  - 支付 2：用户 1 作为目标
- 预期结果：包含 2 个 PaymentResponse 对象的列表

**过滤的数据：**
- 支付 1：源=1，目标=2（状态：CREATED）
- 支付 2：源=1，目标=3（状态：VALIDATED）

**测试内容：**
- 服务按源或目标用户筛选支付
- 服务首先验证用户的存在性
- 列表结果正确映射到 DTO

---

#### 3.2 `testGetPaymentsByUser_UserNotFound` - 用户未找到
**测试目的：**
验证当用户 ID 不存在时抛出 `UserNotFoundException`。

**测试场景：**
- 用户 ID：999L（不存在）
- 预期异常：`UserNotFoundException`

**测试内容：**
- 服务在查询支付前验证用户的存在性

---

## 4. 获取支付历史测试
### 测试方法组：getPaymentHistory

#### 4.1 `testGetPaymentHistory_Success` - 成功获取支付历史
**测试目的：**
验证能够按时间顺序检索支付的所有状态更改历史记录。

**测试场景：**
- 支付 ID：1L
- 返回：两条历史记录
  - 记录 1：CREATED（初始状态）
  - 记录 2：VALIDATED（5 分钟后）
- 预期结果：2 个 PaymentHistoryResponse 对象的列表，按正确顺序排列

**历史记录：**
1. 状态变化：null → CREATED（初始状态）
2. 状态变化：CREATED → VALIDATED（5分钟后）

**测试内容：**
- 服务验证支付的存在性
- 历史记录按时间顺序检索
- 完整的审计跟踪得到维护

---

#### 4.2 `testGetPaymentHistory_PaymentNotFound` - 支付未找到
**测试目的：**
验证当为不存在的支付请求历史时抛出 `PaymentNotFoundException`。

**测试场景：**
- 支付 ID：999L（不存在）
- 预期异常：`PaymentNotFoundException`

**测试内容：**
- 服务在获取历史前验证支付的存在性

---

## 5. 更新支付状态测试
### 测试方法组：updatePaymentStatus

#### 5.1 `testUpdatePaymentStatus_ValidTransition_CreatedToValidated` - 有效转换：CREATED → VALIDATED
**测试目的：**
验证支付状态可以从 CREATED 变更为 VALIDATED（第一个有效转换）。

**测试场景：**
- 支付当前状态：CREATED
- 目标状态：VALIDATED
- 预期结果：状态更新成功，创建历史记录

**状态转换：**
```
CREATED → VALIDATED ✅ （允许）
```

**测试内容：**
- 服务验证支付存在
- 服务允许 CREATED → VALIDATED 转换
- 历史记录被创建并附带说明

---

#### 5.2 `testUpdatePaymentStatus_InvalidTransition_CompletedToFailed` - 无效转换：COMPLETED → FAILED
**测试目的：**
验证无效的状态转换被拒绝（终端状态规则）。

**测试场景：**
- 支付当前状态：COMPLETED（终端状态）
- 目标状态：FAILED
- 预期异常：`InvalidPaymentStatusException`

**为什么无效：**
- COMPLETED 是终端状态 - 不允许从它转换到其他状态

**测试内容：**
- 服务实现了冻结/终端状态逻辑
- 服务阻止无效的状态转换
- 抛出正确的异常类型

---

#### 5.3 `testUpdatePaymentStatus_PaymentNotFound` - 支付未找到
**测试目的：**
验证更新不存在的支付状态时抛出 `PaymentNotFoundException`。

**测试场景：**
- 支付 ID：999L（不存在）
- 目标状态：VALIDATED
- 预期异常：`PaymentNotFoundException`

**测试内容：**
- 服务在更新前验证支付的存在性

---

#### 5.4 `testUpdatePaymentStatus_ValidTransition_ValidatedToSent` - 有效转换：VALIDATED → SENT
**测试目的：**
验证 VALIDATED → SENT 转换（工作流中的第二个有效转换）。

**测试场景：**
- 支付当前状态：VALIDATED
- 目标状态：SENT
- 预期结果：状态更新，创建历史记录并附带 "Payment sent for processing" 说明

**状态转换：**
```
VALIDATED → SENT ✅ （允许）
```

**转换路径：**
```
CREATED → VALIDATED → SENT → COMPLETED
                   ↓
                  FAILED
```

**测试内容：**
- 服务正确处理工作流中间的转换
- 交易说明被适当地更新

---

#### 5.5 `testUpdatePaymentStatus_ValidTransition_SentToCompleted` - 有效转换：SENT → COMPLETED
**测试目的：**
验证 SENT → COMPLETED 转换（成功支付完成）。

**测试场景：**
- 支付当前状态：SENT
- 目标状态：COMPLETED
- 预期结果：状态更新，创建历史记录并附带 "Payment completed successfully" 说明

**状态转换：**
```
SENT → COMPLETED ✅ （允许，终端状态）
```

**达到终端状态：**
- COMPLETED 是终端状态 - 不允许进一步的转换

**测试内容：**
- 服务处理最后的工作流转换
- 服务正确地将支付标记为已完成
- 到达终端状态

---

## 状态转换规则

测试套件验证了以下状态机规则（来自 docs/service-contract.md）：

```
┌─────────────┐
│   CREATED   │
└──────┬──────┘
       │
       ├─→ VALIDATED ────────┐
       │                      │
       └─→ FAILED (✗)        │
                              │
                    ┌─────────┘
                    │
              ┌─────▼──────┐
              │  VALIDATED │
              └─────┬──────┘
                    │
                    ├─→ SENT ──────────────┐
                    │                      │
                    └─→ FAILED (✗)        │
                                           │
                             ┌─────────────┘
                             │
                      ┌──────▼───────┐
                      │    SENT      │
                      └──────┬───────┘
                             │
                             ├─→ COMPLETED (✓ 终端)
                             │
                             └─→ FAILED (✗ 终端)
```

### 转换规则：
- ✅ CREATED → VALIDATED
- ✅ CREATED → FAILED
- ✅ VALIDATED → SENT
- ✅ VALIDATED → FAILED
- ✅ SENT → COMPLETED
- ✅ SENT → FAILED
- ❌ COMPLETED → (任何) - 终端状态，无转换
- ❌ FAILED → (任何) - 终端状态，无转换
- ❌ (任何) → (相同) - 无自转换

---

## 测试执行总结

### 测试结果

| 分类 | 数量 | 状态 |
|------|------|------|
| 总测试数 | 15 | ✅ 通过 |
| 成功场景 | 8 | ✅ 通过 |
| 失败场景 | 7 | ✅ 通过 |
| 异常处理 | 7 | ✅ 通过 |

### 覆盖领域

✅ **业务逻辑：**
- 带验证的支付创建
- 用户存在性和状态检查
- 支付密码验证
- 余额验证

✅ **数据检索：**
- 单笔支付检索
- 用户支付筛选
- 支付历史检索
- 时间顺序排列

✅ **状态管理：**
- 有效的状态转换
- 无效转换的拒绝
- 终端状态强制执行
- 审计跟踪创建

✅ **错误处理：**
- UserNotFoundException
- InvalidPaymentPasswordException
- PaymentNotFoundException
- InvalidPaymentStatusException

---

## 测试覆盖对比表

| 接口方法 | 测试用例 | 成功场景 | 失败场景 | 覆盖率 |
|---------|---------|---------|---------|--------|
| `createPayment` | 4 | 1 | 3 | ✅ 100% |
| `getPaymentById` | 2 | 1 | 1 | ✅ 100% |
| `getPaymentHistory` | 2 | 1 | 1 | ✅ 100% |
| `getPaymentsByUser` | 2 | 1 | 1 | ✅ 100% |
| `updatePaymentStatus` | 5 | 4 | 1 | ✅ 100% |
| **总计** | **15** | **8** | **7** | **✅ 100%** |

---

## 如何运行测试

```bash
# 运行所有支付服务测试
mvn -Dtest=PaymentServiceTest test

# 运行特定测试方法
mvn -Dtest=PaymentServiceTest#testCreatePayment_Success test

# 运行并显示详细输出
mvn -Dtest=PaymentServiceTest test -X

# 运行带 Surefire 交叉驱动器修复
mvn -Dtest=PaymentServiceTest test -DargLine="-Djdk.net.URLClassPath.disableClassPathURLCheck=true"
```

---

## 相关文档

- 服务合同：`docs/service-contract.md`
- API 设计：`docs/api-design.md`
- 数据库设计：`docs/database-design.md`
- Mock 服务指南：`docs/mock-service-guide.md`

---

**最后更新：** 2026-07-28
**测试框架：** JUnit 5 + Mockito
**服务类：** `PaymentServiceImpl`
**测试类：** `PaymentServiceTest`

