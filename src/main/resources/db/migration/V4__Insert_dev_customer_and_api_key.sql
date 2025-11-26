-- Migration: Insert development customer and API key
-- This ensures the dev API key is properly mapped to a customer for testing

-- Insert dev customer (only if it doesn't exist)
INSERT INTO customers (customer_id, name, active)
SELECT 'customer-123', 'Development Customer', TRUE
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE customer_id = 'customer-123');

-- Insert dev API key (only if it doesn't exist)
INSERT INTO api_keys (api_key, customer_id, active)
SELECT 'dev-api-key-123', 'customer-123', TRUE
WHERE NOT EXISTS (SELECT 1 FROM api_keys WHERE api_key = 'dev-api-key-123');

