# Test Report - Fractional Stock Trading Platform

**Test Date:** 2026-01-12 (Step 01 Testing - FAILED)
**Tester:** Q/A Specialist (Claude)
**Build Version:** 1.0.0-SNAPSHOT
**Environment:** Docker Compose (Local Development)
**Iteration:** Step 01 → Step 02 (Bug Fix Loop)

---

## Executive Summary

**STATUS:** ❌ **CRITICAL BUGS FOUND - TESTING FAILED**

Three critical bugs prevent basic system functionality. All tests blocked. System requires Developer attention before Q/A can continue.

---

## Test Environment Setup

### Build Status
- ✅ All 10 microservices compiled successfully
- ✅ Gradle build: 102 tasks, BUILD SUCCESSFUL in 43s
- ✅ Docker Compose: All 15 containers built and started

### Services to Test
1. Securities Pricing Service (Port 8081)
2. Currency Exchange Service (Port 8082)
3. Fee Service (Port 8083)
4. User Signup Service (Port 8084)
5. User Service (Port 8085)
6. Wallet Service (Port 8086)
7. Trading Service (Port 8087)
8. Portfolio Service (Port 8088)
9. Transaction History Service (Port 8089)
10. API Gateway (Port 8080)

### Infrastructure Services
- PostgreSQL (Port 5432)
- Redis (Port 6379)
- Kafka (Port 9092)
- Zookeeper (Port 2181)

---

## Test Execution Log

### System Startup
- **20:21:42** - Docker Compose started (after resolving port conflicts from previous project)
- **20:22:51** - Gradle build executed: 102 tasks, BUILD SUCCESSFUL in 43s
- **20:25:28** - All 15 containers started successfully
- **20:26:17** - All services verified running (10 microservices + 5 infrastructure)
- **Status:** ✅ Infrastructure operational

### Pre-Flight Checks
- ✅ **Build Verification**: All services compile without errors
- ✅ **Container Health**: All 15 containers running
- ✅ **API Gateway Health**: http://localhost:8080/q/health returns {"status":"UP"}
- ✅ **Kafka Topics**: user-events, wallet-events, trading-events created

---

## Test Results

