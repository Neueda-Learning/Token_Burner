import random
from datetime import datetime, timedelta
from decimal import Decimal, ROUND_HALF_UP
from collections import Counter
from pathlib import Path
random.seed(20260728)
base_dir = Path(r"C:\Users\ZhuanZ\Desktop\Payment Processing System\docker\sql-code")
out_file = base_dir / "seed_large_mock_data.sql"
START = datetime(2026, 6, 29, 8, 0, 0)
END = datetime(2026, 7, 28, 20, 0, 0)
SPAN_SECONDS = int((END - START).total_seconds())
USER_COUNT = 100
PAYMENT_COUNT = 1000
USER_STATUS_ACTIVE_COUNT = 90
USER_STATUS_INACTIVE_COUNT = 10
PAYMENT_STATUS_COUNTS = {
    "COMPLETED": 700,
    "SENT": 100,
    "VALIDATED": 80,
    "CREATED": 70,
    "FAILED": 50,
}
assert sum(PAYMENT_STATUS_COUNTS.values()) == PAYMENT_COUNT
NOTES = {
    "CREATED": "Payment created successfully",
    "VALIDATED": "Payment request validated",
    "SENT": "Payment sent for processing",
    "COMPLETED": "Payment completed successfully",
}
FAILED_NOTES = [
    "Payment failed: insufficient balance",
    "Payment failed: beneficiary review required",
    "Payment failed: compliance verification pending",
    "Payment failed: destination account unavailable",
]
def rand_dt(start=START, end=END):
    return start + timedelta(seconds=random.randint(0, int((end - start).total_seconds())))
def fmt_dt(dt):
    return dt.strftime("%Y-%m-%d %H:%M:%S")
def money(min_v, max_v):
    value = Decimal(str(random.uniform(min_v, max_v))).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
    return value
def esc(value: str) -> str:
    return value.replace("'", "''")
# Generate users
statuses = ["ACTIVE"] * USER_STATUS_ACTIVE_COUNT + ["INACTIVE"] * USER_STATUS_INACTIVE_COUNT
random.shuffle(statuses)
users = []
for i in range(1, USER_COUNT + 1):
    created_at = rand_dt()
    updated_at = created_at + timedelta(seconds=random.randint(0, max(0, int((END - created_at).total_seconds()))))
    users.append({
        "id": i,
        "account_number": f"ACC-{10000 + i}",
        "balance": money(500, 50000),
        "status": statuses[i - 1],
        "payment_password_hash": "123456",
        "created_at": created_at,
        "updated_at": updated_at,
    })
user_ids = [u["id"] for u in users]
active_user_ids = [u["id"] for u in users if u["status"] == "ACTIVE"]
# Generate payments
payment_statuses = []
for status, count in PAYMENT_STATUS_COUNTS.items():
    payment_statuses.extend([status] * count)
random.shuffle(payment_statuses)
payments = []
history_rows = []
history_id = 1
for payment_id, final_status in enumerate(payment_statuses, start=1):
    source_id = random.choice(active_user_ids)
    destination_id = random.choice(user_ids)
    while destination_id == source_id:
        destination_id = random.choice(user_ids)
    created_at = rand_dt()
    if final_status == "CREATED":
        updated_at = created_at + timedelta(minutes=random.randint(0, 30))
        transitions = [(None, "CREATED", created_at, NOTES["CREATED"])]
    elif final_status == "VALIDATED":
        validated_at = created_at + timedelta(minutes=random.randint(5, 180))
        updated_at = validated_at
        transitions = [
            (None, "CREATED", created_at, NOTES["CREATED"]),
            ("CREATED", "VALIDATED", validated_at, NOTES["VALIDATED"]),
        ]
    elif final_status == "SENT":
        validated_at = created_at + timedelta(minutes=random.randint(5, 120))
        sent_at = validated_at + timedelta(minutes=random.randint(5, 180))
        updated_at = sent_at
        transitions = [
            (None, "CREATED", created_at, NOTES["CREATED"]),
            ("CREATED", "VALIDATED", validated_at, NOTES["VALIDATED"]),
            ("VALIDATED", "SENT", sent_at, NOTES["SENT"]),
        ]
    elif final_status == "COMPLETED":
        validated_at = created_at + timedelta(minutes=random.randint(5, 120))
        sent_at = validated_at + timedelta(minutes=random.randint(5, 180))
        completed_at = sent_at + timedelta(minutes=random.randint(5, 240))
        updated_at = completed_at
        transitions = [
            (None, "CREATED", created_at, NOTES["CREATED"]),
            ("CREATED", "VALIDATED", validated_at, NOTES["VALIDATED"]),
            ("VALIDATED", "SENT", sent_at, NOTES["SENT"]),
            ("SENT", "COMPLETED", completed_at, NOTES["COMPLETED"]),
        ]
    else:  # FAILED
        failure_note = random.choice(FAILED_NOTES)
        if random.random() < 0.45:
            failed_at = created_at + timedelta(minutes=random.randint(5, 150))
            updated_at = failed_at
            transitions = [
                (None, "CREATED", created_at, NOTES["CREATED"]),
                ("CREATED", "FAILED", failed_at, failure_note),
            ]
        else:
            validated_at = created_at + timedelta(minutes=random.randint(5, 120))
            failed_at = validated_at + timedelta(minutes=random.randint(5, 180))
            updated_at = failed_at
            transitions = [
                (None, "CREATED", created_at, NOTES["CREATED"]),
                ("CREATED", "VALIDATED", validated_at, NOTES["VALIDATED"]),
                ("VALIDATED", "FAILED", failed_at, failure_note),
            ]
    if updated_at > END:
        shift = updated_at - END
        created_at -= shift
        updated_at -= shift
        adjusted = []
        for prev, new, changed_at, notes in transitions:
            adjusted.append((prev, new, changed_at - shift, notes))
        transitions = adjusted
    payment = {
        "id": payment_id,
        "source_account_id": source_id,
        "destination_account_id": destination_id,
        "amount": money(10, 20000),
        "currency": "USD",
        "status": final_status,
        "created_at": created_at,
        "updated_at": updated_at,
    }
    payments.append(payment)
    for prev_status, new_status, changed_at, note in transitions:
        history_rows.append({
            "id": history_id,
            "payment_id": payment_id,
            "previous_status": prev_status,
            "new_status": new_status,
            "changed_at": changed_at,
            "notes": note,
        })
        history_id += 1
