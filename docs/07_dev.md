# Step 07: Developer Implementation Guide - Trading Service API Fix

**Date:** 2026-01-14
**From:** Senior Engineer
**To:** Developer
**Priority:** HIGH - Core Business Feature Fix
**Estimated Time:** 2-3 hours total

---

## Overview

**Task:** Refactor Trading Service endpoints to fix API contract mismatch and enable 21+ integration tests.

**Root Cause:** Trading Service endpoints don't match what API Gateway and tests expect, causing tests to call non-existent endpoints and fail with Content-Type errors.

**Solution:** Consolidate 4 endpoints into 2, matching Gateway expectations.

**Expected Outcome:** 40+ integration tests passing (89%+ coverage)

---

## Pre-Implementation Reading

**Required Reading:**
- `docs/07_discussion.md` - Senior Engineer analysis and architectural decisions
- `docs/07_se.md` - Product Owner business context

**Key Points:**
1. This is NOT a Content-Type annotation issue - annotations are correct
2. Issue is API contract mismatch between Trading Service, Gateway, and Tests
3. We're consolidating 4 endpoints → 2 endpoints
4. Moving userId from path params to request body
5. Creating unified TradeRequest DTO

---

## Changes Summary

### Trading Service Changes

**Before (Current - Broken):**
```
POST /api/v1/trades/{userId}/buy/amount
POST /api/v1/trades/{userId}/buy/quantity
POST /api/v1/trades/{userId}/sell/amount
POST /api/v1/trades/{userId}/sell/quantity
```

**After (Target - Working):**
```
POST /api/v1/trades/buy
POST /api/v1/trades/sell
```

### Integration Test Changes

**Before:**
```groovy
.post("${TRADING_SERVICE_URL}/api/v1/trades/${userId}/buy/${orderType.toLowerCase()}")
// Body: {symbol, amount, currency}
```

**After:**
```groovy
.post("${TRADING_SERVICE_URL}/api/v1/trades/buy")
// Body: {userId, symbol, amount, quantity, currency, orderType}
```

---

## Implementation Steps

### Step 1: Update Trading Service Resource (1-1.5 hours)

#### File to Modify
`services/trading-service/src/main/java/com/trading/platform/trading/resource/TradingResource.java`

#### Backup Current File (Optional but Recommended)
```bash
cp services/trading-service/src/main/java/com/trading/platform/trading/resource/TradingResource.java \
   services/trading-service/src/main/java/com/trading/platform/trading/resource/TradingResource.java.backup
```

#### Complete New Implementation

Replace the entire `TradingResource.java` file with:

```java
package com.trading.platform.trading.resource;

import com.trading.platform.domain.*;
import com.trading.platform.trading.service.TradingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Path("/api/v1/trades")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Trading", description = "Securities trading operations")
public class TradingResource {
    @Inject
    TradingService tradingService;

    @POST
    @Path("/buy")
    @Operation(summary = "Execute buy order")
    public Response buy(TradeRequest request) {
        try {
            // Validate request
            validateTradeRequest(request);

            // Execute buy based on order type
            var trade = tradingService.executeBuy(
                request.userId,
                request.symbol,
                request.currency,
                request.orderType,
                request.orderType == OrderType.BY_AMOUNT ? request.amount : request.quantity
            );

            return Response.ok(trade).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to execute buy order: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/sell")
    @Operation(summary = "Execute sell order")
    public Response sell(TradeRequest request) {
        try {
            // Validate request
            validateTradeRequest(request);

            // Execute sell based on order type
            var trade = tradingService.executeSell(
                request.userId,
                request.symbol,
                request.currency,
                request.orderType,
                request.orderType == OrderType.BY_AMOUNT ? request.amount : request.quantity
            );

            return Response.ok(trade).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to execute sell order: " + e.getMessage()))
                .build();
        }
    }

    private void validateTradeRequest(TradeRequest request) {
        if (request.userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.symbol == null || request.symbol.isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
        if (request.currency == null) {
            throw new IllegalArgumentException("currency is required");
        }
        if (request.orderType == null) {
            throw new IllegalArgumentException("orderType is required");
        }
        if (request.orderType == OrderType.BY_AMOUNT && (request.amount == null || request.amount.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("amount must be positive when orderType is BY_AMOUNT");
        }
        if (request.orderType == OrderType.BY_QUANTITY && (request.quantity == null || request.quantity.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("quantity must be positive when orderType is BY_QUANTITY");
        }
    }

    public static class TradeRequest {
        public UUID userId;
        public String symbol;
        public Currency currency;
        public OrderType orderType;
        public BigDecimal amount;    // Used when orderType = BY_AMOUNT
        public BigDecimal quantity;  // Used when orderType = BY_QUANTITY
    }
}
```

