rootProject.name = "fractional-trading-platform"

include(
    // Shared modules
    "shared:common-domain",
    "shared:common-events",

    // Services
    "services:api-gateway",
    "services:user-service",
    "services:wallet-service",
    "services:trading-service",
    "services:portfolio-service",
    "services:transaction-history-service",
    "services:fee-service",
    "services:securities-pricing-service",
    "services:currency-exchange-service",

    // Integration Tests
    "services:integration-tests"
)
