CREATE TABLE fee_rules (
    id UUID PRIMARY KEY,
    rule_type VARCHAR(20) NOT NULL,
    symbol VARCHAR(10),
    from_currency VARCHAR(3),
    to_currency VARCHAR(3),
    fixed_fee DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    percentage_fee DECIMAL(5, 4) NOT NULL DEFAULT 0.0000
);

CREATE INDEX idx_fee_rules_trading ON fee_rules(rule_type, symbol) WHERE rule_type = 'TRADING';
CREATE INDEX idx_fee_rules_exchange ON fee_rules(rule_type, from_currency, to_currency) WHERE rule_type = 'EXCHANGE';
