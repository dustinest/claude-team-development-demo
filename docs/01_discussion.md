# 01_discussion.md - Product Owner Q/A Session

## Project Vision & Requirements Discussion

This document captures the complete Q/A session between the Product Owner (PO) and the Operator to establish the project vision and requirements.

---

## Q/A Session

### 1. Project Vision & Purpose

**[As Product Owner]** What is the main purpose of this Java project? What problem does it solve? Who are the intended users or target audience? What is the primary value this project will deliver?

**[Operator Response]**
- The purpose of this Java project is to make a stock exchange project
- The intended user and target audience are investors being able to buy fractions of stocks. Instead of buying one stock or index with a large amount of money, customers can buy only, let's say, only 0.1. Be careful here with fractional calculations - they might cause problems with calculations. For that, we limit to 2 digits. And also do calculations in favor of the customers (our fee % should cover it)
- The primary value is to help young investors

### 2. Project Scope

**[As Product Owner]** What are the core features or functionality you envision for this project? Are there any specific use cases or scenarios you want to support? What should be included in the initial version?

**[Operator Response]**
- Core features are stock exchange, currency exchange (we should have a multi-currency environment), simple user management (no need to have a full profile, just their credentials, which in the future we will fill with authentication: phone number/email/username)
- Mocked sign-up service which broadcasts new user credentials (email, username, phone number) which is a unique user identifier. All next steps will use user's ID though.
- Authentication and authorization are not in the scope. User-id can be part of the REST or messaging parameters
- There should be a mock currency providing service and mocked securities providing service as we have not decided yet which external service we will use

### 3. Constraints & Requirements

**[As Product Owner]** Are there any specific technical constraints? Do you have preferences for deployment? Are there performance, security, or scalability requirements?

**[Operator Response]**
- Java 21
- Groovy with Kotlin
- We will use lambdas, so Quarkus should be considered as the best implementation framework
- Microservices - each business functionality should have its own microservice. The best internal communication channel is messaging (Kafka?) and integration with currency and securities services is REST
- All services should be runnable in Docker; it also might help Q/A
- No security concerns at the moment, but services should be scalable, which would be supported by lambda/microservice architecture

### 4. Success Criteria

**[As Product Owner]** How will we know when this project is successful? What are the key acceptance criteria?

**[Operator Response]**
The project is successful when:
- Many users can register with their credentials (username, email, phone number - see the mocked service)
- User can request list of securities in their chosen currency and see the price
- User can request single security price in their chosen currency
- User can buy and sell security fractions in their chosen currency
  - Either giving the amount in money, so the amount of securities is calculated
  - Or giving the amount of securities
- User can see their portfolio in their chosen currency

### 5. User Account & Funds Management

**[As Product Owner]** How do users fund their accounts initially? Do we need a "deposit" feature? Should each user have a wallet/balance per currency? Do we need to track cash balances separately from securities holdings? Any withdrawal functionality needed?

**[Operator Response]**
- Yes, we need deposit and withdrawal functionality, basically a microservice to manage user wallet
- User should have balance per currency, as currency exchange might change. We have to keep their amounts in original currency, so we can convert on demand
- Every buy and sell should have its own transaction, so we could show those to customers

### 6. Currency Details

**[As Product Owner]** Which currencies should we support initially? Does each user have a "preferred" or "base" currency? When viewing portfolio in "chosen currency" - can they switch between any supported currency on-the-fly?

**[Operator Response]**
- USD, EUR, and GBP are sufficient for initial scope
- User does not have a preferred currency. They should be in control of deposit, withdrawal accounts and which currency to use while buying and selling.
- When viewing portfolio in "chosen currency" - the amounts should be calculated on the fly

### 7. Fee Structure

**[As Product Owner]** You mentioned "our fee % should cover" the fractional calculation rounding - what is the fee structure? Is there a percentage fee per transaction? Should fees be clearly shown to users before confirming trades?

**[Operator Response]**
- We have fees for buying and selling. It is a fixed fee (can also be 0), and % (also might be 0). For that, we need a separate microservice which decides based on the security if there is a base fee(s) and a percent
- We have fees for each security (based on the security market), also for currency exchange
- The fees should be clearly shown. We are being honest

### 8. Portfolio & Transaction Tracking

**[As Product Owner]** Should we maintain transaction history? What details should the portfolio show? Do we need to track average purchase price per security?