#### Key Changes Explained

1. **Consolidated Endpoints**
   - Removed: `buyByAmount()`, `buyByQuantity()`, `sellByAmount()`, `sellByQuantity()`
   - Added: `buy()`, `sell()`
   - Path changed from `/{userId}/buy/amount` to `/buy`

2. **Unified Request DTO**
   - Single `TradeRequest` class instead of 4 separate classes
   - Contains all fields: `userId`, `symbol`, `currency`, `orderType`, `amount`, `quantity`
   - `orderType` field determines which value (`amount` or `quantity`) is used

3. **Validation Added**
   - `validateTradeRequest()` method ensures all required fields present
   - Validates correct field based on `orderType` (amount for BY_AMOUNT, quantity for BY_QUANTITY)

4. **Error Handling Improved**
   - Catches `IllegalArgumentException` (validation errors)
   - Catches `IllegalStateException` (business logic errors like insufficient funds)
   - Catches general exceptions (unexpected errors)

#### Verify Compilation

```bash
cd /Users/margus/Git/claude-team-development-demo
./gradlew :services:trading-service:compileJava
```

**Expected:** BUILD SUCCESSFUL

**If compilation fails:**
- Check for typos in imports
- Verify `OrderType` enum exists in `com.trading.platform.domain`
- Verify `TradingService.executeBuy()` and `executeSell()` signatures

---

### Step 2: Update Integration Tests (30 minutes)

#### File to Modify
`services/integration-tests/src/test/groovy/com/trading/integration/BaseIntegrationSpec.groovy`

#### Update the `buyShares()` Helper Method

**Find lines 124-138 (current implementation):**
```groovy
// Helper: Buy shares
Map buyShares(String userId, String symbol, BigDecimal amount, String orderType = "BY_AMOUNT") {
    def body = orderType == "BY_AMOUNT" ?
        [symbol: symbol, amount: amount] :
        [symbol: symbol, quantity: amount]

    def response = given()
        .contentType(ContentType.JSON)
        .body(body)
        .post("${TRADING_SERVICE_URL}/api/v1/trades/${userId}/buy/${orderType.toLowerCase()}")
        .then()
        .extract()
        .response()

    return response.body().as(Map)
}
```

**Replace with:**
```groovy
// Helper: Buy shares
Map buyShares(String userId, String symbol, BigDecimal amount, String orderType = "BY_AMOUNT") {
    def body = [
        userId: userId,
        symbol: symbol,
        currency: "USD",
        orderType: orderType
    ]

    if (orderType == "BY_AMOUNT") {
        body.amount = amount
    } else {
        body.quantity = amount
    }

    def response = given()
        .contentType(ContentType.JSON)
        .body(body)
        .post("${TRADING_SERVICE_URL}/api/v1/trades/buy")
        .then()
        .extract()
        .response()

    return response.body().as(Map)
}
```

#### Add `sellShares()` Helper Method (if not present)

**Add after the `buyShares()` method:**
```groovy
// Helper: Sell shares
Map sellShares(String userId, String symbol, BigDecimal amount, String orderType = "BY_AMOUNT") {
    def body = [
        userId: userId,
        symbol: symbol,
        currency: "USD",
        orderType: orderType
    ]

    if (orderType == "BY_AMOUNT") {
        body.amount = amount
    } else {
        body.quantity = amount
    }

    def response = given()
        .contentType(ContentType.JSON)
        .body(body)
        .post("${TRADING_SERVICE_URL}/api/v1/trades/sell")
        .then()
        .extract()
        .response()

    return response.body().as(Map)
}
```

