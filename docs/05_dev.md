# 05_dev.md - Developer Bug Fix Instructions for Step 05

**Role:** Developer
**Phase:** Step 05 - Fix Service Bugs Identified by Q/A
**From:** Q/A Specialist
**Priority:** CRITICAL
**Date:** 2026-01-13

---

## Executive Summary

Q/A Specialist has completed testing of the Spock integration test suite and **REJECTED** Step 04 due to critical service bugs revealed by the tests.

**Good News:** The tests are working perfectly! They successfully identified 4 service implementation bugs that need fixing.

**Your Task:** Fix the 4 service bugs below, then re-run the test suite until all 45 tests pass.

---

## Test Results Summary

**Total Tests:** 45
**Passing:** 14 (31.1%)
**Failing:** 31 (68.9%)

**Root Cause:** Service implementation bugs, NOT test issues.

---

## Service Bugs to Fix

### Priority 1: Trading Service - Missing Endpoints (CRITICAL)

**Impact:** 21 test failures (46.7% of all failures)

**Problem:** Trading service endpoints return 404 Not Found

**Evidence:**
```bash
curl -X POST "http://localhost:8087/api/v1/trades/{userId}/buy/amount" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"AAPL","amount":100.0,"currency":"USD"}'

# Response: HTTP/1.1 404 Not Found
```

**Affected Tests:**
- BuyOrderSpec (4/4 failures)
- SellOrderSpec (4/4 failures)
- FractionalSharesSpec (4/4 failures)
- PortfolioTrackingSpec (5/5 failures)
- CompleteUserJourneySpec (3/3 failures)

**Required Endpoints:**
1. `POST /api/v1/trades/{userId}/buy/amount` - Buy by dollar amount
2. `POST /api/v1/trades/{userId}/buy/quantity` - Buy by share quantity
3. `POST /api/v1/trades/{userId}/sell/amount` - Sell by dollar amount
4. `POST /api/v1/trades/{userId}/sell/quantity` - Sell by share quantity

**Expected Request Body (Buy by Amount):**
```json
{
  "symbol": "AAPL",
  "amount": 1000.00,
  "currency": "USD"
}
```

**Expected Response:**
```json
{
  "tradeId": "uuid",
  "userId": "uuid",
  "symbol": "AAPL",
  "quantity": 5.23,
  "price": 191.20,
  "totalCost": 1000.00,
  "fee": 1.00,
  "timestamp": "2026-01-13T17:00:00Z"
}
```

**Files to Check:**
- `services/trading-service/src/main/java/com/trading/controller/TradingController.java`
- Check if endpoints are mapped correctly
- Ensure `@POST` annotations and path variables are correct
- Verify Content-Type header is set to `application/json`

**Validation:**
```bash
# Test after fix
curl -X POST "http://localhost:8087/api/v1/trades/123e4567-e89b-12d3-a456-426614174000/buy/amount" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"AAPL","amount":100.0,"currency":"USD"}' \
  -v

# Should return: HTTP/1.1 200 OK or 201 Created with JSON response
```

---

### Priority 2: User Service - Missing Validation (HIGH)

**Impact:** 2 test failures + Security risk

**Problem:** User signup accepts invalid emails and duplicate emails

**Evidence:**
```bash
# Test 1: Invalid email format accepted (WRONG)
curl -X POST "http://localhost:8080/api/v1/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid-email","username":"testuser","phoneNumber":"+1234567890"}'

# Response: HTTP/1.1 201 Created (SHOULD BE 400 Bad Request)
# Body: {"email":"invalid-email","username":"testuser","userId":"..."}

# Test 2: Duplicate email accepted (WRONG)
# Create user with email@example.com
# Try to create another user with same email
# Response: HTTP/1.1 201 Created (SHOULD BE 409 Conflict or 400)
```

**Affected Tests:**
- UserRegistrationSpec > invalid email format is rejected
- UserRegistrationSpec > duplicate email registration is rejected

**Required Fixes:**

1. **Email Format Validation**
   - Add email validation before creating user
   - Return 400 Bad Request for invalid format
   - Use regex or Jakarta validation annotations