**[Operator Response]**
- User should be able to request their transaction history: all buys, sells, deposits, withdrawals, and currency exchanges. Including all fees applied
- User portfolio should show simply holdings, current value, profit/loss based on the date range (day, week, month, year, and YTD). Later we add more info
- We need to track average purchase price per security

### 9. Mock Services Behavior

**[As Product Owner]** For the user signup mock service: Should it generate a unique user-id and broadcast credentials? For the securities service mock: Should it provide a static list of securities, or realistic price fluctuations? For the currency exchange mock: Should it provide realistic exchange rates?

**[Operator Response]**
- For the user signup mock service: It should generate a unique user ID. No validation required for now, except user ID should be UUID and credentials should be unique.
- For the securities service mock: Static list of securities is fine. Choose 10 of the most popular stocks, 5 stock indexes, and 5 bond indexes with real-like, but randomized values. So, give some realistic price fluctuations.
- For the currency exchange mock: Provide realistic exchange rates or static rates which change over time.

### 10. Trading Mechanics

**[As Product Owner]** Are all trades executed immediately (market orders), or do we need limit orders/pending orders? When a user buys with "amount in money" - do we execute at current market price immediately? Any minimum trade amounts or restrictions?

**[Operator Response]**
- All trades are for now executed automatically. No limit or pending orders.
- When a user buys with "amount in money" - we execute at current market price immediately.
- No minimum trade amounts or restrictions for now. They might be applied later.

### 11. Data Persistence

**[As Product Owner]** All user data, portfolios, transactions - should be persisted in databases, correct? Any preferences for database technology?

**[Operator Response]**
- All user data, portfolios, transactions should be persisted in databases.
- Let Senior Engineer decide the database technology. But remind them, it should be runnable in Docker, and later in AWS as natively as possible.

### 12. Interface

**[As Product Owner]** Is this purely backend REST APIs and messaging, or do we need any UI? Are we building APIs that will be consumed by a future frontend application?

**[Operator Response]**
- We need a simple UI for this project.
- Yes, we are building APIs that will be consumed by a future frontend application. Senior Engineer should also take care of API documentation requirements.

### 13. Technology Clarification

**[As Product Owner]** You mentioned "groovy with kotlin" - do you mean we should use both Groovy AND Kotlin, or either/or, or something specific?

**[Operator Response]**
- Groovy with Kotlin, code is in Java

**[As Product Owner]** Just to be crystal clear: Production code should be written in Java 21, correct? Tests should be written in Groovy and/or Kotlin, correct?

**[Operator Response]**
- Yes, production code should be written in Java 21. Tests should be written in Spock/Groovy, and build tool is Gradle with Kotlin (sorry for confusion about Groovy).

### 14. UI Scope Clarification

**[As Product Owner]** You mentioned "we need a simple UI" but also "building APIs for future frontend application" - should we build a simple UI as part of THIS initial project (Step 01)? Or focus on backend APIs first?

**[Operator Response]**
- I let it for you to decide. If it is low-hanging fruit, then it would be good to have a simple React application for frontend and backend. Otherwise, we can implement it in the future.

### 15. Currency Exchange Transactions

**[As Product Owner]** Should users be able to explicitly exchange currency (e.g., convert EUR to USD in their wallet)? Or do currency conversions only happen implicitly when buying/selling securities?

**[Operator Response]**
- Users should be able to convert EUR to USD in their wallet
- For now, we do not automatically convert on buy and sell. This is for the next step if necessary.

### 16. Microservices Breakdown

**[As Product Owner]** Based on your requirements, I'm envisioning these microservices - does this align with your vision?
- User Service - handles user signup and user data
- Wallet Service - manages deposits, withdrawals, and currency balances
- Trading Service - handles buy/sell orders
- Portfolio Service - tracks holdings and calculates portfolio values
- Transaction History Service - records and retrieves all transactions
- Fee Service - determines fees for securities and currency exchanges
- Securities Pricing Service (mock) - provides security prices
- Currency Exchange Service (mock) - provides exchange rates
- User Signup Service (mock) - broadcasts new user credentials

**[Operator Response]**
- I think it aligns with my microservices vision indeed.

---

## Key Decisions Summary

### Technical Stack
- **Language**: Java 21 (production code)
- **Testing**: Spock/Groovy
- **Build Tool**: Gradle with Kotlin DSL
- **Framework**: Quarkus (optimized for lambdas)
- **Architecture**: Microservices
- **Messaging**: Kafka (suggested for internal communication)
- **Integration**: REST for external services
- **Deployment**: Docker containers
- **Database**: Senior Engineer to decide (must be Docker-friendly and AWS-native)

