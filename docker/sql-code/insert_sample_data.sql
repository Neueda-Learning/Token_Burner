-- ===============================================
-- Insert Sample Data for Payment Processing System
-- ===============================================

-- Clear existing data (if any)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE payment_status_history;
TRUNCATE TABLE payments;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- Insert 10 users with explicit IDs
INSERT INTO users (id, account_number, balance, status, payment_password_hash, created_at, updated_at) VALUES
(1, 'ACC-10001', 5000.00, 'ACTIVE', '123456', '2026-07-27 09:00:00', '2026-07-27 09:00:00'),
(2, 'ACC-10002', 3200.00, 'ACTIVE', '123456', '2026-07-27 09:15:00', '2026-07-27 09:15:00'),
(3, 'ACC-10003', 7500.50, 'ACTIVE', '123456', '2026-07-27 09:30:00', '2026-07-27 09:30:00'),
(4, 'ACC-10004', 1200.75, 'ACTIVE', '123456', '2026-07-27 09:45:00', '2026-07-27 09:45:00'),
(5, 'ACC-10005', 9999.99, 'ACTIVE', '123456', '2026-07-27 10:00:00', '2026-07-27 10:00:00'),
(6, 'ACC-10006', 2500.00, 'INACTIVE', '123456', '2026-07-27 10:15:00', '2026-07-27 10:15:00'),
(7, 'ACC-10007', 4300.25, 'ACTIVE', '123456', '2026-07-27 10:30:00', '2026-07-27 10:30:00'),
(8, 'ACC-10008', 6750.00, 'ACTIVE', '123456', '2026-07-27 10:45:00', '2026-07-27 10:45:00'),
(9, 'ACC-10009', 1500.50, 'ACTIVE', '123456', '2026-07-27 11:00:00', '2026-07-27 11:00:00'),
(10, 'ACC-10010', 8200.75, 'ACTIVE', '123456', '2026-07-27 11:15:00', '2026-07-27 11:15:00');

-- Insert 10 payments
INSERT INTO payments (id, source_account_id, destination_account_id, amount, currency, status, created_at, updated_at) VALUES
(1, 1, 2, 1500.00, 'USD', 'COMPLETED', '2026-07-27 10:30:00', '2026-07-27 10:40:00'),
(2, 2, 3, 250.00, 'USD', 'FAILED', '2026-07-27 11:00:00', '2026-07-27 11:05:00'),
(3, 3, 1, 500.50, 'USD', 'COMPLETED', '2026-07-27 11:30:00', '2026-07-27 11:45:00'),
(4, 4, 5, 2000.00, 'USD', 'SENT', '2026-07-27 12:00:00', '2026-07-27 12:10:00'),
(5, 5, 4, 750.25, 'USD', 'VALIDATED', '2026-07-27 12:30:00', '2026-07-27 12:35:00'),
(6, 1, 3, 1200.00, 'USD', 'COMPLETED', '2026-07-27 13:00:00', '2026-07-27 13:15:00'),
(7, 7, 8, 3500.00, 'USD', 'SENT', '2026-07-27 13:30:00', '2026-07-27 13:40:00'),
(8, 8, 9, 600.75, 'USD', 'COMPLETED', '2026-07-27 14:00:00', '2026-07-27 14:20:00'),
(9, 9, 10, 1100.00, 'USD', 'CREATED', '2026-07-27 14:30:00', '2026-07-27 14:30:00'),
(10, 10, 1, 800.50, 'USD', 'VALIDATED', '2026-07-27 15:00:00', '2026-07-27 15:05:00');

-- Insert payment status history records
INSERT INTO payment_status_history (id, payment_id, previous_status, new_status, changed_at, notes) VALUES
(1, 1, NULL, 'CREATED', '2026-07-27 10:30:00', 'Payment created successfully'),
(2, 1, 'CREATED', 'VALIDATED', '2026-07-27 10:32:00', 'Payment request validated'),
(3, 1, 'VALIDATED', 'SENT', '2026-07-27 10:35:00', 'Payment sent for processing'),
(4, 1, 'SENT', 'COMPLETED', '2026-07-27 10:40:00', 'Payment completed successfully'),
(5, 2, NULL, 'CREATED', '2026-07-27 11:00:00', 'Payment created successfully'),
(6, 2, 'CREATED', 'VALIDATED', '2026-07-27 11:02:00', 'Payment request validated'),
(7, 2, 'VALIDATED', 'FAILED', '2026-07-27 11:05:00', 'Payment failed: Insufficient balance'),
(8, 3, NULL, 'CREATED', '2026-07-27 11:30:00', 'Payment created successfully'),
(9, 4, NULL, 'CREATED', '2026-07-27 12:00:00', 'Payment created successfully'),
(10, 5, NULL, 'CREATED', '2026-07-27 12:30:00', 'Payment created successfully');

