CREATE TABLE IF NOT EXISTS rfqs (
    id BIGSERIAL PRIMARY KEY,
    reference_id VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    bid_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    bid_close_time TIMESTAMP WITH TIME ZONE NOT NULL,
    original_bid_close_time TIMESTAMP WITH TIME ZONE NOT NULL,
    forced_bid_close_time TIMESTAMP WITH TIME ZONE NOT NULL,
    pickup_service_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    trigger_window_minutes INT NOT NULL DEFAULT 10,
    extension_duration_minutes INT NOT NULL DEFAULT 5,
    extension_trigger_type VARCHAR(32) NOT NULL DEFAULT 'ANY_BID',
    total_extensions_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quotes (
    id BIGSERIAL PRIMARY KEY,
    rfq_id BIGINT NOT NULL REFERENCES rfqs(id) ON DELETE CASCADE,
    carrier_name VARCHAR(255) NOT NULL,
    freight_charges NUMERIC(14, 2) NOT NULL,
    origin_charges NUMERIC(14, 2) NOT NULL,
    destination_charges NUMERIC(14, 2) NOT NULL,
    total_amount NUMERIC(14, 2) NOT NULL,
    transit_time_days INT NOT NULL,
    quote_validity DATE NOT NULL,
    supplier_rank INT NOT NULL DEFAULT 1,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS auction_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    rfq_id BIGINT NOT NULL REFERENCES rfqs(id) ON DELETE CASCADE,
    quote_id BIGINT REFERENCES quotes(id) ON DELETE SET NULL,
    event_type VARCHAR(64) NOT NULL,
    reason TEXT NOT NULL,
    previous_close_time TIMESTAMP WITH TIME ZONE,
    new_close_time TIMESTAMP WITH TIME ZONE,
    carrier_name VARCHAR(255),
    bid_amount NUMERIC(14, 2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rfqs_status ON rfqs(status);
CREATE INDEX IF NOT EXISTS idx_quotes_rfq_total ON quotes(rfq_id, total_amount ASC);
CREATE INDEX IF NOT EXISTS idx_logs_rfq_created ON auction_activity_logs(rfq_id, created_at DESC);