### Business Requirements
- **Target Users**: Young investors
- **Core Value**: Fractional stock trading (minimum 0.01, maximum 2 decimal places)
- **Currencies**: USD, EUR, GBP
- **Securities**: 10 stocks, 5 stock indexes, 5 bond indexes (mocked with realistic fluctuations)
- **Trading**: Immediate execution, market orders only
- **Fees**: Fixed + percentage per security and currency exchange
- **Rounding**: Always in favor of customer

### Functional Requirements
1. User registration (mocked service, UUID-based user IDs)
2. Wallet management (deposit, withdrawal, multi-currency balances)
3. Currency exchange (explicit conversion in wallet)
4. Security trading (buy/sell fractions by amount or money)
5. Portfolio view (holdings, current value, profit/loss with date ranges)
6. Transaction history (all operations with fees)
7. Security pricing (list and individual, in chosen currency)
8. Fee calculation (transparent display)

### Out of Scope (Initial Version)
- Authentication/authorization
- User profiles (beyond credentials)
- Limit orders / pending orders
- Automatic currency conversion on trades
- Minimum trade restrictions

### UI Decision
- Optional: If low-hanging fruit, implement simple React frontend
- Otherwise: Focus on backend APIs with proper documentation for future frontend integration

---

## Senior Engineer Architectural Analysis

### Architecture Overview

**[As Senior Engineer]** After reviewing the requirements, I'm proposing a microservices architecture with event-driven communication and a choreography-based saga pattern for complex transactions.

### System Architecture Decisions

#### 1. Microservices Structure

**Core Services (10 total):**

1. **API Gateway Service**
   - Entry point for all client requests
   - Routes to appropriate microservices
   - Request/response aggregation
   - Built with Quarkus REST

2. **User Service**
   - Manages user data (UUID, email, username, phone)
   - Listens to user-created events from signup service
   - PostgreSQL database

3. **Wallet Service**
   - Multi-currency balance management
   - Deposit/withdrawal operations
   - Currency exchange operations
   - PostgreSQL database (ACID guarantees critical)
   - Publishes: wallet-updated, deposit-completed, withdrawal-completed, currency-exchanged events

4. **Trading Service (Orchestrator)**
   - Orchestrates buy/sell operations (Saga pattern)
   - Validates trading requests
   - Coordinates with wallet, portfolio, fee, pricing services
   - PostgreSQL database (transaction records)
   - Publishes: trade-initiated, trade-completed, trade-failed events

5. **Portfolio Service**
   - Tracks user holdings per security
   - Calculates portfolio value, profit/loss
   - Listens to: trade-completed events
   - PostgreSQL database

6. **Transaction History Service**
   - Records all financial operations
   - Query interface for transaction history
   - PostgreSQL database (append-only, audit trail)
   - Listens to: all financial events

7. **Fee Service**
   - Fee calculation logic
   - Fee structure per security and currency pair
   - PostgreSQL database (fee rules)
   - Stateless service (no events)

8. **Securities Pricing Service (Mock)**
   - Provides security prices with realistic fluctuations
   - 10 stocks, 5 stock indexes, 5 bond indexes
   - In-memory storage with scheduled price updates
   - Redis cache for performance
   - Publishes: price-updated events (optional)

9. **Currency Exchange Service (Mock)**
   - Provides exchange rates for USD, EUR, GBP
   - Realistic rates with periodic changes
   - In-memory storage with scheduled rate updates
   - Redis cache for performance

10. **User Signup Service (Mock)**
    - Generates UUID for new users
    - Broadcasts user-created events
    - In-memory storage
    - Publishes: user-created events

#### 2. Communication Patterns

**Synchronous Communication (REST):**
- Frontend → API Gateway → Services (for queries and commands requiring immediate response)
- Service-to-service for critical reads (Trading → Fee, Trading → Pricing, Trading → Currency Exchange)

**Asynchronous Communication (Kafka):**

**Topics:**
- `user-events` - user-created, user-updated
- `wallet-events` - wallet-updated, deposit-completed, withdrawal-completed, currency-exchanged
- `trading-events` - trade-initiated, trade-completed, trade-failed
- `portfolio-events` - portfolio-updated
- `transaction-events` - transaction-recorded

