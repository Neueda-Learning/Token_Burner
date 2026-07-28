# 数据库初始化
-- 切换库
use payment_db;

-- 用户表（users）
create table if not exists `users`
(
    `id`                    bigint auto_increment comment '用户ID' primary key,
    `account_number`        varchar(64)                            not null comment '账户号',
    `balance`               decimal(18,2)                          not null default 0.00 comment '账户余额',
    `status`                varchar(32)                            not null default 'ACTIVE' comment '账户状态(ACTIVE/INACTIVE)',
    `payment_password_hash` varchar(255)                           not null comment '支付密码哈希',
    `created_at`            datetime                               not null default CURRENT_TIMESTAMP comment '创建时间',
    `updated_at`            datetime                               not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',

    unique index `uk_account_number` (`account_number`),
    constraint `chk_user_status` check (`status` in ('ACTIVE', 'INACTIVE'))
    ) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci comment '用户账户';

-- 支付表（payments）
create table if not exists `payments`
(
    `id`                     bigint auto_increment comment '支付ID' primary key,
    `source_account_id`      bigint                                not null comment '付款用户ID',
    `destination_account_id`     bigint                            not null comment '收款账户ID',
    `amount`                 decimal(18,2)                         not null comment '支付金额',
    `currency`               varchar(16)                           not null default 'USD' comment '币种',
    `status`                 varchar(32)                           not null default 'CREATED' comment '状态(CREATED/VALIDATED/SENT/COMPLETED/FAILED)',
    `created_at`             datetime                              not null default CURRENT_TIMESTAMP comment '创建时间',
    `updated_at`             datetime                              not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',

    index `idx_source_account_id` (`source_account_id`),
    index `idx_destination_account_id` (`destination_account_id`),
    index `idx_status` (`status`),
    index `idx_created_at` (`created_at`),
    constraint `chk_payment_amount` check (`amount` > 0),
    constraint `chk_payment_status` check (`status` in ('CREATED', 'VALIDATED', 'SENT', 'COMPLETED', 'FAILED')),
    constraint `fk_payment_source_user_id`
        foreign key (`source_account_id`) references `users` (`id`) on delete restrict
    ) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci comment '支付';

-- 支付状态历史表（payment_status_history）
create table if not exists `payment_status_history`
(
    `id`              bigint auto_increment comment '历史ID' primary key,
    `payment_id`      bigint                                not null comment '支付ID',
    `previous_status` varchar(32)                           null comment '变更前状态',
    `new_status`      varchar(32)                           not null comment '变更后状态',
    `changed_at`      datetime                              not null default CURRENT_TIMESTAMP comment '变更时间',
    `notes`           varchar(512)                          null comment '备注',

    index `idx_payment_id` (`payment_id`),
    index `idx_changed_at` (`changed_at`),
    constraint `chk_history_previous_status` check (`previous_status` is null or `previous_status` in ('CREATED', 'VALIDATED', 'SENT', 'COMPLETED', 'FAILED')),
    constraint `chk_history_new_status` check (`new_status` in ('CREATED', 'VALIDATED', 'SENT', 'COMPLETED', 'FAILED')),
    constraint `fk_payment_status_history_payment_id`
        foreign key (`payment_id`) references `payments` (`id`) on delete cascade
    ) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci comment '支付状态历史';

-- =========================
-- Seed data (10 rows/table)
-- =========================

