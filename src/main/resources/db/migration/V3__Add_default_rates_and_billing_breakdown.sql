-- Migration: Add default rates table and billing breakdown
-- Default rates are used when no customer-specific pricing exists

-- Table: default_rates (for service types without customer-specific pricing)
CREATE TABLE IF NOT EXISTS default_rates (
    id BIGSERIAL PRIMARY KEY,
    service_type VARCHAR(255) NOT NULL UNIQUE,
    rate DECIMAL(19, 4) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_default_rates_service_type ON default_rates(service_type);
CREATE INDEX idx_default_rates_active ON default_rates(active);

-- Insert default rates (only if they don't exist)
INSERT INTO default_rates (service_type, rate)
SELECT 'api-calls', 0.001 WHERE NOT EXISTS (SELECT 1 FROM default_rates WHERE service_type = 'api-calls');

INSERT INTO default_rates (service_type, rate)
SELECT 'storage', 0.10 WHERE NOT EXISTS (SELECT 1 FROM default_rates WHERE service_type = 'storage');

INSERT INTO default_rates (service_type, rate)
SELECT 'compute', 0.50 WHERE NOT EXISTS (SELECT 1 FROM default_rates WHERE service_type = 'compute');

INSERT INTO default_rates (service_type, rate)
SELECT 'data-transfer', 0.05 WHERE NOT EXISTS (SELECT 1 FROM default_rates WHERE service_type = 'data-transfer');

-- Table: billing_breakdown (detailed breakdown per service type)
CREATE TABLE IF NOT EXISTS billing_breakdown (
    id BIGSERIAL PRIMARY KEY,
    billing_record_id BIGINT NOT NULL,
    service_type VARCHAR(255) NOT NULL,
    quantity DECIMAL(19, 4) NOT NULL,
    rate DECIMAL(19, 4) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (billing_record_id) REFERENCES billing_records(id) ON DELETE CASCADE
);

CREATE INDEX idx_billing_breakdown_record ON billing_breakdown(billing_record_id);
CREATE INDEX idx_billing_breakdown_service_type ON billing_breakdown(service_type);

-- Change billing_period from DATE to VARCHAR(7) to store YYYY-MM format
-- PostgreSQL syntax
ALTER TABLE billing_records ALTER COLUMN billing_period TYPE VARCHAR(7) USING TO_CHAR(billing_period, 'YYYY-MM');

