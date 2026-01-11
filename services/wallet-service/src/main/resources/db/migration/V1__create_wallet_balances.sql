CREATE TABLE wallet_balances (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_currency UNIQUE (user_id, currency)
);

CREATE INDEX idx_wallet_user ON wallet_balances(user_id);
CREATE INDEX idx_wallet_currency ON wallet_balances(currency);