# Validation
assert len(users) == USER_COUNT
assert len(payments) == PAYMENT_COUNT
assert len({u['account_number'] for u in users}) == USER_COUNT
assert sum(1 for u in users if u['status'] == 'ACTIVE') == USER_STATUS_ACTIVE_COUNT
assert sum(1 for u in users if u['status'] == 'INACTIVE') == USER_STATUS_INACTIVE_COUNT
payment_counter = Counter(p['status'] for p in payments)
assert payment_counter == PAYMENT_STATUS_COUNTS, payment_counter
for u in users:
    assert Decimal('500.00') <= u['balance'] <= Decimal('50000.00')
    assert START <= u['created_at'] <= END
    assert u['created_at'] <= u['updated_at'] <= END
user_id_set = set(user_ids)
payment_id_set = {p['id'] for p in payments}
for p in payments:
    assert p['source_account_id'] in user_id_set
    assert p['destination_account_id'] in user_id_set
    assert p['source_account_id'] != p['destination_account_id']
    assert Decimal('10.00') <= p['amount'] <= Decimal('20000.00')
    assert p['currency'] == 'USD'
    assert START <= p['created_at'] <= END
    assert p['created_at'] <= p['updated_at'] <= END
valid_statuses = {'CREATED','VALIDATED','SENT','COMPLETED','FAILED'}
for h in history_rows:
    assert h['payment_id'] in payment_id_set
    assert h['previous_status'] is None or h['previous_status'] in valid_statuses
    assert h['new_status'] in valid_statuses
    assert h['notes']
# SQL generation
lines = []
lines.append("-- ================================================================")
lines.append("-- Large-scale Development Seed Data for TransiPay Payment System")
lines.append("-- Generated for MySQL 8.0 dashboard, search, detail, and history testing")
lines.append("-- ================================================================")
lines.append("")
lines.append("USE payment_db;")
lines.append("")
lines.append("SET FOREIGN_KEY_CHECKS = 0;")
lines.append("TRUNCATE TABLE payment_status_history;")
lines.append("TRUNCATE TABLE payments;")
lines.append("TRUNCATE TABLE users;")
lines.append("SET FOREIGN_KEY_CHECKS = 1;")
lines.append("")
lines.append("-- Insert 100 users")
lines.append("INSERT INTO users (id, account_number, balance, status, payment_password_hash, created_at, updated_at) VALUES")
user_values = []
for u in users:
    user_values.append(
        f"({u['id']}, '{u['account_number']}', {u['balance']}, '{u['status']}', '{u['payment_password_hash']}', '{fmt_dt(u['created_at'])}', '{fmt_dt(u['updated_at'])}')"
    )
lines.append(",\n".join(user_values) + ";")
lines.append("")
lines.append("-- Insert 1000 payments")
lines.append("INSERT INTO payments (id, source_account_id, destination_account_id, amount, currency, status, created_at, updated_at) VALUES")
payment_values = []
for p in payments:
    payment_values.append(
        f"({p['id']}, {p['source_account_id']}, {p['destination_account_id']}, {p['amount']}, '{p['currency']}', '{p['status']}', '{fmt_dt(p['created_at'])}', '{fmt_dt(p['updated_at'])}')"
    )
lines.append(",\n".join(payment_values) + ";")
lines.append("")
lines.append(f"-- Insert {len(history_rows)} payment status history records")
lines.append("INSERT INTO payment_status_history (id, payment_id, previous_status, new_status, changed_at, notes) VALUES")
history_values = []
for h in history_rows:
    prev = "NULL" if h['previous_status'] is None else f"'{h['previous_status']}'"
    history_values.append(
        f"({h['id']}, {h['payment_id']}, {prev}, '{h['new_status']}', '{fmt_dt(h['changed_at'])}', '{esc(h['notes'])}')"
    )
lines.append(",\n".join(history_values) + ";")
lines.append("")
lines.append("SET FOREIGN_KEY_CHECKS = 1;")
out_file.write_text("\n".join(lines), encoding="utf-8")
print(f"Created: {out_file}")
print(f"Users: {len(users)} | Payments: {len(payments)} | History: {len(history_rows)}")
print(f"User status distribution: {Counter(u['status'] for u in users)}")
print(f"Payment status distribution: {payment_counter}")
