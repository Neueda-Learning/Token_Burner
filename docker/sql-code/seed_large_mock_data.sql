-- ================================================================
-- Large-scale Development Seed Data for TransiPay Payment System
-- Purpose:
--   - Dashboard summary statistics
--   - Payment status distribution chart
--   - User payment search
--   - Payment detail page
--   - Payment lifecycle history display
--   - Future payment trend charts
-- Compatible with MySQL 8.0
-- ================================================================

USE payment_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE payment_status_history;
TRUNCATE TABLE payments;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

DROP PROCEDURE IF EXISTS seed_large_mock_data;

DELIMITER $$

CREATE PROCEDURE seed_large_mock_data()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE remaining_users INT DEFAULT 100;
    DECLARE remaining_active INT DEFAULT 90;
    DECLARE remaining_inactive INT DEFAULT 10;

    DECLARE remaining_payments INT DEFAULT 1000;
    DECLARE remaining_completed INT DEFAULT 700;
    DECLARE remaining_sent INT DEFAULT 100;
    DECLARE remaining_validated INT DEFAULT 80;
    DECLARE remaining_created INT DEFAULT 70;
    DECLARE remaining_failed INT DEFAULT 50;

    DECLARE pick INT;
    DECLARE source_id BIGINT;
    DECLARE destination_id BIGINT;
    DECLARE payment_id BIGINT;
    DECLARE max_update_seconds INT;
    DECLARE failure_path INT;

    DECLARE account_status VARCHAR(32);
    DECLARE payment_status VARCHAR(32);
    DECLARE failure_note VARCHAR(512);

    DECLARE payment_amount DECIMAL(18,2);

    DECLARE user_created_at DATETIME;
    DECLARE user_updated_at DATETIME;
    DECLARE payment_created_at DATETIME;
    DECLARE payment_updated_at DATETIME;
    DECLARE validated_at DATETIME;
    DECLARE sent_at DATETIME;
    DECLARE completed_at DATETIME;
    DECLARE failed_at DATETIME;

    -- ------------------------------------------------------------
    -- Generate 100 users
    -- 90% ACTIVE, 10% INACTIVE
    -- ------------------------------------------------------------
    WHILE i <= 100 DO
        SET user_created_at = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 2592000) SECOND);
        SET max_update_seconds = GREATEST(TIMESTAMPDIFF(SECOND, user_created_at, NOW()), 0);
        SET user_updated_at = DATE_ADD(user_created_at, INTERVAL FLOOR(RAND() * (max_update_seconds + 1)) SECOND);

        SET pick = FLOOR(1 + RAND() * remaining_users);
        IF pick <= remaining_active THEN
            SET account_status = 'ACTIVE';
            SET remaining_active = remaining_active - 1;
        ELSE
            SET account_status = 'INACTIVE';
            SET remaining_inactive = remaining_inactive - 1;
        END IF;

        INSERT INTO users (
            account_number,
            balance,
            status,
            payment_password_hash,
            created_at,
            updated_at
        ) VALUES (
            CONCAT('ACC-', 10000 + i),
            ROUND(500 + RAND() * 49500, 2),
            account_status,
            '123456',
            user_created_at,
            user_updated_at
        );

        SET remaining_users = remaining_users - 1;
        SET i = i + 1;
    END WHILE;

    -- ------------------------------------------------------------
    -- Generate 1000 payments
    -- Distribution:
    -- COMPLETED 70%, SENT 10%, VALIDATED 8%, CREATED 7%, FAILED 5%
    -- ------------------------------------------------------------
    SET i = 1;
    WHILE i <= 1000 DO
        SET pick = FLOOR(1 + RAND() * remaining_payments);
        IF pick <= remaining_completed THEN
            SET payment_status = 'COMPLETED';
            SET remaining_completed = remaining_completed - 1;
        ELSEIF pick <= remaining_completed + remaining_sent THEN
            SET payment_status = 'SENT';
            SET remaining_sent = remaining_sent - 1;
        ELSEIF pick <= remaining_completed + remaining_sent + remaining_validated THEN
            SET payment_status = 'VALIDATED';
            SET remaining_validated = remaining_validated - 1;
        ELSEIF pick <= remaining_completed + remaining_sent + remaining_validated + remaining_created THEN
            SET payment_status = 'CREATED';
            SET remaining_created = remaining_created - 1;
        ELSE
            SET payment_status = 'FAILED';
            SET remaining_failed = remaining_failed - 1;
        END IF;

        SET source_id = FLOOR(1 + RAND() * 100);
        SET destination_id = FLOOR(1 + RAND() * 100);
        WHILE destination_id = source_id DO
            SET destination_id = FLOOR(1 + RAND() * 100);
        END WHILE;

        SET payment_amount = ROUND(10 + RAND() * 19990, 2);
        -- Keep payments at least 12 hours old so lifecycle timestamps remain realistic and never go beyond NOW().
        SET payment_created_at = DATE_SUB(NOW(), INTERVAL (720 + FLOOR(RAND() * 42481)) MINUTE);

        IF payment_status = 'CREATED' THEN
            SET payment_updated_at = DATE_ADD(payment_created_at, INTERVAL FLOOR(RAND() * 31) MINUTE);

        ELSEIF payment_status = 'VALIDATED' THEN
            SET validated_at = DATE_ADD(payment_created_at, INTERVAL (5 + FLOOR(RAND() * 176)) MINUTE);
            SET payment_updated_at = validated_at;

        ELSEIF payment_status = 'SENT' THEN
            SET validated_at = DATE_ADD(payment_created_at, INTERVAL (5 + FLOOR(RAND() * 116)) MINUTE);
            SET sent_at = DATE_ADD(validated_at, INTERVAL (5 + FLOOR(RAND() * 176)) MINUTE);
            SET payment_updated_at = sent_at;

        ELSEIF payment_status = 'COMPLETED' THEN
            SET validated_at = DATE_ADD(payment_created_at, INTERVAL (5 + FLOOR(RAND() * 116)) MINUTE);
            SET sent_at = DATE_ADD(validated_at, INTERVAL (5 + FLOOR(RAND() * 176)) MINUTE);
            SET completed_at = DATE_ADD(sent_at, INTERVAL (5 + FLOOR(RAND() * 236)) MINUTE);
            SET payment_updated_at = completed_at;

        ELSE
            SET failure_path = IF(RAND() < 0.45, 1, 2);
            SET failure_note = CASE FLOOR(RAND() * 4)
                WHEN 0 THEN 'Payment failed: insufficient balance'
                WHEN 1 THEN 'Payment failed: beneficiary review required'
                WHEN 2 THEN 'Payment failed: compliance verification pending'
                ELSE 'Payment failed: destination account unavailable'
            END;

            IF failure_path = 1 THEN
                SET failed_at = DATE_ADD(payment_created_at, INTERVAL (5 + FLOOR(RAND() * 146)) MINUTE);
                SET payment_updated_at = failed_at;
            ELSE
                SET validated_at = DATE_ADD(payment_created_at, INTERVAL (5 + FLOOR(RAND() * 116)) MINUTE);
                SET failed_at = DATE_ADD(validated_at, INTERVAL (5 + FLOOR(RAND() * 176)) MINUTE);
                SET payment_updated_at = failed_at;
            END IF;
        END IF;

        INSERT INTO payments (
            source_account_id,
            destination_account_id,
            amount,
            currency,
            status,
            created_at,
            updated_at
        ) VALUES (
            source_id,
            destination_id,
            payment_amount,
            'USD',
            payment_status,
            payment_created_at,
            payment_updated_at
        );

        SET payment_id = LAST_INSERT_ID();

        -- --------------------------------------------------------
        -- Generate lifecycle history based on final payment status
        -- --------------------------------------------------------
        INSERT INTO payment_status_history (
            payment_id,
            previous_status,
            new_status,
            changed_at,
            notes
        ) VALUES (
            payment_id,
            NULL,
            'CREATED',
            payment_created_at,
            'Payment created successfully'
        );

        IF payment_status = 'VALIDATED' THEN
            INSERT INTO payment_status_history (
                payment_id,
                previous_status,
                new_status,
                changed_at,
                notes
            ) VALUES (
                payment_id,
                'CREATED',
                'VALIDATED',
                validated_at,
                'Payment request validated'
            );

        ELSEIF payment_status = 'SENT' THEN
            INSERT INTO payment_status_history (payment_id, previous_status, new_status, changed_at, notes)
            VALUES
                (payment_id, 'CREATED', 'VALIDATED', validated_at, 'Payment request validated'),
                (payment_id, 'VALIDATED', 'SENT', sent_at, 'Payment sent for processing');

        ELSEIF payment_status = 'COMPLETED' THEN
            INSERT INTO payment_status_history (payment_id, previous_status, new_status, changed_at, notes)
            VALUES
                (payment_id, 'CREATED', 'VALIDATED', validated_at, 'Payment request validated'),
                (payment_id, 'VALIDATED', 'SENT', sent_at, 'Payment sent for processing'),
                (payment_id, 'SENT', 'COMPLETED', completed_at, 'Payment completed successfully');

        ELSEIF payment_status = 'FAILED' THEN
            IF failure_path = 1 THEN
                INSERT INTO payment_status_history (
                    payment_id,
                    previous_status,
                    new_status,
                    changed_at,
                    notes
                ) VALUES (
                    payment_id,
                    'CREATED',
                    'FAILED',
                    failed_at,
                    failure_note
                );
            ELSE
                INSERT INTO payment_status_history (payment_id, previous_status, new_status, changed_at, notes)
                VALUES
                    (payment_id, 'CREATED', 'VALIDATED', validated_at, 'Payment request validated'),
                    (payment_id, 'VALIDATED', 'FAILED', failed_at, failure_note);
            END IF;
        END IF;

        SET remaining_payments = remaining_payments - 1;
        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

CALL seed_large_mock_data();
DROP PROCEDURE IF EXISTS seed_large_mock_data;

