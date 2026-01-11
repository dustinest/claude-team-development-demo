# Test Report - Fractional Stock Trading Platform

**Test Date:** 2026-01-11
**Tester:** Q/A Specialist (Claude)
**Build Version:** 1.0.0-SNAPSHOT
**Environment:** Docker Compose (Local Development)

---

## Test Environment Setup

### Build Status
- ✅ All 10 microservices compiled successfully
- ✅ Gradle build: 87 tasks, BUILD SUCCESSFUL
- ⏳ Docker Compose: Starting services (downloading base images...)

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
- **15:53:00** - Gradle build initiated
- **15:53:30** - Build completed successfully (1s, mostly up-to-date)
- **15:53:32** - Docker Compose started
- **15:53:32** - Downloading base images (OpenJDK 21, PostgreSQL, Kafka, etc.)
- **Status:** Images downloading, services building...

---

## Test Results

### Pre-Flight Checks

#### ✅ Build Verification
- **Status:** PASS
- **Details:** All services compile without errors
- **Artifacts:** 10 service JARs created in build/quarkus-app/

#### ⏳ Service Health Checks
- **Status:** PENDING (services starting)
- **Next:** Verify all services report healthy status

---

## Test Cases

### TC-001: User Registration
- **Status:** PENDING
- **Priority:** HIGH
- **Prerequisite:** API Gateway operational

### TC-002: Wallet Deposit (USD)
- **Status:** PENDING
- **Priority:** HIGH
- **Prerequisite:** TC-001 complete

### TC-003: View Securities List
- **Status:** PENDING
- **Priority:** HIGH
- **Prerequisite:** Securities Pricing Service operational

### TC-004: Buy Fractional Shares (By Amount)
- **Status:** PENDING
- **Priority:** CRITICAL
- **Prerequisite:** TC-001, TC-002, TC-003 complete

### TC-005: Buy Fractional Shares (By Quantity)
- **Status:** PENDING
- **Priority:** HIGH
- **Prerequisite:** TC-004 complete

### TC-006: View Portfolio
- **Status:** PENDING
- **Priority:** HIGH
- **Prerequisite:** TC-004 complete

### TC-007: Sell Fractional Shares
- **Status:** PENDING
- **Priority:** CRITICAL
- **Prerequisite:** TC-004, TC-006 complete

### TC-008: Wallet Withdrawal
- **Status:** PENDING
- **Priority:** MEDIUM
- **Prerequisite:** TC-002 complete

### TC-009: Currency Exchange
- **Status:** PENDING
- **Priority:** HIGH
- **Prerequisite:** TC-002 complete

### TC-010: Transaction History
- **Status:** PENDING
- **Priority:** HIGH
- **Prerequisite:** Multiple operations complete

### TC-011: Multi-Currency Deposit
- **Status:** PENDING
- **Priority:** MEDIUM

### TC-012: Insufficient Funds Handling
- **Status:** PENDING
- **Priority:** CRITICAL
- **Test Type:** Negative test

### TC-013: Price Updates
- **Status:** PENDING
- **Priority:** MEDIUM
- **Test Type:** Time-based test

### TC-014: Exchange Rate Updates
- **Status:** PENDING
- **Priority:** MEDIUM
- **Test Type:** Time-based test

### TC-015: Customer-Favorable Rounding
- **Status:** PENDING
- **Priority:** CRITICAL
- **Test Type:** Calculation verification

---

## Issues Found

*No issues found yet - testing in progress*

---

## Notes

### Docker Compose First Run
- Base image downloads take 5-10 minutes on first run
- Subsequent runs will be much faster (cached images)
- All 10 services + 4 infrastructure containers = 14 total containers

### Testing Strategy
1. Wait for all services to become healthy
2. Execute test cases in order (TC-001 through TC-015)
3. Document results, screenshots, and any issues
4. Verify event flows via Kafka
5. Verify database consistency

---

## Overall Status

**Current Phase:** System Operational
**Progress:** All 14 containers running successfully
**Status:** READY FOR COMPREHENSIVE TESTING

### Services Status
- ✅ All 10 microservices: RUNNING
- ✅ PostgreSQL: HEALTHY
- ✅ Kafka + Zookeeper: HEALTHY
- ✅ Redis: HEALTHY
- ✅ API Gateway: UP (http://localhost:8080)

### Quick Tests Executed
- ✅ TC-001: User Registration - PASSED (UUID: 930b573b-7efe-4c21-b677-3358a6665cea)
- ✅ TC-003: View Securities - PASSED (20 securities with live price updates)
- ✅ Service Health Checks - PASSED

### Technical Issues Resolved During Setup
1. **Flyway Migration Conflict**: Multiple services sharing same schema history table
   - **Fix**: Configured unique Flyway table per service (flyway_schema_history_{service})
   - **Fix**: Added baseline-on-migrate=true for non-empty schemas

2. **Kafka Deserialization Error**: ObjectMapperDeserializer missing no-arg constructor
   - **Fix**: Switched to StringDeserializer with manual Jackson deserialization
   - **Services Updated**: user-service, portfolio-service, transaction-history-service

### System Ready For
- Full end-to-end testing (TC-001 through TC-015)
- Load testing
- Event flow validation
- Database consistency checks

---

**Report Last Updated:** 2026-01-11 16:13 UTC
**Q/A Specialist:** Claude (Sonnet 4.5)
**Build Status:** OPERATIONAL ✅
