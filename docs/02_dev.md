# 02_dev.md - Bug Fix Instructions for Developer

**Iteration:** Step 02 - Bug Fix Loop
**Date:** 2026-01-12
**From:** Q/A Specialist
**To:** Developer
**Status:** CRITICAL BUGS - IMMEDIATE ACTION REQUIRED

---

## Executive Summary

Q/A testing of Step 01 implementation has **FAILED** due to **2 critical bugs** that prevent basic system functionality. Testing was blocked at TC-002 (only 2 of 15 test cases attempted).

**Impact:** System is non-functional. No user operations work.

---

## Critical Bugs to Fix

### 🔴 BUG #1: Flyway Migration Failure (HIGHEST PRIORITY)

**File:** `TEST_REPORT.md` - Line 87-134
**Severity:** CRITICAL - System Non-Functional
**Impact:** All application database tables are missing

#### Problem

Configuration `quarkus.flyway.baseline-on-migrate=true` causes Flyway to skip all migrations on a fresh database. Instead of executing V1__*.sql migrations, Flyway baselines to version 1 and reports "Schema is up to date".

**Result:** Only Flyway history tables exist. Application tables (users, wallet_balances, trades, holdings, transactions) are missing.

#### Evidence from Q/A Testing

```sql
postgres=# \dt
                      List of relations
 Schema |               Name                | Type  |  Owner
--------+-----------------------------------+-------+---------
 public | fee_rules                         | table | trading
 public | flyway_schema_history_fee         | table | trading
 public | flyway_schema_history_portfolio   | table | trading
 public | flyway_schema_history_trading     | table | trading
 public | flyway_schema_history_transaction | table | trading
 public | flyway_schema_history_user        | table | trading
 public | flyway_schema_history_wallet      | table | trading
(7 rows)

-- Missing: users, wallet_balances, trades, holdings, transactions tables
```

#### Root Cause

The `baseline-on-migrate` flag was added during Step 01 to resolve Flyway schema history conflicts between services. However, this creates a new problem: on a **fresh database**, Flyway baselines to V1 without running any migrations.

From Flyway documentation:
> baseline-on-migrate: Automatically calls baseline when migrate is executed against a non-empty schema with no schema history table

The problem: Flyway considers an **empty schema** as "already at baseline V1", so it skips V1 migrations.

#### The Fix

**Option A (RECOMMENDED):** Remove baseline-on-migrate entirely

```properties
# services/*/src/main/resources/application.properties

# REMOVE this line:
# quarkus.flyway.baseline-on-migrate=true

# Keep these:
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
quarkus.flyway.table=flyway_schema_history_<service>
```

**Option B:** Set baseline-version to 0

```properties
quarkus.flyway.baseline-on-migrate=true
quarkus.flyway.baseline-version=0
```

This allows V1 migrations to run since baseline is at V0.

#### Files to Fix

**Apply Option A to ALL these files:**

1. `services/user-service/src/main/resources/application.properties`
2. `services/wallet-service/src/main/resources/application.properties`
3. `services/trading-service/src/main/resources/application.properties`
4. `services/portfolio-service/src/main/resources/application.properties`
5. `services/transaction-history-service/src/main/resources/application.properties`

#### Verification Steps

After applying the fix:

```bash
# 1. Clean database
docker-compose down -v

# 2. Rebuild and start
./gradlew clean build -x test
docker-compose up -d

# 3. Wait for services to start (30 seconds)
sleep 30

# 4. Verify tables exist
docker exec claude-team-development-demo-postgres-1 \
  psql -U trading -d trading -c "\dt"

# Expected output: users, wallet_balances, trades, holdings, transactions, fee_rules tables
```

---

### 🔴 BUG #2: Kafka Event Flow Broken (HIGHEST PRIORITY)

**File:** `TEST_REPORT.md` - Line 137-171
**Severity:** CRITICAL - Core Functionality Broken
**Impact:** User signup events are not published to Kafka, users not persisted

#### Problem

When a user is created via `POST /api/v1/signup`:
1. ✅ user-signup-service returns HTTP 201 with UUID
2. ❌ Event is NOT published to `user-events` Kafka topic
3. ❌ user-service never receives the event
4. ❌ User is NOT persisted to the database

