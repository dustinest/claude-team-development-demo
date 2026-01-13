# 02_discussion.md - Step 02 Bug Fix Discussion

**Iteration:** Step 02 - Bug Fix Loop
**Date:** 2026-01-12
**Participants:** Q/A Specialist → Developer

---

## Q/A → Developer Handoff

### Context

[As Q/A Specialist] I have completed initial testing of the Step 01 implementation and discovered **2 critical bugs** that prevent basic system functionality. Testing was blocked after attempting only 2 of 15 test cases.

### Bugs Found

1. **BUG #1 (CRITICAL):** Flyway migrations not executing due to `baseline-on-migrate=true` configuration
   - **Impact:** All application database tables are missing
   - **Services Affected:** user-service, wallet-service, trading-service, portfolio-service, transaction-history-service

2. **BUG #2 (CRITICAL):** Kafka event flow broken - user signup events not published
   - **Impact:** Users created via API are not persisted to database
   - **Services Affected:** user-signup-service → user-service

3. **BUG #3 (MEDIUM):** Service startup race condition with Kafka
   - **Impact:** Services log errors on startup, require manual restart

### Test Results

- **TC-001 (User Registration):** ❌ FAILED - User not persisted
- **TC-002 (Wallet Deposit):** ❌ BLOCKED - Database tables missing
- **TC-003 through TC-015:** ❌ BLOCKED - Cannot proceed

### Documentation Created

- **TEST_REPORT.md:** Updated with detailed bug information, evidence, and test results
- **docs/02_dev.md:** Comprehensive bug fix instructions for Developer with verification steps

### Q/A Recommendation

System requires immediate bug fixes before testing can continue. Looping back to Developer for Step 02 bug fix iteration.

---

## Developer Notes

**[As Developer - Completed 2026-01-12 20:55 UTC]**

### Root Cause Analysis

#### BUG #1: Flyway Migration Failure ✅ FIXED
- **Root Cause:** `quarkus.flyway.baseline-on-migrate=true` configuration setting
- **Why it happened:** When this setting is enabled and the database schema is empty, Flyway creates a baseline at version 1 and marks the schema as "up to date" WITHOUT executing any V1__*.sql migration scripts. This was added in Step 01 to resolve schema history conflicts between services, but it inadvertently broke fresh database initialization.
- **Fix applied:** Removed `quarkus.flyway.baseline-on-migrate=true` from all 5 affected services. The unique `flyway_schema_history_{service}` tables are sufficient to prevent conflicts without needing baseline-on-migrate.

#### BUG #2: Kafka Event Flow Broken ✅ FIXED (Two Root Causes)
- **Root Cause #2a:** Missing `@Inject` annotation on `userEventsEmitter` field in SignupService
- **Why it happened:** The field was declared with `@Channel` annotation only. Without `@Inject`, CDI doesn't inject the emitter, leaving it null. When `.send()` was called, it either did nothing or threw a NullPointerException that was silently swallowed.
- **Fix applied:** Added `@Inject` annotation before `@Channel("user-events-out")` in SignupService.java:18

- **Root Cause #2b:** Missing `@Blocking` annotation on `consumeUserCreatedEvent()` method in UserEventConsumer
- **Why it happened:** Kafka consumer methods run on Vert.x event loop threads (IO threads) by default. When the consumer tried to call `userService.createUser()` which starts a JTA transaction, Quarkus threw "Cannot start a JTA transaction from the IO thread" error. The exception was caught and logged but the user was never persisted.
- **Fix applied:** Added `@Blocking` annotation to `consumeUserCreatedEvent()` method in UserEventConsumer.java:23. This tells SmallRye Reactive Messaging to execute the method on a worker thread where JTA transactions are allowed.

#### BUG #3: Service Startup Race Condition
- **Root Cause:** Docker Compose starts all services in parallel. Microservices attempt to connect to Kafka before Kafka is fully ready.
- **Why it happened:** No startup dependencies or health check waiting in docker-compose.yml
- **Fix applied:** NOT FIXED in this iteration (low priority). Services eventually connect after retries. Can be addressed in future iteration with proper depends_on health checks.

### Changes Made

1. `services/user-service/src/main/resources/application.properties` - Removed line 20 (baseline-on-migrate)
2. `services/wallet-service/src/main/resources/application.properties` - Removed line 16 (baseline-on-migrate)
3. `services/trading-service/src/main/resources/application.properties` - Removed line 14 (baseline-on-migrate)
4. `services/portfolio-service/src/main/resources/application.properties` - Removed line 12 (baseline-on-migrate)
5. `services/transaction-history-service/src/main/resources/application.properties` - Removed line 12 (baseline-on-migrate)
6. `services/user-signup-service/src/main/java/.../SignupService.java` - Added `@Inject` annotation (line 18)
7. `services/user-service/src/main/java/.../UserEventConsumer.java` - Added `@Blocking` annotation (line 23) and import

### Self-Testing Results

#### TC-001: User Registration ✅ PASSED
- **Test Performed:** Created user via POST /api/v1/signup with email=test99@example.com
- **Result:** SUCCESS - User persisted to database with UUID d1754421-91e3-439f-9dfc-bd6e24c18081
- **Evidence:**
  - API returned HTTP 201 with userId
  - Kafka topic contained event: `{"userId":"d1754421-91e3-439f-9dfc-bd6e24c18081","email":"test99@example.com"...}`
  - User-service logs: "Successfully processed UserCreatedEvent" on executor-thread-1 (worker thread)
  - Database query confirmed: 1 row in users table with correct data
- **End-to-End Flow Verified:** signup-service → Kafka → user-service → PostgreSQL ✅

#### TC-002: Wallet Deposit ⚠️ BLOCKED
- **Test Performed:** Attempted POST /api/v1/wallets/{userId}/deposit via API Gateway
- **Result:** HTTP 404 Not Found
- **Evidence:** API Gateway does not proxy wallet endpoints
- **Status:** Blocked by separate issue (API Gateway routing configuration)
- **Note:** Wallet service is healthy and database tables exist. This is an API Gateway configuration issue, not related to the critical bugs fixed in this iteration.

### Additional Issues Discovered

1. **API Gateway Routing:** Wallet service endpoints not proxied through API Gateway (returns 404). Frontend would also be affected. Requires investigation of API Gateway proxy configuration.

2. **Docker Build Caching:** Had to force container recreation (`docker rm` + `docker-compose up`) to deploy code changes. Standard `docker-compose restart` used cached images. Development workflow should use `docker-compose up --build` to ensure fresh builds.

### Ready for Q/A

- [x] All CRITICAL bugs fixed (BUG #1 and BUG #2)
- [x] Database tables created on fresh start
- [x] User signup working end-to-end
- [ ] Wallet operations working (BLOCKED by API Gateway routing - separate bug)
- [x] No CRITICAL errors in service logs
- [x] Self-tested TC-001 successfully (TC-002 blocked by different issue)

---

## Next Steps

**After Developer completes fixes:**

[As Developer] Update this discussion file with your findings and pass back to Q/A Specialist for Step 02 regression testing.

[As Q/A Specialist] Will execute full test suite (TC-001 through TC-015) and provide final sign-off or create Step 03 if additional issues found.

---

**Created by:** Q/A Specialist
**Date:** 2026-01-12 20:45 UTC
**Status:** AWAITING DEVELOPER RESPONSE