**Event Flow Example (Buy Order):**
1. Client → API Gateway → Trading Service (REST: POST /api/trades/buy)
2. Trading Service orchestrates:
   - REST call to Securities Pricing Service (get current price)
   - REST call to Fee Service (calculate fees)
   - REST call to Currency Exchange Service (if currency conversion needed)
   - REST call to Wallet Service (validate and reserve funds)
   - If successful: Update own database, publish trade-completed event
   - If failed: Publish trade-failed event with reason
3. Portfolio Service listens to trade-completed → updates holdings
4. Transaction History Service listens to trade-completed → records transaction
5. Wallet Service listens to trade-completed → finalizes fund deduction
6. Response sent back to client through API Gateway

**Saga Pattern (Choreography-Based):**
- Each service is responsible for its own local transaction
- Services listen to events and react accordingly
- Compensating transactions if needed (trade-failed → release reserved funds)

#### 3. Database Selection

| Service | Database | Rationale |
|---------|----------|-----------|
| User Service | PostgreSQL | Relational data, ACID guarantees |
| Wallet Service | PostgreSQL | Financial data, ACID critical, multi-currency balances |
| Trading Service | PostgreSQL | Transaction records, ACID guarantees |
| Portfolio Service | PostgreSQL | Holdings tracking, calculations |
| Transaction History Service | PostgreSQL | Audit trail, append-only pattern |
| Fee Service | PostgreSQL | Fee rules, relatively static data |
| Securities Pricing (Mock) | In-Memory + Redis Cache | Mock data, high read volume |
| Currency Exchange (Mock) | In-Memory + Redis Cache | Mock data, high read volume |
| User Signup (Mock) | In-Memory | Mock service, transient data |

**Docker Setup:**
- PostgreSQL container (shared for development, separate in production)
- Redis container
- Kafka + Zookeeper containers

**AWS Compatibility:**
- PostgreSQL → AWS RDS PostgreSQL
- Redis → AWS ElastiCache
- Kafka → AWS MSK (Managed Streaming for Kafka)
- Services → AWS Lambda (Quarkus native compilation)

#### 4. API Design

**REST API Standards:**
- Base path: `/api/v1`
- HTTP methods: GET, POST, PUT, DELETE
- Response format: JSON
- Error format: RFC 7807 Problem Details
- Pagination: `?page=0&size=20`
- Filtering: Query parameters

**API Documentation:**
- OpenAPI 3.0 specification
- Quarkus Swagger UI extension
- Auto-generated from JAX-RS annotations

**Key API Endpoints (via API Gateway):**

**User Management:**
- POST `/api/v1/users` - Register user (delegates to signup service)
- GET `/api/v1/users/{userId}` - Get user details

**Wallet:**
- GET `/api/v1/wallets/{userId}/balances` - Get all currency balances
- POST `/api/v1/wallets/{userId}/deposit` - Deposit funds
- POST `/api/v1/wallets/{userId}/withdraw` - Withdraw funds
- POST `/api/v1/wallets/{userId}/exchange` - Exchange currency

**Trading:**
- GET `/api/v1/securities` - List all securities with prices
- GET `/api/v1/securities/{symbol}` - Get single security price
- POST `/api/v1/trades/buy` - Execute buy order
- POST `/api/v1/trades/sell` - Execute sell order
- GET `/api/v1/trades/{tradeId}` - Get trade details

**Portfolio:**
- GET `/api/v1/portfolios/{userId}` - Get user portfolio
- GET `/api/v1/portfolios/{userId}/value?currency=USD` - Get portfolio value in currency
- GET `/api/v1/portfolios/{userId}/profit-loss?period=MONTH` - Get profit/loss

**Transaction History:**
- GET `/api/v1/transactions/{userId}` - Get transaction history
- GET `/api/v1/transactions/{userId}?type=BUY&from=2024-01-01&to=2024-12-31` - Filtered history

**Fees:**
- GET `/api/v1/fees/trading/{symbol}` - Get trading fees for security
- GET `/api/v1/fees/exchange?from=USD&to=EUR` - Get currency exchange fees

#### 5. Data Models

**Core Entities:**

**User:**
```json
{
  "userId": "uuid",
  "email": "string (unique)",
  "username": "string (unique)",
  "phoneNumber": "string (unique)",
  "createdAt": "timestamp"
}
```

**Wallet Balance:**
```json
{
  "walletId": "uuid",
  "userId": "uuid",
  "currency": "USD|EUR|GBP",
  "balance": "decimal(19,2)",
  "updatedAt": "timestamp"
}
```

