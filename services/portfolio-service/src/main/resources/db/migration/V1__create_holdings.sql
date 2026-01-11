CREATE TABLE holdings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    quantity DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    average_price DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_symbol UNIQUE (user_id, symbol)
);

CREATE INDEX idx_holdings_user ON holdings(user_id);
