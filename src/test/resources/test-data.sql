-- Test data for integration tests
-- This file can be used with @Sql annotation in tests

-- Insert test usage events for customer-123
INSERT INTO usage_events (id, customer_id, service_type, quantity, unit, timestamp, metadata, created_at)
VALUES
    ('650e8400-e29b-41d4-a716-446655440000', 'customer-123', 'api-calls', 100.00, 'requests', '2024-01-15 10:00:00', '{"endpoint":"/api/users","method":"GET"}', CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440001', 'customer-123', 'api-calls', 200.00, 'requests', '2024-01-15 11:00:00', '{"endpoint":"/api/products","method":"POST"}', CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440002', 'customer-123', 'storage', 5.50, 'gb', '2024-01-15 12:00:00', '{"bucket":"user-data"}', CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440003', 'customer-123', 'api-calls', 150.00, 'requests', '2024-01-16 09:00:00', '{"endpoint":"/api/orders","method":"GET"}', CURRENT_TIMESTAMP);

-- Insert test usage events for customer-456
INSERT INTO usage_events (id, customer_id, service_type, quantity, unit, timestamp, metadata, created_at)
VALUES
    ('650e8400-e29b-41d4-a716-446655440010', 'customer-456', 'api-calls', 50.00, 'requests', '2024-01-15 10:00:00', '{"endpoint":"/api/users","method":"GET"}', CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440011', 'customer-456', 'compute', 2.50, 'hours', '2024-01-15 14:00:00', '{"instance":"t2.medium"}', CURRENT_TIMESTAMP);

-- Insert test billing record (id is auto-generated via BIGSERIAL)
INSERT INTO billing_records (customer_id, billing_period, total_amount, created_at)
VALUES
    ('customer-123', '2024-01-01', 4.50, CURRENT_TIMESTAMP);