#### Key Changes Explained

1. **Endpoint URL Updated**
   - From: `${TRADING_SERVICE_URL}/api/v1/trades/${userId}/buy/${orderType.toLowerCase()}`
   - To: `${TRADING_SERVICE_URL}/api/v1/trades/buy`
   - Removed userId from path
   - Removed orderType from path

2. **Request Body Enhanced**
   - Added `userId` field
   - Added `currency` field (defaults to "USD")
   - Added `orderType` field
   - Conditionally adds `amount` or `quantity` based on orderType

3. **Consistency**
   - Both `buyShares()` and `sellShares()` follow same pattern
   - Easier to maintain and understand

#### Verify Test Compilation

```bash
cd /Users/margus/Git/claude-team-development-demo
./gradlew :services:integration-tests:compileGroovy
```

**Expected:** BUILD SUCCESSFUL

---

### Step 3: Local Verification (30-45 minutes)

#### 3.1 Start Docker Compose Environment

```bash
cd /Users/margus/Git/claude-team-development-demo
docker-compose up -d
```

**Wait for services to be healthy (about 60 seconds):**
```bash
docker-compose ps
```

**Expected:** All services should show "Up" status

#### 3.2 Verify Services Are Running

```bash
# Check trading service health
curl -s http://localhost:8087/q/health | jq

# Check API gateway health
curl -s http://localhost:8080/q/health | jq
```

**Expected:** Both should return `{"status": "UP", ...}`

#### 3.3 Run Integration Tests

```bash
cd /Users/margus/Git/claude-team-development-demo
./gradlew :services:integration-tests:test --tests "*"
```

**Expected Results:**
- Total tests: 45
- Passing: 40-43 (89-96%)
- Failing: 2-5 (infrastructure or timing issues)

**Look for these specific improvements:**
- ✅ Trading operations tests: Should now PASS
- ✅ Portfolio tracking tests: Should now PASS (dependency on trading resolved)
- ✅ User/wallet/exchange tests: Should still PASS (no regressions)

#### 3.4 Check Test Report

```bash
open services/integration-tests/build/reports/tests/test/index.html
```

**Verify:**
- `com.trading.integration.trading.BuyOrderSpec`: PASS
- `com.trading.integration.trading.SellOrderSpec`: PASS
- `com.trading.integration.trading.FractionalSharesSpec`: PASS
- `com.trading.integration.portfolio.PortfolioTrackingSpec`: PASS

#### 3.5 Manual API Testing (Optional but Recommended)

**Test buy endpoint directly:**
```bash
# Create test user first
USER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test-'$(date +%s)'@example.com",
    "username": "testuser'$(date +%s)'",
    "phoneNumber": "+1234567890"
  }')

USER_ID=$(echo $USER_RESPONSE | jq -r '.userId')
echo "Created user: $USER_ID"

# Wait for Kafka event processing
sleep 5

# Deposit funds
curl -s -X POST "http://localhost:8086/api/v1/wallets/${USER_ID}/deposit" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "USD",
    "amount": 1000.00
  }' | jq

# Execute buy order
curl -s -X POST http://localhost:8087/api/v1/trades/buy \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "'$USER_ID'",
    "symbol": "AAPL",
    "currency": "USD",
    "orderType": "BY_AMOUNT",
    "amount": 500.00
  }' | jq
```

**Expected Response:**
```json
{
  "id": "uuid",
  "userId": "uuid",
  "symbol": "AAPL",
  "tradeType": "BUY",
  "orderType": "BY_AMOUNT",
  "quantity": 2.5,
  "pricePerUnit": 195.00,
  "currency": "USD",
  "totalAmount": 500.00,
  "fees": 2.50,
  "status": "COMPLETED",
  "createdAt": "timestamp",
  "completedAt": "timestamp"
}
```

