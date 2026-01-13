# 03_dev.md - Developer Instructions for Step 03

**Role:** Developer
**Phase:** Step 03 - Implement Schema Isolation Architecture
**Date:** 2026-01-13
**From:** Senior Engineer
**Priority:** CRITICAL - System Non-Functional

---

## Overview

The Senior Engineer has diagnosed the root cause of the Flyway migration failures: **all microservices share the same PostgreSQL "public" schema**, violating microservice isolation principles.

**Architectural Decision:** Implement **separate database schemas per service** (schema-per-service pattern).

This is a **configuration-only change**. No code modifications required.

---

## What You're Fixing

### The Problem

All 6 database-backed services currently share the PostgreSQL "public" schema:

```
PostgreSQL Database: "trading"
└── Schema: "public" (SHARED - BROKEN)
    ├── fee_rules (created by fee-service)
    ├── users (should exist but causes conflicts)
    ├── wallet_balances (should exist but causes conflicts)
    └── ... other tables
```

**Startup sequence:**
1. fee-service starts first → creates `fee_rules` in "public" schema ✅
2. user-service starts → sees non-empty "public" schema → Flyway crashes ❌
3. wallet-service, trading-service, portfolio-service, transaction-history-service → all crash ❌

### The Solution

Each service gets its own isolated PostgreSQL schema:

```
PostgreSQL Database: "trading"
├── Schema: "user_service" (isolated)
│   ├── users
│   └── flyway_schema_history
├── Schema: "wallet_service" (isolated)
│   ├── wallet_balances
│   └── flyway_schema_history
├── Schema: "trading_service" (isolated)
│   ├── trades
│   └── flyway_schema_history
├── Schema: "portfolio_service" (isolated)
│   ├── holdings
│   └── flyway_schema_history
├── Schema: "transaction_history_service" (isolated)
│   ├── transactions
│   └── flyway_schema_history
└── Schema: "fee_service" (isolated)
    ├── fee_rules
    └── flyway_schema_history
```

**Benefits:**
- Each service sees its own empty schema on first start
- Flyway migrations run independently without conflicts
- Services can start in any order
- True microservice isolation
- Production-ready architecture

---

## Implementation Tasks

### Task 1: Update Service Configurations

You need to modify `application.properties` for **6 services**. Each service gets a dedicated schema.

#### Files to Modify

1. `services/user-service/src/main/resources/application.properties`
2. `services/wallet-service/src/main/resources/application.properties`
3. `services/trading-service/src/main/resources/application.properties`
4. `services/portfolio-service/src/main/resources/application.properties`
5. `services/transaction-history-service/src/main/resources/application.properties`
6. `services/fee-service/src/main/resources/application.properties`

#### Configuration Changes

For each service, locate the Flyway and datasource configuration section and make these changes:

**Current Configuration (Broken):**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history_{service_suffix}
# Note: baseline-on-migrate was removed in Step 02
```

**New Configuration (Working):**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading?currentSchema={schema_name}
quarkus.flyway.schemas={schema_name}
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history
```

#### Specific Configuration per Service

**1. user-service**

File: `services/user-service/src/main/resources/application.properties`

**Find these lines:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history_user
```

**Replace with:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading?currentSchema=user_service
quarkus.flyway.schemas=user_service
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history
```

---

**2. wallet-service**

File: `services/wallet-service/src/main/resources/application.properties`

**Find these lines:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history_wallet
```

**Replace with:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading?currentSchema=wallet_service
quarkus.flyway.schemas=wallet_service
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history
```

---

**3. trading-service**

File: `services/trading-service/src/main/resources/application.properties`

**Find these lines:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history_trading
```

**Replace with:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading?currentSchema=trading_service
quarkus.flyway.schemas=trading_service
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history
```

---

**4. portfolio-service**

File: `services/portfolio-service/src/main/resources/application.properties`

**Find these lines:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history_portfolio
```

**Replace with:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading?currentSchema=portfolio_service
quarkus.flyway.schemas=portfolio_service
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history
```

---

**5. transaction-history-service**

File: `services/transaction-history-service/src/main/resources/application.properties`

**Find these lines:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history_transaction
```

**Replace with:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading?currentSchema=transaction_history_service
quarkus.flyway.schemas=transaction_history_service
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history
```

