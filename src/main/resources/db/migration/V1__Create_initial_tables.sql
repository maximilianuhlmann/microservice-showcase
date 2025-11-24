-- Flyway migration script for initial database schema
-- This creates the core tables for usage-based billing

-- Table: customers
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_customer_id ON customers(customer_id);
CREATE INDEX idx_customers_status ON customers(status);

-- Table: usage_events
CREATE TABLE IF NOT EXISTS usage_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id VARCHAR(255) NOT NULL,
    service_type VARCHAR(255) NOT NULL,
    quantity DECIMAL(19, 4) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usage_events_customer_timestamp ON usage_events(customer_id, timestamp);
CREATE INDEX idx_usage_events_timestamp ON usage_events(timestamp);
CREATE INDEX idx_usage_events_customer_service ON usage_events(customer_id, service_type);

-- Table: billing_records
CREATE TABLE IF NOT EXISTS billing_records (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    billing_period DATE NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(customer_id, billing_period)
);

CREATE INDEX idx_billing_records_customer_period ON billing_records(customer_id, billing_period);
CREATE INDEX idx_billing_records_period ON billing_records(billing_period);

-- Table: billing_cycles
CREATE TABLE IF NOT EXISTS billing_cycles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    invoice_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_billing_cycles_customer_period ON billing_cycles(customer_id, start_date, end_date);
CREATE INDEX idx_billing_cycles_status ON billing_cycles(status);

