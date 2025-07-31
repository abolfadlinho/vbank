-- Initialize PostgreSQL database with required schema

-- Create enum type for transaction status
CREATE TYPE transaction_status_enum AS ENUM ('INITIATED', 'SUCCESS', 'FAILED');

-- Create transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_account_id UUID NOT NULL,
    to_account_id UUID NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description VARCHAR(255),
    status transaction_status_enum DEFAULT 'INITIATED',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on timestamp for efficient incremental loading
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp);

-- Create some sample data for testing
INSERT INTO transactions (from_account_id, to_account_id, amount, description, status, timestamp) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 100.50, 'Payment for services', 'SUCCESS', NOW() - INTERVAL '1 day'),
    ('550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440004', 2500.00, 'Large transfer', 'SUCCESS', NOW() - INTERVAL '12 hours'),
    ('550e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440006', 50.00, 'Small payment', 'FAILED', NOW() - INTERVAL '6 hours'),
    ('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440008', 10000.00, 'Suspicious large amount', 'INITIATED', NOW() - INTERVAL '2 hours'),
    ('550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440010', 75.25, 'Regular payment', 'SUCCESS', NOW() - INTERVAL '1 hour');

-- Create a table to track fraud labels (for ML training)
CREATE TABLE fraud_labels (
    transaction_id UUID REFERENCES transactions(id),
    is_fraud BOOLEAN NOT NULL,
    labeled_by VARCHAR(100),
    labeled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (transaction_id)
);

-- Add some fraud labels for the sample data
INSERT INTO fraud_labels (transaction_id, is_fraud, labeled_by) 
SELECT id, 
       CASE 
           WHEN amount > 5000 THEN true 
           WHEN status = 'FAILED' THEN true
           ELSE false 
       END as is_fraud,
       'system_init'
FROM transactions;