**Trade:**
```json
{
  "tradeId": "uuid",
  "userId": "uuid",
  "symbol": "string",
  "tradeType": "BUY|SELL",
  "orderType": "BY_AMOUNT|BY_QUANTITY",
  "quantity": "decimal(19,2)",
  "pricePerUnit": "decimal(19,2)",
  "currency": "USD|EUR|GBP",
  "totalAmount": "decimal(19,2)",
  "fees": "decimal(19,2)",
  "status": "PENDING|COMPLETED|FAILED",
  "createdAt": "timestamp",
  "completedAt": "timestamp"
}
```

**Holding:**
```json
{
  "holdingId": "uuid",
  "userId": "uuid",
  "symbol": "string",
  "quantity": "decimal(19,2)",
  "averagePrice": "decimal(19,2)",
  "currency": "USD|EUR|GBP",
  "updatedAt": "timestamp"
}
```

**Transaction:**
```json
{
  "transactionId": "uuid",
  "userId": "uuid",
  "type": "DEPOSIT|WITHDRAWAL|BUY|SELL|CURRENCY_EXCHANGE",
  "currency": "USD|EUR|GBP",
  "amount": "decimal(19,2)",
  "fees": "decimal(19,2)",
  "relatedEntityId": "uuid (optional, e.g., tradeId)",
  "metadata": "json",
  "createdAt": "timestamp"
}
```

**Security:**
```json
{
  "symbol": "string",
  "name": "string",
  "type": "STOCK|STOCK_INDEX|BOND_INDEX",
  "currentPrice": "decimal(19,2)",
  "currency": "USD",
  "lastUpdated": "timestamp"
}
```

**Exchange Rate:**
```json
{
  "fromCurrency": "USD|EUR|GBP",
  "toCurrency": "USD|EUR|GBP",
  "rate": "decimal(19,6)",
  "lastUpdated": "timestamp"
}
```

#### 6. Fractional Calculation Strategy

**Precision Handling:**
- Use `java.math.BigDecimal` throughout
- Scale: 2 decimal places for quantities and amounts
- Scale: 6 decimal places for exchange rates
- Rounding mode: `RoundingMode.HALF_UP` as default
- **Customer-favorable rounding:** When calculating fees or amounts, always round in customer's favor

**Example Buy Calculation (By Money):**
```
Given: User wants to buy $100 of AAPL, current price $150.00, fee 1% + $0.50
1. Subtract fees from amount: $100 - $0.50 = $99.50
2. Apply percentage fee: $99.50 / 1.01 = $98.51 (customer gets to invest this amount)
3. Calculate quantity: $98.51 / $150.00 = 0.656733... → 0.66 shares (round up, customer favor)
4. Actual cost: 0.66 * $150.00 = $99.00
5. Total with fees: $99.00 * 1.01 + $0.50 = $100.49
6. Charge customer: $100.00 (original amount, we absorb $0.49 difference)
```

**Example Sell Calculation (By Quantity):**
```
Given: User wants to sell 0.50 shares of AAPL, current price $150.00, fee 1% + $0.50
1. Calculate gross amount: 0.50 * $150.00 = $75.00
2. Apply fees: $75.00 - ($75.00 * 0.01) - $0.50 = $73.75
3. Credit customer: $73.75 (rounded up to nearest cent if needed)
```

#### 7. Mock Service Implementation

**Securities Pricing Service:**
- **Stocks (10):** AAPL, GOOGL, MSFT, AMZN, TSLA, META, NVDA, JPM, JNJ, V
- **Stock Indexes (5):** SPY (S&P 500), QQQ (NASDAQ), DIA (Dow Jones), IWM (Russell 2000), VTI (Total Market)
- **Bond Indexes (5):** AGG (US Aggregate), TLT (20+ Year Treasury), BND (Total Bond), LQD (Investment Grade), HYG (High Yield)
- **Price Generation:** Seed with realistic base prices, add random fluctuation ±2% every 30 seconds
- **Storage:** In-memory ConcurrentHashMap, scheduled task updates prices

**Currency Exchange Service:**
- **Rates (as of design time):**
  - USD/EUR: 0.92
  - USD/GBP: 0.79
  - EUR/USD: 1.09
  - EUR/GBP: 0.86
  - GBP/USD: 1.27
  - GBP/EUR: 1.16
- **Fluctuation:** ±0.5% every 60 seconds
- **Storage:** In-memory ConcurrentHashMap