#### Evidence from Q/A Testing

```bash
# Created 2 users:
# - 5ad78b8d-455f-4804-b30b-a4ffc9a51625 (trader1@example.com)
# - 0f63a395-777e-4535-9b31-14a76d6650e2 (trader2@example.com)

# Both returned HTTP 201 with UUID

# But database shows 0 users:
postgres=# SELECT COUNT(*) FROM users;
 count
-------
     0

# And Kafka topic is empty:
$ kafka-console-consumer --topic user-events --from-beginning --max-messages 10
Processed a total of 0 messages
```

#### Investigation Needed

**Check user-signup-service event publishing:**

1. **Verify Emitter configuration:**
```java
// services/user-signup-service/src/main/java/.../SignupService.java

@Inject
@Channel("user-events-out")
Emitter<String> userEventsEmitter;

// Is the emitter being called?
// Is the JSON serialization working?
```

2. **Check application.properties:**
```properties
# services/user-signup-service/src/main/resources/application.properties

mp.messaging.outgoing.user-events-out.connector=smallrye-kafka
mp.messaging.outgoing.user-events-out.topic=user-events
mp.messaging.outgoing.user-events-out.value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

3. **Add debug logging:**
```java
// Before emitter.send()
logger.info("Publishing user event to Kafka: userId={}, email={}", userId, email);

// After emitter.send()
logger.info("User event published successfully");
```

4. **Check for serialization errors:**
Look for exceptions in user-signup-service logs when creating events.

#### Possible Root Causes

1. **Emitter not injected** - Check if `@Inject @Channel` is working
2. **Event not serialized** - Check if `UserCreatedEvent` can be converted to JSON
3. **Kafka connection issue** - Check if producer is actually connected to Kafka
4. **Blocking send** - Check if `emitter.send()` is throwing an exception that's being swallowed
5. **Transaction rollback** - Check if the send is happening in a transaction that rolls back

#### The Fix

**Step 1:** Add detailed logging to user-signup-service

```java
// services/user-signup-service/src/main/java/.../SignupService.java

public UserSignupResponse signup(UserSignupRequest request) {
    // ... create user ...

    logger.info("About to publish user event: userId={}, email={}", userId, request.getEmail());

    try {
        UserCreatedEvent event = new UserCreatedEvent(userId, request.getEmail(),
            request.getUsername(), request.getPhoneNumber());

        String json = objectMapper.writeValueAsString(event);
        logger.info("Event JSON: {}", json);

        userEventsEmitter.send(json);
        logger.info("Event sent to emitter successfully");
    } catch (Exception e) {
        logger.error("FAILED to publish user event", e);
        throw new RuntimeException("Event publishing failed", e);
    }

    return new UserSignupResponse(userId, request.getEmail(), request.getUsername());
}
```

**Step 2:** Verify Kafka producer configuration

Ensure user-signup-service logs show:
```
INFO  [io.sma.rea.mes.kafka] Kafka producer kafka-producer-user-events-out,
  connected to Kafka brokers 'kafka:29092', is configured to write records to 'user-events'
```

**Step 3:** Test event publishing manually

```bash
# After services start, create a user
curl -X POST http://localhost:8080/api/v1/signup \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "username": "test", "phoneNumber": "+1234567890"}'

# Check logs immediately
docker logs claude-team-development-demo-user-signup-service-1 | tail -20

# Should see:
# "About to publish user event..."
# "Event JSON: {...}"
# "Event sent to emitter successfully"

# Then verify Kafka topic has the message
docker exec claude-team-development-demo-kafka-1 \
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic user-events --from-beginning --max-messages 1 --timeout-ms 5000
```

#### Verification Steps

After fixing:

```bash
# 1. Create a test user
curl -X POST http://localhost:8080/api/v1/signup \
  -H "Content-Type: application/json" \
  -d '{"email": "testuser@example.com", "username": "testuser", "phoneNumber": "+1234567890"}'

# Expected: HTTP 201 with userId

# 2. Wait 3 seconds for event processing
sleep 3