2. **Duplicate Email Check**
   - Query database to check if email already exists
   - Return 409 Conflict or 400 Bad Request if duplicate
   - Perform check BEFORE publishing Kafka event

**Files to Modify:**
- `services/user-signup-service/src/main/java/com/trading/service/SignupService.java`

**Example Fix (Email Validation):**
```java
// Option 1: Use Jakarta validation
import jakarta.validation.constraints.Email;

public class SignupRequest {
    @Email(message = "Invalid email format")
    private String email;
    // ...
}

// Option 2: Manual validation
public void validateEmail(String email) {
    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    if (!email.matches(emailRegex)) {
        throw new IllegalArgumentException("Invalid email format");
    }
}
```

**Example Fix (Duplicate Check):**
```java
// In SignupService.java
public SignupResponse signup(SignupRequest request) {
    // 1. Validate email format
    validateEmail(request.getEmail());

    // 2. Check for duplicate
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new DuplicateEmailException("Email already registered");
    }

    // 3. Create user
    String userId = UUID.randomUUID().toString();

    // 4. Publish event
    // ...
}
```

**Validation:**
```bash
# Test invalid email (should fail)
curl -X POST "http://localhost:8080/api/v1/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid","username":"test","phoneNumber":"+1234567890"}' \
  -i

# Should return: HTTP/1.1 400 Bad Request

# Test duplicate email (should fail)
# First signup succeeds
curl -X POST "http://localhost:8080/api/v1/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","username":"test1","phoneNumber":"+1234567890"}'

# Second signup with same email should fail
curl -X POST "http://localhost:8080/api/v1/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","username":"test2","phoneNumber":"+1234567890"}' \
  -i

# Should return: HTTP/1.1 409 Conflict or 400 Bad Request
```

---

### Priority 3: Portfolio Service - Incorrect Empty Response (MEDIUM)

**Impact:** 1 test failure

**Problem:** GET /portfolio/{userId} returns 404 for users with no holdings (should return 200 with empty array)

**Evidence:**
```bash
curl -X GET "http://localhost:8088/api/v1/portfolio/123e4567-e89b-12d3-a456-426614174000" -i

# Response: HTTP/1.1 404 Not Found (WRONG - should be 200 OK)
```

**Affected Tests:**
- PortfolioTrackingSpec > empty portfolio returns valid response

**Expected Behavior:**
- User with no holdings → 200 OK with `{"holdings": []}`
- User does not exist → 404 Not Found (this is correct)

**Current Behavior:**
- User with no holdings → 404 Not Found (WRONG)

**Required Fix:**
Differentiate between "user doesn't exist" and "user has no holdings"

**Files to Modify:**
- `services/portfolio-service/src/main/java/com/trading/controller/PortfolioController.java`
- `services/portfolio-service/src/main/java/com/trading/service/PortfolioService.java`

**Example Fix:**
```java
// In PortfolioController.java
@GET
@Path("/{userId}")
@Produces(MediaType.APPLICATION_JSON)
public Response getPortfolio(@PathParam("userId") String userId) {
    List<Holding> holdings = portfolioService.getHoldings(userId);

    // Return 200 with empty array if no holdings
    return Response.ok(Map.of("holdings", holdings)).build();
}

// Note: Only return 404 if you need to verify user exists first
// Most REST APIs return 200 with empty array for "no data" scenarios
```

**Validation:**
```bash
# Create user without any trades
# Then query portfolio
curl -X GET "http://localhost:8088/api/v1/portfolio/{userId}" -i

# Should return:
# HTTP/1.1 200 OK
# Content-Type: application/json
# {"holdings": []}
```

---

### Priority 4: Currency Exchange - Endpoint Failure (MEDIUM)

**Impact:** 1 test failure

**Problem:** POST /wallet/exchange returns 400 Bad Request for valid requests

**Affected Tests:**
- CurrencyExchangeSpec > currency exchange converts USD to EUR correctly

**Expected Request:**
```json
{
  "fromCurrency": "USD",
  "toCurrency": "EUR",
  "amount": 1000.00
}
```

**Expected Response:**
```json
{
  "fromCurrency": "USD",
  "toCurrency": "EUR",
  "fromAmount": 1000.00,
  "toAmount": 850.00,
  "exchangeRate": 0.85,
  "timestamp": "2026-01-13T17:00:00Z"
}
```