**User Signup Service:**
- **Function:** Receives signup request, generates UUID, publishes user-created event
- **Validation:** Email/username/phone uniqueness (check with User Service)
- **Storage:** In-memory (transient)

#### 8. UI Decision

**Decision: Implement Simple React UI**

**Rationale:**
- Demonstrates platform functionality immediately
- Enables comprehensive Q/A testing
- Relatively low effort for basic flows
- Provides value for stakeholder demonstrations

**Technology Stack:**
- **Framework:** React 18 with TypeScript
- **Build Tool:** Vite (faster than Create React App)
- **State Management:** React Context API (lightweight, no Redux needed initially)
- **HTTP Client:** Axios
- **UI Library:** Material-UI (MUI) for rapid development
- **Routing:** React Router v6

**Key Pages:**
1. **Signup Page** - User registration
2. **Wallet Page** - View balances, deposit, withdraw, currency exchange
3. **Market Page** - List securities, view prices, filter by type
4. **Trading Page** - Buy/sell securities
5. **Portfolio Page** - View holdings, profit/loss
6. **Transactions Page** - Transaction history with filters

**Deployment:**
- Separate Docker container for React app
- Nginx to serve static files
- Reverse proxy to API Gateway

#### 9. Docker & Local Development

**Docker Compose Structure:**
```yaml
services:
  postgres:
    image: postgres:15-alpine
  redis:
    image: redis:7-alpine
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
  kafka:
    image: confluentinc/cp-kafka:7.5.0
  api-gateway:
    build: ./api-gateway
  user-service:
    build: ./user-service
  wallet-service:
    build: ./wallet-service
  trading-service:
    build: ./trading-service
  portfolio-service:
    build: ./portfolio-service
  transaction-history-service:
    build: ./transaction-history-service
  fee-service:
    build: ./fee-service
  securities-pricing-service:
    build: ./securities-pricing-service
  currency-exchange-service:
    build: ./currency-exchange-service
  user-signup-service:
    build: ./user-signup-service
  frontend:
    build: ./frontend
    ports:
      - "3000:80"
```

**Development Workflow:**
- Each service has its own Gradle project
- Root project aggregates all services
- Docker Compose for integration testing
- Individual services can run standalone with dev mode (Quarkus dev mode)

#### 10. Observability

**Logging:**
- Structured JSON logging (SLF4J with Logback)
- Correlation IDs for request tracing
- Log levels: INFO (default), DEBUG (dev), ERROR (production)

**Health Checks:**
- Quarkus SmallRye Health extension
- Liveness: `/q/health/live`
- Readiness: `/q/health/ready`

**Metrics:**
- Quarkus Micrometer extension
- Prometheus endpoint: `/q/metrics`
- Key metrics: request count, duration, error rate, database connection pool

**Distributed Tracing (Optional for Step 01):**
- Can add Jaeger in future iterations
- Quarkus OpenTelemetry extension ready

### Technology Summary

| Category | Technology | Purpose |
|----------|-----------|---------|
| Language | Java 21 | Production code |
| Testing | Spock/Groovy | Unit & integration tests |
| Build Tool | Gradle (Kotlin DSL) | Build automation |
| Framework | Quarkus | Microservices framework |
| Messaging | Apache Kafka | Event-driven communication |
| Database | PostgreSQL 15 | Primary data store |
| Cache | Redis 7 | Caching layer |
| API Docs | OpenAPI 3 / Swagger UI | API documentation |
| Frontend | React 18 + TypeScript | User interface |
| UI Framework | Material-UI (MUI) | UI components |
| Containerization | Docker | Service deployment |
| Orchestration | Docker Compose | Local development |

### Implementation Priority

**Phase 1 (Step 01): Core Infrastructure & Mock Services**
1. Project structure and Gradle multi-module setup
2. Docker Compose configuration
3. Kafka setup
4. Mock services (Securities Pricing, Currency Exchange, User Signup)
5. Fee Service (simple in-memory fee rules)

**Phase 2 (Step 01): Core Services**
6. User Service
7. Wallet Service
8. Trading Service (orchestration logic)
9. Portfolio Service
10. Transaction History Service

**Phase 3 (Step 01): API & Frontend**
11. API Gateway
12. React Frontend (basic flows)

**Phase 4 (Step 01): Testing & Documentation**
13. Integration tests
14. API documentation
15. End-to-end testing

---

## Next Steps for Developer

The Developer should now implement the system following the architectural design above. Detailed instructions are provided in `01_dev.md`.
