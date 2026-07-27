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
    `destination_account_number` varchar(64)                       not null comment '收款账户号',
    `amount`                 decimal(18,2)                         not null comment '支付金额',
    `currency`               varchar(16)                           not null default 'USD' comment '币种',
    `status`                 varchar(32)                           not null default 'CREATED' comment '状态(CREATED/VALIDATED/SENT/COMPLETED/FAILED)',
    `created_at`             datetime                              not null default CURRENT_TIMESTAMP comment '创建时间',
    `updated_at`             datetime                              not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',

    index `idx_source_account_id` (`source_account_id`),
    index `idx_destination_account_number` (`destination_account_number`),
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

-- users: 10 records
INSERT INTO `users` (`id`, `account_number`, `balance`, `status`, `payment_password_hash`, `created_at`, `updated_at`) VALUES
(1001, '6222021234567890123', 12000.00, 'ACTIVE',   'hash_pwd_u1001', '2026-07-27 09:00:00', '2026-07-27 09:00:00'),
(1002, '6214830000000001',    8600.50,  'ACTIVE',   'hash_pwd_u1002', '2026-07-27 09:02:00', '2026-07-27 09:02:00'),
(1003, '6228480402564890018', 3000.00,  'ACTIVE',   'hash_pwd_u1003', '2026-07-27 09:04:00', '2026-07-27 09:04:00'),
(1004, '6259071000001234',    1500.00,  'INACTIVE', 'hash_pwd_u1004', '2026-07-27 09:06:00', '2026-07-27 09:06:00'),
(1005, '6217003810000000005', 500.00,   'ACTIVE',   'hash_pwd_u1005', '2026-07-27 09:08:00', '2026-07-27 09:08:00'),
(1006, '6225889900112233445', 20000.00, 'ACTIVE',   'hash_pwd_u1006', '2026-07-27 09:10:00', '2026-07-27 09:10:00'),
(1007, '622260999900001234',  980.75,   'ACTIVE',   'hash_pwd_u1007', '2026-07-27 09:12:00', '2026-07-27 09:12:00'),
(1008, '6216610180001234567', 4300.20,  'INACTIVE', 'hash_pwd_u1008', '2026-07-27 09:14:00', '2026-07-27 09:14:00'),
(1009, '6226090000008888999', 7650.00,  'ACTIVE',   'hash_pwd_u1009', '2026-07-27 09:16:00', '2026-07-27 09:16:00'),
(1010, '6212261001020304050', 1111.11,  'ACTIVE',   'hash_pwd_u1010', '2026-07-27 09:18:00', '2026-07-27 09:18:00');

-- payments: 10 records
INSERT INTO `payments` (`id`, `source_account_id`, `destination_account_number`, `amount`, `currency`, `status`, `created_at`, `updated_at`) VALUES
(2001, 1001, '6214830000000001',    500.00,  'USD', 'VALIDATED', '2026-07-27 10:00:00', '2026-07-27 10:02:00'),
(2002, 1002, '6228480402564890018', 1200.00, 'USD', 'SENT',      '2026-07-27 10:05:00', '2026-07-27 10:08:00'),
(2003, 1003, '6222021234567890123', 88.88,   'USD', 'FAILED',    '2026-07-27 10:10:00', '2026-07-27 10:12:00'),
(2004, 1004, '6217003810000000005', 300.00,  'USD', 'CREATED',   '2026-07-27 10:15:00', '2026-07-27 10:15:00'),
(2005, 1005, '6225889900112233445', 66.60,   'USD', 'COMPLETED', '2026-07-27 10:20:00', '2026-07-27 10:25:00'),
(2006, 1006, '622260999900001234',  999.99,  'USD', 'SENT',      '2026-07-27 10:30:00', '2026-07-27 10:33:00'),
(2007, 1007, '6216610180001234567', 10.00,   'USD', 'VALIDATED', '2026-07-27 10:35:00', '2026-07-27 10:36:00'),
(2008, 1008, '6226090000008888999', 250.50,  'USD', 'FAILED',    '2026-07-27 10:40:00', '2026-07-27 10:42:00'),
(2009, 1009, '6212261001020304050', 700.00,  'USD', 'COMPLETED', '2026-07-27 10:45:00', '2026-07-27 10:50:00'),
(2010, 1010, '6259071000001234',    45.67,   'USD', 'CREATED',   '2026-07-27 10:55:00', '2026-07-27 10:55:00');

-- payment_status_history: 10 records
INSERT INTO `payment_status_history` (`id`, `payment_id`, `previous_status`, `new_status`, `changed_at`, `notes`) VALUES
(3001, 2001, 'CREATED',   'VALIDATED', '2026-07-27 10:02:00', 'Password verified'),
(3002, 2002, 'VALIDATED', 'SENT',      '2026-07-27 10:08:00', 'Sent to clearing'),
(3003, 2003, 'VALIDATED', 'FAILED',    '2026-07-27 10:12:00', 'Insufficient balance'),
(3004, 2004, NULL,        'CREATED',   '2026-07-27 10:15:00', 'Payment created'),
(3005, 2005, 'SENT',      'COMPLETED', '2026-07-27 10:25:00', 'Settlement success'),
(3006, 2006, 'VALIDATED', 'SENT',      '2026-07-27 10:33:00', 'Sent to processor'),
(3007, 2007, 'CREATED',   'VALIDATED', '2026-07-27 10:36:00', 'Risk check passed'),
(3008, 2008, 'SENT',      'FAILED',    '2026-07-27 10:42:00', 'Channel timeout'),
(3009, 2009, 'SENT',      'COMPLETED', '2026-07-27 10:50:00', 'Funds credited'),
(3010, 2010, NULL,        'CREATED',   '2026-07-27 10:55:00', 'Payment initialized');