---

**6. fee-service**

File: `services/fee-service/src/main/resources/application.properties`

**Find these lines:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history_fee
```

**Replace with:**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading?currentSchema=fee_service
quarkus.flyway.schemas=fee_service
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history
```

---

### Key Configuration Changes Explained

**1. JDBC URL with currentSchema:**
```properties
# Before:
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading

# After:
quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/trading?currentSchema=user_service
```
- Adds `?currentSchema={schema_name}` parameter
- Tells PostgreSQL which schema to use for this service
- Service will only see tables in its own schema

**2. Flyway schemas property:**
```properties
# New line:
quarkus.flyway.schemas=user_service
```
- Tells Flyway which schema to manage
- Flyway will create the schema if it doesn't exist
- Flyway will run migrations in this schema only

**3. Simplified flyway table name:**
```properties
# Before:
quarkus.flyway.table=flyway_schema_history_user

# After:
quarkus.flyway.table=flyway_schema_history
```
- No longer need unique suffix since each service has its own schema
- Cleaner configuration

---

## Task 2: Build and Test

### Step 1: Clean Build

```bash
./gradlew clean build -x test
```

**Expected Result:**
- BUILD SUCCESSFUL
- All 10 services compile without errors
- 102 actionable tasks executed

### Step 2: Clean Docker Environment

```bash
# Stop all containers and remove volumes (fresh database)
docker-compose down -v

# Remove any orphaned containers
docker-compose rm -f
```

**Why this is required:**
- The old "public" schema with mixed tables must be destroyed
- Fresh database will create new schemas correctly
- `-v` flag removes volumes (including PostgreSQL data)

### Step 3: Start System

```bash
docker-compose up --build
```

**Expected behavior:**
1. PostgreSQL starts
2. Services start (any order)
3. Each service creates its own schema automatically
4. Flyway runs migrations in each schema
5. All 15 containers reach healthy state

**Watch for these SUCCESS patterns in logs:**
```
user-service       | INFO  [org.flywaydb] Successfully validated 1 migration
user-service       | INFO  [org.flywaydb] Creating Schema History table "user_service"."flyway_schema_history"
user-service       | INFO  [org.flywaydb] Successfully applied 1 migration
```

**You should NOT see these ERROR patterns:**
```
# OLD ERROR (should not appear):
org.flywaydb.core.api.FlywayException: Found non-empty schema(s) "public" but no schema history table
```

### Step 4: Verify Container Status

```bash
docker-compose ps
```

**Expected result:**
```
NAME                    STATUS
api-gateway             Up
currency-exchange       Up
fee-service             Up
frontend                Up
kafka                   Up
portfolio-service       Up    ← Should be Up now (was crashed in Step 02)
postgres                Up
redis                   Up
securities-pricing      Up
trading-service         Up    ← Should be Up now (was crashed in Step 02)
transaction-history     Up    ← Should be Up now (was crashed in Step 02)
user-service            Up    ← Should be Up now (was crashed in Step 02)
user-signup-service     Up
wallet-service          Up    ← Should be Up now (was crashed in Step 02)
zookeeper               Up
```

**Critical:** All 15 containers must be "Up". If any show "Exit" or are missing, check logs.

---

## Task 3: Database Verification

### Verify Schemas Created

```bash
# Connect to PostgreSQL
docker exec -it postgres psql -U trading -d trading

# List all schemas
\dn
```

**Expected output:**
```
        List of schemas
          Name               | Owner
-----------------------------+--------
 fee_service                 | trading
 portfolio_service           | trading
 transaction_history_service | trading
 trading_service             | trading
 user_service                | trading
 wallet_service              | trading
 public                      | postgres
```

**Verify:** 6 service-specific schemas exist (plus default "public" schema)

### Verify Tables in Each Schema