# 3. Verify user in database
docker exec claude-team-development-demo-postgres-1 \
  psql -U trading -d trading \
  -c "SELECT id, email, username FROM users WHERE email = 'testuser@example.com';"

# Expected: 1 row with the user data

# 4. Verify Kafka message
docker exec claude-team-development-demo-kafka-1 \
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic user-events --from-beginning --max-messages 1 --timeout-ms 5000

# Expected: JSON message with user data
```

---

### ⚠️ BUG #3: Service Startup Race Condition (MEDIUM PRIORITY)

**File:** `TEST_REPORT.md` - Line 174-193
**Severity:** MEDIUM - Operational Issue
**Impact:** Services log errors on startup, require manual restart

#### Problem

Microservices (user-service, wallet-service, etc.) start before Kafka is fully ready, causing connection errors:

```
WARN  [org.apa.kaf.cli.NetworkClient] Error while fetching metadata:
  {user-events=LEADER_NOT_AVAILABLE}
```

Services eventually connect, but this creates noise in logs and may cause issues in orchestrated environments.

#### The Fix (Optional for Step 02)

**Option A:** Add depends_on with health checks in docker-compose.yml

```yaml
services:
  user-service:
    depends_on:
      postgres:
        condition: service_healthy
      kafka:
        condition: service_healthy
    # ...
```

**Option B:** Implement retry logic in services (more robust)

This is lower priority - fix BUG #1 and BUG #2 first.

---

## Testing Instructions for Developer

After fixing BUG #1 and BUG #2:

### 1. Clean Start

```bash
# Stop and remove everything
docker-compose down -v

# Rebuild
./gradlew clean build -x test

# Start fresh
docker-compose up -d

# Wait for services
sleep 30
```

### 2. Verify Database Tables

```bash
docker exec claude-team-development-demo-postgres-1 \
  psql -U trading -d trading -c "\dt"
```

**Expected:** All application tables exist (users, wallet_balances, trades, holdings, transactions, fee_rules)

### 3. Test User Registration + Persistence

```bash
# Create user
curl -X POST http://localhost:8080/api/v1/signup \
  -H "Content-Type: application/json" \
  -d '{"email": "devtest@example.com", "username": "devtest", "phoneNumber": "+9999999999"}' \
  | jq '.'

# Expected: {"userId": "...", "email": "devtest@example.com", "username": "devtest"}

# Save the userId, then check database
USER_ID="<paste-userId-here>"

docker exec claude-team-development-demo-postgres-1 \
  psql -U trading -d trading \
  -c "SELECT id, email, username FROM users WHERE id = '${USER_ID}';"

# Expected: 1 row with the user data
```

### 4. Test Wallet Deposit

```bash
# Using the userId from step 3
curl -X POST http://localhost:8086/api/v1/wallets/${USER_ID}/deposit \
  -H "Content-Type: application/json" \
  -d '{"currency": "USD", "amount": 1000.00}' \
  | jq '.'

# Expected: {"id": "...", "userId": "...", "currency": "USD", "balance": 1000.00, ...}
```

### 5. Verify Kafka Event Flow

```bash
# Check that events are in Kafka topics
docker exec claude-team-development-demo-kafka-1 \
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic user-events --from-beginning --max-messages 1 --timeout-ms 5000

# Expected: JSON message with user data
```

---

## Definition of Done

Before passing back to Q/A for Step 02 testing:

- [ ] BUG #1 FIXED: All database tables created on fresh start
- [ ] BUG #2 FIXED: User signup events published to Kafka and consumed by user-service
- [ ] User registration creates database record
- [ ] Wallet deposit works (HTTP 200, balance updated)
- [ ] All services start without CRITICAL errors
- [ ] Developer has manually tested TC-001 and TC-002 successfully

---

## Communication

**[As Developer]** After completing fixes, update `docs/02_discussion.md` with:
1. Root cause analysis for each bug
2. Changes made to fix them
3. Self-test results (TC-001 and TC-002)
4. Any additional issues discovered

Then pass back to Q/A for regression testing.

---

**Created by:** Q/A Specialist
**Date:** 2026-01-12 20:40 UTC
**Priority:** CRITICAL - BLOCKING