**Verify:**
- Response has `Content-Type: application/json` header
- Trade object returned successfully
- All fields populated correctly

#### 3.6 Test Through API Gateway

```bash
# Try same buy order through gateway
curl -s -X POST http://localhost:8080/api/v1/trades/buy \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "'$USER_ID'",
    "symbol": "GOOGL",
    "currency": "USD",
    "orderType": "BY_QUANTITY",
    "quantity": 5.0
  }' | jq
```

**Expected:** Similar successful response

**This verifies Gateway → Trading Service integration works!**

---

### Step 4: Troubleshooting Common Issues

#### Issue 1: Compilation Errors

**Problem:** `cannot find symbol: class OrderType`

**Solution:**
```bash
# Verify OrderType enum exists
grep -r "enum OrderType" shared/common-domain/
```

Expected to find it in `shared/common-domain/src/main/java/com/trading/platform/domain/OrderType.java`

If missing, check imports in TradingResource.java:
```java
import com.trading.platform.domain.*;
```

#### Issue 2: Test Failures - "Connection refused"

**Problem:** Services not running

**Solution:**
```bash
# Check service status
docker-compose ps

# Restart services if needed
docker-compose restart trading-service
docker-compose restart api-gateway

# Check logs
docker-compose logs trading-service
```

#### Issue 3: Test Failures - "Insufficient funds"

**Problem:** Test data not properly set up

**Solution:**
- Tests should clean database before running (check `cleanDatabase()` method)
- Verify `depositToWallet()` is called before `buyShares()`
- Check test execution order in spec files

#### Issue 4: Test Failures - "Cannot parse content"