```sql
-- Check user_service schema
SET search_path TO user_service;
\dt

-- Expected output:
--   users
--   flyway_schema_history

-- Check wallet_service schema
SET search_path TO wallet_service;
\dt

-- Expected output:
--   wallet_balances
--   flyway_schema_history

-- Check trading_service schema
SET search_path TO trading_service;
\dt

-- Expected output:
--   trades
--   flyway_schema_history

-- Check portfolio_service schema
SET search_path TO portfolio_service;
\dt

-- Expected output:
--   holdings
--   flyway_schema_history

-- Check transaction_history_service schema
SET search_path TO transaction_history_service;
\dt

-- Expected output:
--   transactions
--   flyway_schema_history

-- Check fee_service schema
SET search_path TO fee_service;
\dt

-- Expected output:
--   fee_rules
--   flyway_schema_history

-- Exit psql
\q
```

**Verify:** Each schema has its application table(s) plus `flyway_schema_history`

### Verify Public Schema is Empty

```bash
docker exec -it postgres psql -U trading -d trading -c "\dt public.*"
```

**Expected result:**
```
Did not find any relation named "public.*".
```

OR only PostgreSQL system tables (no application tables like users, wallet_balances, etc.)

**Important:** Application tables should NOT be in "public" schema anymore.

---

## Task 4: Functional Verification

### Test 1: User Registration (TC-001)

```bash
# Create a test user
curl -X POST http://localhost:8080/api/v1/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.step03@example.com",
    "username": "test_step03",
    "phoneNumber": "+1234567890"
  }'
```

**Expected response:**
```json
{
  "userId": "some-uuid-here",
  "email": "test.step03@example.com",
  "username": "test_step03",
  "phoneNumber": "+1234567890"
}
```

**Verify in database:**
```bash
docker exec -it postgres psql -U trading -d trading -c \
  "SELECT * FROM user_service.users WHERE email = 'test.step03@example.com';"
```

**Expected:** User exists in `user_service.users` table

### Test 2: Service Logs Clean

```bash
# Check for Flyway errors (should return nothing)
docker-compose logs | grep -i "FlywayException"

# Check for successful migrations (should show 6 services)
docker-compose logs | grep -i "Successfully applied"
```

**Expected:**
- No FlywayException errors
- 6 services showing "Successfully applied 1 migration" (or similar)

---

## Task 5: Documentation Update

### Update 03_discussion.md

Append your implementation notes:

```markdown
## [As Developer] Implementation Completed - Step 03

**Date:** 2026-01-13

### Changes Made

Updated `application.properties` for 6 services with schema isolation:
1. user-service → schema: user_service
2. wallet-service → schema: wallet_service
3. trading-service → schema: trading_service
4. portfolio-service → schema: portfolio_service
5. transaction-history-service → schema: transaction_history_service
6. fee-service → schema: fee_service

**Configuration changes:**
- Added `?currentSchema={schema_name}` to JDBC URL
- Added `quarkus.flyway.schemas={schema_name}` property
- Simplified `flyway.table` to just "flyway_schema_history"

### Test Results

✅ All 15 containers running
✅ All 6 schemas created in PostgreSQL
✅ All application tables created in correct schemas
✅ No Flyway exceptions in logs
✅ User registration test passed (TC-001)

**System Status:** Functional and ready for Q/A testing
```

---

## Definition of Done

Before handing off to Q/A, verify ALL checkboxes:

### Configuration
- [ ] `user-service/application.properties` updated with `currentSchema=user_service`
- [ ] `wallet-service/application.properties` updated with `currentSchema=wallet_service`
- [ ] `trading-service/application.properties` updated with `currentSchema=trading_service`
- [ ] `portfolio-service/application.properties` updated with `currentSchema=portfolio_service`
- [ ] `transaction-history-service/application.properties` updated with `currentSchema=transaction_history_service`
- [ ] `fee-service/application.properties` updated with `currentSchema=fee_service`
- [ ] All 6 services have `quarkus.flyway.schemas` property set
- [ ] All 6 services have simplified `flyway.table=flyway_schema_history`

### Build & Deploy
- [ ] Gradle build succeeds: `./gradlew clean build -x test`
- [ ] Docker Compose clean start: `docker-compose down -v && docker-compose up --build`
- [ ] All 15 containers reach "Up" status
- [ ] No container restart loops or crashes

### Database
- [ ] 6 schemas exist in PostgreSQL (user_service, wallet_service, trading_service, portfolio_service, transaction_history_service, fee_service)
- [ ] Each schema contains expected application table(s)
- [ ] Each schema contains `flyway_schema_history` table
- [ ] Public schema does NOT contain application tables
- [ ] Total application tables: 6 (users, wallet_balances, trades, holdings, transactions, fee_rules)

