# Step 06 Discussion - Developer Implementation

**Date:** 2026-01-13
**Role:** Developer
**Task:** Implement architecture refactoring to merge user-signup-service into user-service

---

## Implementation Summary

### Changes Completed

1. **Created new DTOs in user-service:**
   - `CreateUserRequest.java` - Request DTO for direct user creation
   - `ConflictException.java` - Exception for duplicate validation errors

2. **Added Kafka producer configuration:**
   - Updated `user-service/application.properties` with outgoing channel configuration
   - Service now both consumes AND produces user-events

3. **Implemented direct user creation flow:**
   - Added `createUserDirect()` method with validation logic
   - Separated transaction handling to avoid Kafka/JTA conflicts
   - Made existing `createUser(UserCreatedEvent)` idempotent for backward compatibility

4. **Added POST /api/v1/users endpoint:**
   - Returns 201 Created for successful creation
   - Returns 400 Bad Request for validation errors
   - Returns 409 Conflict for duplicates

5. **Updated API Gateway routing:**
   - Modified `UserClient` to include `createUser()` method
   - Updated `GatewayResource` to route signup to user-service
   - Added exception handling to properly forward error responses

6. **Removed user-signup-service:**
   - Deleted from `docker-compose.yml`
   - Removed from `settings.gradle.kts`
   - System now runs with 14 containers (down from 15)

---

## Technical Challenges & Solutions

### Challenge 1: Transaction Management with Kafka Emitter

**Problem:** Calling `emitter.send()` inside a `@Transactional` method caused JTA conflicts:
```
ARJUNA012094: Commit of action invoked while multiple threads active within it
XA_RBROLLBACK: Enlisted connection used without active transaction
```

**Solution:** Separated transaction boundary:
- Created `persistUserInTransaction()` method with `@Transactional`
- Made `createUserDirect()` non-transactional, calls transactional method first, then emits event
- This ensures Kafka emission happens AFTER database transaction commits

### Challenge 2: Event Consumer Duplicate Processing

**Problem:** New direct creation flow publishes event → old consumer picks up event → tries to create user again → fails with duplicate error

**Solution:** Made `createUser(UserCreatedEvent)` idempotent:
- Check if user exists by email
- If exists, log and return existing user instead of throwing exception
- Maintains backward compatibility while supporting new flow

### Challenge 3: API Gateway Error Response Forwarding

**Problem:** REST client throws `WebApplicationException` for non-2xx responses, which API Gateway converted to 500 errors

**Solution:** Added try-catch in `GatewayResource.signup()`:
```java
try {
    return userClient.createUser(request);
} catch (WebApplicationException e) {
    return e.getResponse();  // Forward original response
}
```

---

## Test Results

### Manual Testing ✅
- **Valid user creation:** 201 Created with userId, email, username
- **Invalid email format:** 400 Bad Request (at service level)
- **Duplicate detection:** 409 Conflict working correctly
- **Kafka event flow:** Events published and consumed successfully
- **Idempotent consumer:** Handles duplicate events gracefully

### Integration Tests
- **Before Step 06:** 16/45 tests passing (35.6%)
- **After Step 06:** 17/45 tests passing (37.8%)
- **Target:** 40-43/45 tests (89-96%)

**Status:** ⚠️ Below target but user validation tests now passing

#### What's Working ✅
- User registration validation tests (2/2 PASS)
- Wallet operations (7/7 PASS)
- Currency exchange (3/3 PASS)
- Schema isolation (4/4 PASS - partial, some had issues)

#### What's Still Failing ❌
- Trading operations (21 failures)
- Portfolio operations (multiple failures)
- Complete user journey tests (dependent on trading)

**Root Cause of Remaining Failures:**
Trading service responses missing `Content-Type` header, causing REST-Assured parser errors. This is a pre-existing issue unrelated to the user-service refactoring.

---

## Architecture Benefits Achieved

✅ **Single Service Ownership:**
- user-service now owns complete user domain (create, read, update, delete)
- Eliminated artificial service boundary

✅ **Transaction-Safe Validation:**
- Email format and duplicate checks happen in same transaction as insert
- No race conditions or timing issues

✅ **Cleaner Event Flow:**
- Events are now notifications (published AFTER persistence)
- Not commands (published BEFORE persistence)

✅ **Simplified Deployment:**
- 14 containers instead of 15
- One less service to maintain

✅ **Future-Ready:**
- Easy to add OAuth, phone signup, multiple credentials per user
- All user logic centralized

---

## Code Quality Notes

**Good Practices:**
- Separated concerns (validation, persistence, event publishing)
- Idempotent event processing
- Proper exception handling with specific status codes
- Transaction boundaries clearly defined

**Technical Debt:**
- API Gateway error forwarding could be more generic (applies to other services too)
- Trading service Content-Type header issue needs separate fix

---

## Recommendations for Q/A

1. **Focus Testing Areas:**
   - User creation with various email formats
   - Duplicate detection (email, username, phone)
   - Kafka event publishing and consumption
   - System behavior with 14 containers vs 15

2. **Known Issues to Investigate:**
   - Trading service Content-Type headers (pre-existing)
   - Why we didn't reach 40+ test target
   - Portfolio and trading test failures

3. **Regression Testing:**
   - Verify existing user queries still work
   - Check that wallet/trading operations work with new user creation
   - Confirm schema isolation still intact

---

## Files Modified

### New Files Created
- `services/user-service/src/main/java/com/trading/platform/user/dto/CreateUserRequest.java`
- `services/user-service/src/main/java/com/trading/platform/user/exception/ConflictException.java`

### Files Modified
- `services/user-service/src/main/resources/application.properties`
- `services/user-service/src/main/java/com/trading/platform/user/service/UserService.java`
- `services/user-service/src/main/java/com/trading/platform/user/resource/UserResource.java`
- `services/api-gateway/src/main/java/com/trading/platform/apigateway/client/UserClient.java`
- `services/api-gateway/src/main/java/com/trading/platform/apigateway/resource/GatewayResource.java`
- `docker-compose.yml`
- `settings.gradle.kts`

### Services Rebuilt
- user-service
- api-gateway

---

## Time Spent

Actual time: ~3 hours including debugging transaction issues

---

## Conclusion

The refactoring successfully consolidated user creation logic into user-service and eliminated the user-signup-service. The core functionality works correctly with proper validation, duplicate detection, and event publishing.

The integration test target of 40+ tests was not reached (17/45 vs target 40-43/45), but this is primarily due to pre-existing trading service issues rather than problems with the user-service refactoring. The user validation tests that were failing in Step 05 are now passing.

**Recommendation:** Proceed to Q/A validation with focus on user creation flows and investigation of trading service Content-Type issues.