**Files to Check:**
- `services/wallet-service/src/main/java/com/trading/controller/WalletController.java`
- Check request validation
- Verify all required fields are present
- Check if there's a mismatch between what endpoint expects and what test sends

**Debugging Steps:**
1. Enable debug logging in wallet-service
2. Send test request and check logs for validation errors
3. Verify request body field names match DTOs
4. Check for required fields that might be missing

**Validation:**
```bash
# Test after fix
curl -X POST "http://localhost:8086/api/v1/wallet/{userId}/exchange" \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency":"USD","toCurrency":"EUR","amount":1000.00}' \
  -v

# Should return: HTTP/1.1 200 OK with exchange result
```

---

### Priority 5: Kafka Event Timing (LOW)

**Impact:** 6 test failures (but tests pass individually)

**Problem:** 2-second wait time insufficient for Kafka event processing under concurrent test load

**Affected Tests:**
- UserRegistrationSpec > successful user registration
- KafkaEventFlowSpec (multiple tests)
- SchemaIsolationSpec (one test)
- CompleteUserJourneySpec (one test)

**Required Fix:** Increase wait time or run tests sequentially

**Option A: Increase Wait Time (Recommended)**

File: `services/integration-tests/src/test/groovy/com/trading/integration/BaseIntegrationSpec.groovy`

```groovy
// Line 88 - Change from:
Thread.sleep(2000)

// To:
Thread.sleep(5000)
```

**Option B: Sequential Test Execution**

File: `services/integration-tests/build.gradle`

```gradle
test {
    useJUnitPlatform()
    maxParallelForks = 1  // Change from 2 to 1
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }
}
```

**Recommended:** Try Option A first. Only use Option B if tests remain flaky.

---

## Testing After Fixes

### Step 1: Build Services
```bash
./gradlew clean build -x test
```

### Step 2: Restart Docker Containers
```bash
docker-compose down
docker-compose up -d
sleep 30  # Wait for services to start
```

### Step 3: Run Integration Tests
```bash
./gradlew :services:integration-tests:test
```

**Expected Result:** All 45 tests should PASS (100%)

### Step 4: Run Tests Multiple Times (Stability Check)
```bash
for i in {1..3}; do
  echo "Run $i"
  ./gradlew :services:integration-tests:test
done
```

**Expected:** All 3 runs should pass with 45/45 tests

---

## Acceptance Criteria

Before handing back to Q/A, ensure:

- ✅ All 45 integration tests passing (100%)
- ✅ Tests run successfully 3 times consecutively
- ✅ No flaky tests
- ✅ Manual validation of each endpoint fix
- ✅ Build successful with no errors
- ✅ All 15 services running healthy

---

## Documentation Requirements

After fixing bugs, update:

1. **docs/05_discussion.md** (create new file)
   - Document which bugs you fixed
   - Include code changes made
   - Explain root causes
   - Show test results (before/after)

2. **IMPLEMENTATION_STATUS.md**
   - Update Step 05 status
   - Document final test results

---

## Hand-off to Q/A

When all fixes complete and tests pass, hand back to Q/A:

1. Update `docs/05_discussion.md` with your findings
2. Run final test suite: `./gradlew :services:integration-tests:test`
3. Capture test report screenshot or output
4. Notify Q/A that Step 05 is ready for re-validation

---

## Estimated Effort

- Trading service endpoints: 2-4 hours
- User validation: 30 minutes
- Portfolio empty response: 15 minutes
- Currency exchange debug: 30-60 minutes
- Test timing adjustment: 5 minutes
- Testing and validation: 30 minutes

**Total:** 4-6 hours

---

## Questions?

If you encounter issues:
1. Check service logs: `docker logs <service-name>`
2. Verify database state: `docker exec -it postgres psql -U trading -d trading`
3. Test endpoints manually with curl before running integration tests
4. Escalate to Senior Engineer if architectural changes needed

---

**Status:** AWAITING DEVELOPER - Step 05 Bug Fixes
**From:** Q/A Specialist
**Date:** 2026-01-13
**Priority:** CRITICAL - Production bugs identified
