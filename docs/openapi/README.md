# OpenAPI Documentation

This directory contains OpenAPI 3.0 specifications for all microservices in the Fractional Stock Trading Platform.

## Generated Documentation

The OpenAPI specs are automatically generated from running services using:

```bash
./gradlew generateOpenApiDocs
```

**Note:** All services must be running (via `docker-compose up`) before generating documentation.

## Available Specifications

### Core Services

1. **api-gateway-openapi.yaml** - Unified API Gateway
   - Port: 8080
   - All public-facing endpoints
   - Aggregates all backend services

2. **user-signup-service-openapi.yaml** - User Registration
   - Port: 8084
   - User account creation
   - Kafka event publishing

3. **user-service-openapi.yaml** - User Management
   - Port: 8085
   - User data retrieval
   - Kafka event consumption

### Trading Services

4. **securities-pricing-service-openapi.yaml** - Security Prices
   - Port: 8081
   - 20 securities (stocks, indexes, bonds)
   - Live price updates every 30s

5. **currency-exchange-service-openapi.yaml** - Exchange Rates
   - Port: 8082
   - USD/EUR/GBP conversion rates
   - Rate updates every 60s

6. **fee-service-openapi.yaml** - Fee Calculations
   - Port: 8083
   - Trading fees (1% + $0.50)
   - Exchange fees (0.5% + $0.25)

7. **trading-service-openapi.yaml** - Trade Execution
   - Port: 8087
   - Buy/sell fractional shares
   - Order types: BY_AMOUNT, BY_QUANTITY

### Portfolio & Wallet Services

8. **wallet-service-openapi.yaml** - Multi-Currency Wallet
   - Port: 8086
   - Deposit/withdraw operations
   - Currency exchange

9. **portfolio-service-openapi.yaml** - Holdings Management
   - Port: 8088
   - Portfolio valuation
   - Average cost tracking

10. **transaction-history-service-openapi.yaml** - Audit Trail
    - Port: 8089
    - Complete transaction history
    - All operations with fees

## Viewing Documentation

### Option 1: Swagger UI (Recommended)

Each service has Swagger UI available at:
```
http://localhost:{PORT}/swagger-ui
```

Examples:
- API Gateway: http://localhost:8080/swagger-ui
- Trading Service: http://localhost:8087/swagger-ui
- Wallet Service: http://localhost:8086/swagger-ui

### Option 2: Online Viewers

Upload YAML files to:
- [Swagger Editor](https://editor.swagger.io/)
- [Redoc](https://redocly.github.io/redoc/)

### Option 3: VS Code Extensions

Install OpenAPI extensions:
- **Swagger Viewer** - Preview OpenAPI specs
- **OpenAPI (Swagger) Editor** - Edit and validate specs

## API Endpoints Summary

### User Flow
```
POST /api/v1/signup          → Register user
GET  /api/v1/users/{userId}  → Get user info
```

### Wallet Operations
```
POST /api/v1/wallet/{userId}/deposit         → Deposit funds
POST /api/v1/wallet/{userId}/withdraw        → Withdraw funds
POST /api/v1/wallet/{userId}/exchange        → Exchange currency
GET  /api/v1/wallet/{userId}/balances        → View balances
```

### Trading Operations
```
GET  /api/v1/securities                      → List all securities
GET  /api/v1/securities/{symbol}             → Get security details
POST /api/v1/trades/buy                      → Buy shares
POST /api/v1/trades/sell                     → Sell shares
```

### Portfolio & History
```
GET  /api/v1/portfolios/{userId}             → View portfolio
GET  /api/v1/transactions/{userId}           → Transaction history
```

## Authentication

**Current Status:** No authentication required (development mode)

**Future:** JWT bearer tokens will be required for all endpoints except `/api/v1/signup`

## Error Responses

All services follow RFC 7807 Problem Details format:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid currency code: XYZ"
}
```

## Rate Limiting

**Development:** No rate limiting

**Production:** Rate limits will be enforced at API Gateway level

## Data Formats

- **Money**: 2 decimal places (e.g., 100.50)
- **Quantity**: 2 decimal places (0.01 minimum)
- **Exchange Rates**: 6 decimal places
- **Dates**: ISO 8601 format (2026-01-11T16:17:00Z)
- **UUIDs**: Standard format (930b573b-7efe-4c21-b677-3358a6665cea)

## Regenerating Documentation

To update OpenAPI specs after code changes:

```bash
# 1. Ensure services are running
docker-compose ps

# 2. Generate fresh documentation
./gradlew generateOpenApiDocs

# 3. Verify generated files
ls -lh docs/openapi/*.yaml
```

## Support

For API questions or issues:
- Check Swagger UI for interactive testing
- Review TEST_REPORT.md for system status
- See docs/01_q_a.md for test cases and examples

---

**Last Generated:** 2026-01-11
**Version:** 1.0.0-SNAPSHOT
**OpenAPI Version:** 3.0.3
