# 测试覆盖率报告

- 生成时间：2026-07-30
- 项目路径：`C:\Users\JANG\Desktop\Token_Burner`
- 构建工具：Maven
- 覆盖率工具：JaCoCo（已在 `pom.xml` 中配置）
- 执行命令：`mvn clean test`

## 1. 测试执行结果

本次执行成功完成全部后端测试：

- 总测试数：**46**
- 失败：**0**
- 错误：**0**
- 跳过：**0**
- 总耗时：**10.472s**

## 2. 总体覆盖率汇总

| 指标 | 已覆盖 | 总数 | 覆盖率 |
| --- | ---: | ---: | ---: |
| Instruction | 891 | 1107 | **80.49%** |
| Branch | 62 | 89 | **69.66%** |
| Line | 192 | 250 | **76.80%** |
| Method | 54 | 80 | **67.50%** |
| Class | 25 | 26 | **96.15%** |
| Complexity | 77 | 128 | **60.16%** |

## 3. 按包统计

| 包名 | 行覆盖率 | 覆盖行数 / 总行数 | 分支覆盖率 | 总分支数 |
| --- | ---: | ---: | ---: | ---: |
| `com.example.paymentprocessing` | 33.33% | 1 / 3 | 100.00% | 0 |
| `com.example.paymentprocessing.exception` | 34.43% | 21 / 61 | 100.00% | 0 |
| `com.example.paymentprocessing.entity` | 51.85% | 14 / 27 | 50.00% | 20 |
| `com.example.paymentprocessing.service` | 97.62% | 123 / 126 | 75.36% | 69 |
| `com.example.paymentprocessing.config` | 100.00% | 6 / 6 | 100.00% | 0 |
| `com.example.paymentprocessing.controller` | 100.00% | 12 / 12 | 100.00% | 0 |
| `com.example.paymentprocessing.dto.request` | 100.00% | 2 / 2 | 100.00% | 0 |
| `com.example.paymentprocessing.dto.response` | 100.00% | 4 / 4 | 100.00% | 0 |
| `com.example.paymentprocessing.enums` | 100.00% | 9 / 9 | 100.00% | 0 |

## 4. 覆盖率较低的类

以下类当前覆盖率相对较低，适合作为后续补测优先项：

| 类 | 包 | 行覆盖率 | 覆盖行数 / 总行数 |
| --- | --- | ---: | ---: |
| `InvalidAccountStatusException` | `com.example.paymentprocessing.exception` | 0.00% | 0 / 6 |
| `PaymentProcessingApplication` | `com.example.paymentprocessing` | 33.33% | 1 / 3 |
| `InvalidCurrencyException` | `com.example.paymentprocessing.exception` | 33.33% | 2 / 6 |
| `InvalidPaymentStatusException` | `com.example.paymentprocessing.exception` | 33.33% | 2 / 6 |
| `UserNotFoundException` | `com.example.paymentprocessing.exception` | 33.33% | 2 / 6 |
| `InsufficientBalanceException` | `com.example.paymentprocessing.exception` | 33.33% | 2 / 6 |
| `PaymentNotFoundException` | `com.example.paymentprocessing.exception` | 33.33% | 2 / 6 |
| `InvalidPaymentPasswordException` | `com.example.paymentprocessing.exception` | 33.33% | 2 / 6 |
| `InvalidPaymentAmountException` | `com.example.paymentprocessing.exception` | 33.33% | 2 / 6 |
| `User` | `com.example.paymentprocessing.entity` | 50.00% | 6 / 12 |

## 5. 产出文件位置

JaCoCo 已生成以下报告文件：

- HTML 报告：`target/site/jacoco/index.html`
- XML 报告：`target/site/jacoco/jacoco.xml`
- CSV 报告：`target/site/jacoco/jacoco.csv`

## 6. 结论与建议

1. 当前测试对 `service`、`controller`、`dto`、`enum` 层覆盖较好。
2. 主要薄弱点集中在：
   - `exception` 包
   - `entity` 包
   - 应用启动类 `PaymentProcessingApplication`
3. 如果目标是提升整体行覆盖率，建议优先补充：
   - 各异常类的构造与抛出路径测试
   - `entity` 的 getter/setter、构造器、状态分支相关测试
   - 启动类基础加载测试（如需要纳入统计）

## 7. 复现命令

```powershell
Set-Location "C:\Users\JANG\Desktop\Token_Burner"
mvn clean test
```

执行完成后，直接打开下列文件即可查看可视化报告：

```text
target/site/jacoco/index.html
```