**Problem:** Content-Type header still missing (shouldn't happen with our fix)

**Solution:**
```bash
# Test endpoint directly and check headers
curl -v -X POST http://localhost:8087/api/v1/trades/buy \
  -H "Content-Type: application/json" \
  -d '{...}' 2>&1 | grep -i content-type
```

Expected to see: `< Content-Type: application/json`

If missing, verify `@Produces(MediaType.APPLICATION_JSON)` annotation on `TradingResource` class.

#### Issue 5: Portfolio Tests Still Failing

**Problem:** Kafka event processing delay

**Solution:**
- Portfolio tests depend on Kafka events from trading service
- Verify `Thread.sleep(1000)` or `await()` statements in tests
- Check Kafka logs: `docker-compose logs kafka`
- Verify trading service publishes events: `docker-compose logs trading-service | grep -i kafka`

---

## Verification Checklist

Before moving to Q/A, verify:

- [ ] Trading Service compiles successfully
- [ ] Integration tests compile successfully
- [ ] Docker Compose environment running (all services "Up")
- [ ] Integration test suite runs
- [ ] **40+ integration tests passing (89%+ coverage)**
- [ ] Trading operation tests passing (was 0%, should be 100%)
- [ ] Portfolio tracking tests passing (dependency resolved)
- [ ] User/wallet/exchange tests still passing (no regressions)
- [ ] Manual API test successful (buy order through trading service)
- [ ] Manual API test successful (buy order through gateway)
- [ ] Content-Type headers present on responses
- [ ] No errors in service logs

---

## Creating Q/A Handoff Document

After verification is complete, create `docs/07_q_a.md`:

```bash
cat > docs/07_q_a.md << 'EOF'
# Step 07: Q/A Testing Instructions

**Date:** 2026-01-14
**From:** Developer
**To:** Q/A Specialist
**Status:** Ready for Q/A Validation

## Changes Implemented

1. ✅ Refactored Trading Service endpoints (4 → 2 endpoints)
2. ✅ Updated integration test helpers
3. ✅ Verified locally: 40+ tests passing

## Integration Test Results (Local)

**Before Fix:**
- Total: 45 tests
- Passing: 17 (37.8%)
- Failing: 28 (62.2%)

**After Fix:**
- Total: 45 tests
- Passing: [YOUR_RESULT] ([YOUR_PERCENTAGE]%)
- Failing: [YOUR_RESULT] ([YOUR_PERCENTAGE]%)

## Q/A Testing Tasks

### 1. Run Full Integration Test Suite
```bash
./gradlew :services:integration-tests:test
```

**Expected:** 40+ tests passing (89%+)

### 2. Verify Trading Operations
- ✅ Buy by amount
- ✅ Buy by quantity
- ✅ Sell by amount
- ✅ Sell by quantity
- ✅ Portfolio tracking after trades

### 3. Regression Testing
- ✅ User registration still works
- ✅ Wallet operations still work
- ✅ Currency exchange still works

### 4. Manual Exploratory Testing
- Test trading through API Gateway
- Test with invalid inputs
- Test with insufficient funds scenarios
- Verify error messages are clear

## Known Issues

[Document any issues you encountered during development]

## Developer Sign-Off

**Status:** ✅ Ready for Q/A
**Developer:** [Your Name]
**Date:** 2026-01-14
EOF
```

**Customize this template with your actual test results before sending to Q/A.**

---

## Expected Timeline

| Phase | Time | Status |
|-------|------|--------|
| **Step 1:** Update Trading Service | 1-1.5 hours | ⏳ |
| **Step 2:** Update Integration Tests | 30 minutes | ⏳ |
| **Step 3:** Local Verification | 30-45 minutes | ⏳ |
| **Step 4:** Q/A Handoff Doc | 15 minutes | ⏳ |
| **Total** | **2.5-3 hours** | |

---

## Success Criteria

**Development phase complete when:**
1. ✅ All code changes implemented
2. ✅ No compilation errors
3. ✅ 40+ integration tests passing locally
4. ✅ No regressions in existing tests
5. ✅ Manual API testing successful
6. ✅ Q/A handoff document created

---

## Getting Help

**If you encounter issues:**

1. **Check Senior Engineer analysis:** `docs/07_discussion.md` - Contains architectural decisions and rationale
2. **Check service logs:** `docker-compose logs [service-name]`
3. **Compare with working services:** Review `UserResource.java` or `WalletResource.java` for reference patterns
4. **Review test failures:** Test reports in `services/integration-tests/build/reports/tests/test/`

**Common questions answered in `docs/07_discussion.md`:**
- Why consolidate endpoints?
- Why move userId to request body?
- Why is this better than the old design?
- What are the risks?

---

## Commit Message Template

After successful local verification and before Q/A handoff:

```bash
git add services/trading-service/src/main/java/com/trading/platform/trading/resource/TradingResource.java
git add services/integration-tests/src/test/groovy/com/trading/integration/BaseIntegrationSpec.groovy
git add docs/07_dev.md
git add docs/07_discussion.md
git add docs/07_q_a.md

git commit -m "$(cat <<'EOF'
Step 07: Refactor Trading Service API to fix integration tests

Problem:
- 21+ trading integration tests failing
- Root cause: API contract mismatch between Trading Service, Gateway, and Tests
- Tests calling non-existent endpoints, getting responses without Content-Type headers

Solution:
- Consolidated 4 endpoints into 2 (/buy, /sell)
- Moved userId from path params to request body
- Created unified TradeRequest DTO
- Updated integration test helpers to match new API

Results:
- Integration tests: [X]/45 passing ([Y]%)
- Trading operations: Fully validated
- Portfolio tracking: Dependency resolved
- No regressions in user/wallet/exchange features

Testing:
- Local integration tests: PASS
- Manual API testing: PASS
- API Gateway integration: PASS

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"
```

---

## Next Steps

1. **[You]** Implement changes in Steps 1-2
2. **[You]** Verify locally in Step 3
3. **[You]** Create Q/A handoff document
4. **[You]** Commit changes
5. **[Q/A]** Run comprehensive testing
6. **[Q/A]** Approve or loop back

---

**Document created:** 2026-01-14
**Senior Engineer:** Claude Sonnet 4.5
**For:** Developer implementation
**Status:** Ready for development

**Good luck! This is a high-value fix that will unlock full validation of our core trading platform functionality.**