### Logs
- [ ] No "FlywayException" errors in any service logs
- [ ] All 6 services show "Successfully applied" migration messages
- [ ] No persistent Kafka connection errors (initial warnings OK, should resolve)

### Functional
- [ ] User registration works (POST /api/v1/signup returns 201 with UUID)
- [ ] User persisted to `user_service.users` table
- [ ] API Gateway health check returns UP (http://localhost:8080/q/health)

### Documentation
- [ ] `docs/03_discussion.md` updated with implementation notes
- [ ] `IMPLEMENTATION_STATUS.md` ready for Q/A update

---

## Troubleshooting

### Issue: Service fails to start with schema error

**Check:**
```bash
docker-compose logs {service-name} | tail -50
```

**Common causes:**
- Typo in schema name (must match exactly: `user_service` not `user-service`)
- Old database data from previous run (solution: `docker-compose down -v`)
- Missing `quarkus.flyway.schemas` property

### Issue: Schema not created

**Verify Flyway is enabled:**
```bash
docker-compose logs {service-name} | grep -i flyway
```

**Should see:**
```
INFO  [org.flywaydb] Flyway Community Edition
INFO  [org.flywaydb] Creating Schema History table
```

**If missing:** Check that `quarkus.flyway.migrate-at-start=true` is present

### Issue: Tables in wrong schema

**Check current database state:**
```bash
docker exec -it postgres psql -U trading -d trading -c "
  SELECT schemaname, tablename
  FROM pg_tables
  WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
  ORDER BY schemaname, tablename;"
```

**Solution:** Clean start required
```bash
docker-compose down -v
docker-compose up --build
```

### Issue: Gradle build fails

**Error:** Configuration property not recognized

**Cause:** Typo in property name

**Fix:** Property names are case-sensitive:
- ✅ `quarkus.flyway.schemas`
- ❌ `quarkus.flyway.schema` (missing 's')

---

## Notes from Senior Engineer

### Why This Approach?

**Microservice Isolation Principle:**
- Each service owns its data completely
- Services cannot accidentally access each other's tables
- Clear schema ownership: schema name = service name

**Production Readiness:**
- Works correctly in Kubernetes (no startup order dependencies)
- Supports future migration to separate database instances
- Standard pattern for microservices architecture

**Flyway Benefits:**
- Each service sees empty schema on first run
- Migrations run independently without conflicts
- No baseline workarounds needed

### Cross-Service Data Access

**Anti-Pattern (Forbidden):**
```sql
-- Service A directly querying Service B's schema
SELECT * FROM wallet_service.wallet_balances WHERE user_id = ?;
```

**Correct Pattern (Use APIs or Events):**
```java
// Option 1: REST API call
WalletBalance balance = walletServiceClient.getBalance(userId);

// Option 2: Kafka events
@Incoming("wallet-events")
public void handleWalletUpdate(WalletEvent event) { ... }
```

### Future Scalability

This pattern enables:
1. **Database Splitting:** Move schema to separate PostgreSQL instance (change connection string only)
2. **Read Replicas:** Add read-only replicas per schema
3. **Multi-Tenancy:** Add tenant_id partitioning within schemas
4. **Cloud Migration:** Works with AWS RDS, Google Cloud SQL, etc.

---

## Success Criteria Summary

**System is ready for Q/A when:**
1. ✅ All 15 containers running healthy
2. ✅ All 6 schemas created with correct tables
3. ✅ No Flyway errors in any logs
4. ✅ User registration end-to-end flow works

**Next Phase:** Q/A will execute full regression test suite (TC-001 through TC-015)

---

## Questions?

If you encounter issues not covered in this document:
1. Check service logs: `docker-compose logs {service-name}`
2. Verify database state using psql commands above
3. Ensure clean start with `docker-compose down -v`
4. Review Senior Engineer's architectural decision in docs/03_se.md

---

**[As Senior Engineer]** This is a clean architectural fix that resolves the root cause permanently. The implementation is straightforward—6 configuration files with clear patterns. Once complete, the system will be production-ready with proper microservice isolation.

Good luck with the implementation!