### TC-001: User Registration
- **Status:** ❌ **FAILED**
- **Priority:** HIGH
- **Result:**
  - API returned 201 with UUID: `5ad78b8d-455f-4804-b30b-a4ffc9a51625`
  - BUT user NOT persisted to database (0 rows in users table)
  - **Root Cause:** Kafka event flow broken (see Bug #2 below)

### TC-002: Wallet Deposit (USD)
- **Status:** ❌ **BLOCKED**
- **Priority:** HIGH
- **Result:** HTTP 500 Internal Server Error
- **Root Cause:** Database table `wallet_balances` does not exist (see Bug #1 below)

### TC-003 through TC-015
- **Status:** ❌ **BLOCKED**
- **Reason:** Cannot proceed without working user registration and wallet functionality

---

## Critical Bugs Found

### 🔴 BUG #1: Flyway Migration Failure (CRITICAL)

**Severity:** CRITICAL - System Non-Functional
**Impact:** All application tables missing from database
**Services Affected:** user-service, wallet-service, trading-service, portfolio-service, transaction-history-service, fee-service

**Description:**
Configuration setting `quarkus.flyway.baseline-on-migrate=true` causes Flyway to baseline to version 1 without executing any migrations on a fresh database.

**Evidence:**
```bash
# Expected tables: users, wallet_balances, trades, holdings, transactions
# Actual tables: Only flyway_schema_history_* tables exist

postgres=# \dt
                      List of relations
 Schema |               Name                | Type  |  Owner
--------+-----------------------------------+-------+---------
 public | fee_rules                         | table | trading  ← Only this exists
 public | flyway_schema_history_fee         | table | trading
 public | flyway_schema_history_portfolio   | table | trading
 public | flyway_schema_history_trading     | table | trading
 public | flyway_schema_history_transaction | table | trading
 public | flyway_schema_history_user        | table | trading
 public | flyway_schema_history_wallet      | table | trading
```

**Flyway Logs:**
```
INFO  [org.fly.cor.int.sch.JdbcTableSchemaHistory] Schema history table does not exist yet
INFO  [org.fly.cor.int.com.DbBaseline] Successfully baselined schema with version: 1
INFO  [org.fly.cor.int.com.DbMigrate] Schema "public" is up to date. No migration necessary.
```

**Workaround Applied:**
Manually executed all V1__*.sql migration files to create tables

**Fix Required:**
- Remove `quarkus.flyway.baseline-on-migrate=true` from all services
- OR set `quarkus.flyway.baseline-version=0` to allow V1 migrations to run

**Files to Fix:**
- services/user-service/src/main/resources/application.properties
- services/wallet-service/src/main/resources/application.properties
- services/trading-service/src/main/resources/application.properties
- services/portfolio-service/src/main/resources/application.properties
- services/transaction-history-service/src/main/resources/application.properties

---

### 🔴 BUG #2: Kafka Event Flow Broken (CRITICAL)

**Severity:** CRITICAL - Core Functionality Broken
**Impact:** User signup events not published, users not persisted
**Services Affected:** user-signup-service → user-service

**Description:**
Users created via POST /api/v1/signup return success (HTTP 201 with UUID) but are never persisted to the database. The Kafka event from user-signup-service is not reaching user-service.

**Evidence:**
```bash
# Test 1: Created user 5ad78b8d-455f-4804-b30b-a4ffc9a51625
# Result: 0 rows in users table

# Test 2: Created user 0f63a395-777e-4535-9b31-14a76d6650e2
# Result: 0 rows in users table

# Kafka topic check:
$ kafka-console-consumer --topic user-events --from-beginning --max-messages 5
Processed a total of 0 messages  ← NO MESSAGES IN TOPIC
```

**Logs:**
```
# user-signup-service: Claims to publish
INFO  [com.tra.pla.sig.ser.SignupService] User signup completed:
  userId=0f63a395-777e-4535-9b31-14a76d6650e2, email=trader2@example.com

# BUT: No Kafka publish logs
# AND: user-service shows NO consumption logs
```

**Status:** Not resolved
**Fix Required:** Developer investigation needed - event may not be publishing or serialization issue

---

### ⚠️ BUG #3: Service Startup Race Condition (MEDIUM)

**Severity:** MEDIUM - Operational Issue
**Impact:** Services fail Kafka connection on first startup

**Description:**
Microservices start before Kafka is fully ready, causing LEADER_NOT_AVAILABLE errors.

**Evidence:**
```
WARN  [org.apa.kaf.cli.NetworkClient] Error while fetching metadata:
  {user-events=LEADER_NOT_AVAILABLE}
```

**Workaround Applied:**
Manual restart of services after Kafka is healthy: `docker-compose restart user-service wallet-service ...`

**Fix Required:**
- Add health check dependencies in docker-compose.yml
- OR implement Kafka connection retry logic in services

---

## Overall Status

**Current Phase:** ❌ **TESTING FAILED - BUG FIX REQUIRED**
**Progress:** 0% (0/15 test cases passed)
**Status:** **BLOCKED** - System non-functional

### Test Summary
- ❌ TC-001: User Registration - **FAILED** (Kafka event not published)
- ❌ TC-002: Wallet Deposit - **BLOCKED** (Database tables missing)
- ❌ TC-003 through TC-015: **BLOCKED** (Prerequisites not met)

### Critical Bugs
1. 🔴 **BUG #1**: Flyway migrations not executing (baseline-on-migrate issue)
2. 🔴 **BUG #2**: Kafka event flow broken (user signup events not published)
3. ⚠️ **BUG #3**: Service startup race condition with Kafka

### Impact Assessment
- **User Registration:** Non-functional
- **Wallet Operations:** Non-functional
- **Trading:** Cannot test (blocked)
- **Portfolio:** Cannot test (blocked)
- **Transactions:** Cannot test (blocked)

### Infrastructure Status
- ✅ All 15 containers: RUNNING
- ✅ PostgreSQL: HEALTHY
- ✅ Kafka + Zookeeper: HEALTHY
- ✅ Redis: HEALTHY
- ❌ **Application layer:** NON-FUNCTIONAL

---

## Q/A Decision

**❌ TESTING CANNOT CONTINUE**

The system has **2 critical bugs** that prevent basic functionality. I am looping back to the **Developer** role to fix these issues before testing can proceed.

### Next Steps (Step 02 - Bug Fix Iteration)

**Developer Tasks:**
1. Fix BUG #1: Remove or correct Flyway baseline configuration
2. Fix BUG #2: Investigate and fix Kafka event publishing in user-signup-service
3. Fix BUG #3: Add proper startup dependencies for Kafka

**After Fixes:**
- Q/A will re-test from TC-001 through TC-015
- Full regression testing required

---

**Report Last Updated:** 2026-01-12 20:35 UTC
**Q/A Specialist:** Claude (Sonnet 4.5)
**Build Status:** ❌ **FAILED - REQUIRES BUG FIXES**

**Iteration:** Step 01 Testing → Step 02 Bug Fixes Required
