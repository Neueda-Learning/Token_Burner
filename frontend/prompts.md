# Payment Processing System 前端实现提示词（Vue 3）

本文档用于指导前端同学或 AI 助手在 Vue 3 技术栈下实现 Payment Processing System 的前端。内容基于项目 README 与 API 设计文档整理，目标是快速落地一个可演示、可扩展、结构清晰的前端应用。

## 一、项目目标

请实现一个基于 Vue 3 的前端应用，用于完成以下能力：

1. 创建支付
2. 按支付 ID 查询支付详情
3. 查询支付历史
4. 按用户 ID 查询支付列表
5. 更新支付状态
6. 清晰展示支付生命周期

支付生命周期主流程为：

CREATED -> VALIDATED -> SENT -> COMPLETED

允许失败态：

FAILED

## 二、技术约束

请使用以下前端技术方案：

1. Vue 3（Composition API）
2. Vite
3. Vue Router
4. Pinia（用于状态管理）
5. Axios（用于 API 调用）
6. 基础样式可用 CSS Modules 或 Scoped CSS（不强制 UI 库）

## 三、页面与路由设计

请生成以下页面并配置路由：

1. 首页 Dashboard
2. 创建支付页 Create Payment
3. 支付详情页 Payment Detail
4. 支付历史页 Payment History
5. 用户支付列表页 User Payments

建议路由：

1. / -> Dashboard
2. /payments/create -> 创建支付
3. /payments/:paymentId -> 支付详情
4. /payments/:paymentId/history -> 支付历史
5. /users/:userId/payments -> 用户支付列表

## 四、API 对接规范

后端基础路径：

/api/payments

请按以下接口实现前端服务层：

1. 创建支付
方法：POST
路径：/api/payments

2. 查询支付详情
方法：GET
路径：/api/payments/{payment_id}

3. 查询支付历史
方法：GET
路径：/api/payments/{payment_id}/history

4. 按用户查询支付
方法：GET
路径：/api/payments/user/{id}

5. 更新支付状态
方法：PUT
路径：/api/payments/{payment_id}/status

请在前端统一封装：

1. Axios 实例
2. 请求超时
3. 错误拦截
4. 通用错误映射（400、404、409、500）

## 五、数据模型建议

请在前端定义清晰的数据类型（TypeScript 优先）：

1. Payment
字段：paymentId, sourceAccountId, destinationAccountId, amount, currency, status, createdAt, updatedAt

2. PaymentHistoryItem
字段：status, updatedAt

3. PaymentHistoryResponse
字段：paymentId, history

4. ErrorResponse
字段：timestamp, status, error, errorCode, message, path

## 六、核心交互要求

1. 创建支付表单
- 字段：sourceAccountId, destinationAccountId, amount, currency
- 前端校验：必填、金额大于 0、币种长度与格式基本校验
- 创建成功后跳转到支付详情页

2. 支付详情展示
- 展示完整支付信息
- 以视觉标签展示当前状态

3. 支付历史展示
- 按时间顺序展示状态变化
- 使用时间线样式提升可读性

4. 用户支付列表
- 输入或路由传入 userId
- 展示该用户全部支付记录
- 支持点击进入支付详情

5. 更新支付状态
- 在详情页提供状态更新操作
- 仅允许合法状态流转
- 非法流转时展示后端返回错误信息（如 INVALID_STATUS_TRANSITION）

## 七、状态流转前端规则

前端可做轻量预校验，后端做最终校验。

建议前端维护状态流转映射：

1. CREATED -> VALIDATED 或 FAILED
2. VALIDATED -> SENT 或 FAILED
3. SENT -> COMPLETED 或 FAILED
4. COMPLETED -> 无后续
5. FAILED -> 无后续

当用户尝试非法流转时，前端提示：

当前状态不允许流转到目标状态

## 八、UI 展示建议

1. 全局布局
- 顶部导航：Dashboard / Create / Search
- 主内容区卡片化

2. 状态色建议
- CREATED：灰蓝
- VALIDATED：蓝色
- SENT：橙色
- COMPLETED：绿色
- FAILED：红色

3. 可用性要求
- 所有异步操作提供 loading 状态
- 所有失败请求提供可读错误提示
- 空数据场景提供空态文案

## 九、建议目录结构

请按以下结构组织代码：

frontend/
	src/
		api/
			client.ts
			payments.ts
		components/
			AppHeader.vue
			PaymentForm.vue
			PaymentCard.vue
			StatusBadge.vue
			PaymentTimeline.vue
		views/
			DashboardView.vue
			CreatePaymentView.vue
			PaymentDetailView.vue
			PaymentHistoryView.vue
			UserPaymentsView.vue
		stores/
			payment.ts
		router/
			index.ts
		utils/
			status.ts
			format.ts
		App.vue
		main.ts

## 十、给 AI 代码生成器的一次性主提示词

你是高级前端工程师。请使用 Vue 3 + Vite + TypeScript + Vue Router + Pinia + Axios，为 Payment Processing System 生成一个可运行的前端项目骨架，并实现以下功能：创建支付、查询支付详情、查询支付历史、按用户查询支付列表、更新支付状态。后端 API 基础路径为 /api/payments，接口遵循 REST 规范。请实现统一 API 封装、错误拦截、页面级 loading 与 error 状态、状态标签展示、支付历史时间线、以及支付状态流转校验。页面需至少包含 Dashboard、Create Payment、Payment Detail、Payment History、User Payments。代码要求模块化、可维护、命名清晰，并给出每个关键文件的实现内容。

## 十一、分步提示词（可逐段使用）

提示词 A：初始化项目

请生成 Vue 3 + Vite + TypeScript 项目基础结构，集成 Vue Router、Pinia、Axios，并创建基础路由与页面占位组件。

提示词 B：实现 API 层

请实现 Axios 客户端与 payments API 模块，包含 createPayment、getPaymentById、getPaymentHistory、getPaymentsByUser、updatePaymentStatus 方法，并统一处理 400/404/409/500 错误。

提示词 C：实现创建支付页面

请实现 Create Payment 页面与 PaymentForm 组件，包含字段校验、提交 loading、错误提示、创建成功后跳转到支付详情页。

提示词 D：实现支付详情与状态更新

请实现 Payment Detail 页面，展示支付完整信息与状态标签；实现状态更新操作与前端流转预校验，不合法流转时阻止提交并提示。

提示词 E：实现支付历史与用户支付列表

请实现 Payment History 页面（时间线展示）与 User Payments 页面（表格或卡片列表展示），并支持从列表跳转详情。

提示词 F：体验优化

请补充全局错误提示、空态、加载态、基础响应式布局，并优化状态颜色与信息层级，使页面适合项目演示。

## 十二、验收清单

1. 能成功调用创建支付接口并展示返回数据
2. 能通过 paymentId 查询详情
3. 能展示指定 paymentId 的历史记录
4. 能按 userId 查询支付列表
5. 能更新状态并处理非法状态流转
6. 具备清晰的 loading、error、empty 三类界面反馈
7. 项目结构清晰，可继续扩展
