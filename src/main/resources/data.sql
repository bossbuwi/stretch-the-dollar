INSERT INTO transactions (id, name, description, transaction_type, amount, currency)
VALUES (1, 'Transaction 1', 'Test data', 'SAVINGS', 100, 'USD');

INSERT INTO transactions (id, name, description, transaction_type, amount, currency)
VALUES (2, 'Transaction 2', 'Test data', 'EXPENSES', 100, 'USD');

INSERT INTO transactions (id, name, description, transaction_type, amount, currency)
VALUES (3, 'Transaction 3', 'Test data', 'SAVINGS', 50, 'USD');

ALTER TABLE transactions ALTER COLUMN id RESTART WITH 4;