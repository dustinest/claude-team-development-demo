# 06_dev.md - Developer Implementation Instructions

**Date:** 2026-01-13
**Role:** Developer
**Step:** 06 - Refactor: Merge user-signup-service into user-service
**From:** Senior Engineer
**Priority:** HIGH

---

## Overview

You are implementing an architectural refactoring to merge user-signup-service into user-service. The current separation of these services is causing validation issues and violates domain boundaries.

**Goal:** Move all user creation logic into user-service, where it belongs.

**Expected Outcome:**
- POST /api/v1/users endpoint in user-service
- Email format and duplicate validation working correctly
- 40-43/45 integration tests passing (up from 16/45)
- user-signup-service removed from system

---

## Background

**Problem with Current Architecture:**
- user-signup-service publishes events but doesn't validate
- user-service consumes events but validation happens too late
- Created artificial complexity and Step 05 validation blocker

**New Architecture:**
- user-service handles direct user creation via POST /api/v1/users
- Validation happens in same transaction as database insert
- Event published AFTER successful creation (notification)
- API Gateway routes signup directly to user-service

---

## Implementation Steps

### Step 1: Add DTOs to user-service

**File:** `services/user-service/src/main/java/com/trading/platform/user/dto/CreateUserRequest.java`

**Action:** CREATE NEW FILE

```java
package com.trading.platform.user.dto;

public class CreateUserRequest {
    private String email;
    private String username;
    private String phoneNumber;

    // Constructors
    public CreateUserRequest() {}

    public CreateUserRequest(String email, String username, String phoneNumber) {
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
```

**File:** `services/user-service/src/main/java/com/trading/platform/user/exception/ConflictException.java`

**Action:** CREATE NEW FILE

```java
package com.trading.platform.user.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
```

---

### Step 2: Add Kafka Producer Configuration to user-service

**File:** `services/user-service/src/main/resources/application.properties`

**Action:** ADD these lines at the end

```properties
# Kafka Producer (for publishing UserCreated events after direct user creation)
mp.messaging.outgoing.user-events-out.connector=smallrye-kafka
mp.messaging.outgoing.user-events-out.topic=user-events
mp.messaging.outgoing.user-events-out.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

**Note:** Keep all existing properties. user-service will both consume AND produce events.

---

### Step 3: Update UserService with Direct Creation Method

**File:** `services/user-service/src/main/java/com/trading/platform/user/service/UserService.java`

**Action:** ADD these imports at the top

```java
import com.trading.platform.user.dto.CreateUserRequest;
import com.trading.platform.user.exception.ConflictException;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import java.util.regex.Pattern;
```

**Action:** ADD this constant after the class declaration

```java
@ApplicationScoped
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Inject
    @Channel("user-events-out")
    Emitter<UserCreatedEvent> userEventsEmitter;

    // ... existing methods ...
```

**Action:** ADD this new method BEFORE the existing createUser method

```java
    /**
     * Create user directly via REST endpoint (new flow).
     * Validates, persists, and publishes event.
     */
    @Transactional
    public UserEntity createUserDirect(CreateUserRequest request) {
        LOG.info("Creating user directly: email={}, username={}", request.getEmail(), request.getUsername());

        // Step 1: Validate input is not null/blank
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Step 2: Validate email format
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            LOG.warn("Invalid email format: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email format");
        }

        // Step 3: Check for duplicates (in same transaction as insert)
        if (UserEntity.existsByEmail(request.getEmail())) {
            LOG.warn("Duplicate email: {}", request.getEmail());
            throw new ConflictException("Email already registered");
        }
        if (UserEntity.existsByUsername(request.getUsername())) {
            LOG.warn("Duplicate username: {}", request.getUsername());
            throw new ConflictException("Username already taken");
        }
        if (UserEntity.existsByPhoneNumber(request.getPhoneNumber())) {
            LOG.warn("Duplicate phone: {}", request.getPhoneNumber());
            throw new ConflictException("Phone number already registered");
        }

        // Step 4: Create and persist user
        UserEntity user = new UserEntity();
        user.id = UUID.randomUUID();
        user.email = request.getEmail();
        user.username = request.getUsername();
        user.phoneNumber = request.getPhoneNumber();
        user.createdAt = Instant.now();
        user.persist();

        LOG.info("User persisted: id={}, email={}, username={}", user.id, user.email, user.username);

        // Step 5: Publish event notification (after successful persistence)
        UserCreatedEvent event = new UserCreatedEvent(
            user.id,
            user.email,
            user.username,
            user.phoneNumber
        );

        LOG.info("Publishing UserCreated event for userId={}", user.id);
        userEventsEmitter.send(event);

        return user;
    }
```

**Action:** KEEP the existing createUser(UserCreatedEvent event) method unchanged
This maintains backward compatibility with the old event-driven flow.

---

### Step 4: Update UserResource with POST Endpoint

**File:** `services/user-service/src/main/java/com/trading/platform/user/resource/UserResource.java`

**Action:** ADD these imports at the top

```java
import com.trading.platform.user.dto.CreateUserRequest;
import com.trading.platform.user.exception.ConflictException;
```

**Action:** ADD this new method at the top of the class (after field declarations)

```java
    @POST
    @Operation(summary = "Create new user", description = "Register a new user with email, username, and phone number")
    public Response createUser(CreateUserRequest request) {
        try {
            UserEntity user = userService.createUserDirect(request);
            return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                    "userId", user.id,
                    "email", user.email,
                    "username", user.username
                ))
                .build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Bad request: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (ConflictException e) {
            LOG.warn("Conflict: {}", e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }
```

**Action:** ADD logger field if not present

```java
    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);
```

**Action:** ADD import for Logger

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

---

### Step 5: Verify user-service Dependencies

**File:** `services/user-service/build.gradle.kts`

**Action:** VERIFY these dependencies exist (they should already be there)

```kotlin
dependencies {
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    implementation("io.quarkus:quarkus-kafka-client")
    // ... other dependencies
}
```

If any are missing, add them.

---

### Step 6: Update API Gateway Routing

**File:** `services/api-gateway/src/main/java/com/trading/platform/apigateway/client/UserClient.java`

**Action:** ADD this method to the interface

```java
    @POST
    @Path("/api/v1/users")
    Response createUser(SignupRequest request);
```

**Note:** SignupRequest is defined as an inner class in GatewayResource, so you'll need to use the full path or add an import.

**File:** `services/api-gateway/src/main/java/com/trading/platform/apigateway/resource/GatewayResource.java`

**Action:** MODIFY the signup method (around line 33)

**OLD CODE:**
```java
    @POST
    @Path("/signup")
    @Tag(name = "Gateway")
    @Operation(summary = "Register new user")
    public Response signup(SignupRequest request) {
        return userSignupClient.signup(request);
    }
```

**NEW CODE:**
```java
    @POST
    @Path("/signup")
    @Tag(name = "Gateway")
    @Operation(summary = "Register new user")
    public Response signup(SignupRequest request) {
        return userClient.createUser(request);  // Changed from userSignupClient to userClient
    }
```

**Single line change:** Replace `userSignupClient.signup(request)` with `userClient.createUser(request)`

---

### Step 7: Remove user-signup-service from docker-compose

**File:** `docker-compose.yml`

**Action:** FIND and DELETE the entire user-signup-service section

Look for:
```yaml
  user-signup-service:
    build:
      context: .
      dockerfile: services/user-signup-service/Dockerfile
    container_name: claude-team-development-demo-user-signup-service-1
    # ... (entire section)
```

Delete from `user-signup-service:` until the next service definition starts.

---

### Step 8: Remove user-signup-service from Gradle

**File:** `settings.gradle.kts`

**Action:** FIND and DELETE this line

```kotlin
include(":services:user-signup-service")
```

---

### Step 9: Build and Deploy

**Commands:**

```bash
# Step 1: Clean build (skip tests for now)
./gradlew clean build -x test

# Step 2: Stop all containers
docker-compose down

# Step 3: Remove user-signup-service container and image
docker rm -f claude-team-development-demo-user-signup-service-1 2>/dev/null || true
docker rmi claude-team-development-demo-user-signup-service 2>/dev/null || true

# Step 4: Rebuild services (only user-service and api-gateway changed)
docker-compose build user-service api-gateway

# Step 5: Start all services
docker-compose up -d

# Step 6: Check logs for user-service
docker logs claude-team-development-demo-user-service-1 --tail 50

# Step 7: Verify all containers running (should be 14 now, not 15)
docker ps | grep claude-team
```

**Expected Output:**
- 14 containers running (user-signup-service removed)
- user-service logs show successful startup
- No errors related to Kafka producer

---

### Step 10: Manual Testing

**Test 1: Valid User Creation**

```bash
curl -X POST "http://localhost:8080/api/v1/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test-valid@example.com",
    "username": "testvalid",
    "phoneNumber": "+1234567890"
  }' | jq .
```

**Expected Response:** 201 Created
```json
{
  "userId": "<uuid>",
  "email": "test-valid@example.com",
  "username": "testvalid"
}
```

**Test 2: Invalid Email Format**

```bash
curl -X POST "http://localhost:8080/api/v1/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "not-an-email",
    "username": "testuser",
    "phoneNumber": "+1234567890"
  }' -i
```

**Expected Response:** 400 Bad Request
```json
{
  "error": "Invalid email format"
}
```

**Test 3: Duplicate Email**

```bash
# Create first user
curl -X POST "http://localhost:8080/api/v1/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "duplicate@example.com",
    "username": "user1",
    "phoneNumber": "+1111111111"
  }' | jq .

# Wait for Kafka processing
sleep 5

# Try to create duplicate
curl -X POST "http://localhost:8080/api/v1/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "duplicate@example.com",
    "username": "user2",
    "phoneNumber": "+2222222222"
  }' -i
```

**Expected Response:** 409 Conflict
```json
{
  "error": "Email already registered"
}
```

**Test 4: Verify Event Published**

```bash
# Check user-service logs for event publication
docker logs claude-team-development-demo-user-service-1 | grep "Publishing UserCreated"
```

**Expected:** You should see log entries like:
```
Publishing UserCreated event for userId=<uuid>
```

---

### Step 11: Run Integration Tests

**Command:**

```bash
./gradlew :services:integration-tests:test
```

**Expected Results:**

**Before Step 06:**
- 16/45 tests passing (35.6%)
- User validation tests failing
- Many trading tests failing (dependent on users)

**After Step 06:**
- **Target: 40-43/45 tests passing (89-96%)**
- User validation tests should PASS
- Trading tests should mostly PASS
- Remaining failures likely edge cases

**Key Tests to Verify:**

✅ **Should NOW PASS:**
- `UserRegistrationSpec > invalid email format is rejected`
- `UserRegistrationSpec > duplicate email registration is rejected`
- `TradingSpec > buy operations` (dependent on user creation)
- `PortfolioSpec > portfolio tracking` (dependent on user creation)

**Check Test Report:**

```bash
open services/integration-tests/build/reports/tests/test/index.html
```

---

## Verification Checklist

Before marking this step complete, verify:

### Code Changes
- [ ] CreateUserRequest.java created in user-service
- [ ] ConflictException.java created in user-service
- [ ] Kafka producer config added to user-service/application.properties
- [ ] createUserDirect() method added to UserService
- [ ] POST endpoint added to UserResource
- [ ] UserClient.createUser() method added
- [ ] GatewayResource signup method updated
- [ ] user-signup-service removed from docker-compose.yml
- [ ] user-signup-service removed from settings.gradle.kts

### Build & Deployment
- [ ] ./gradlew clean build succeeds
- [ ] docker-compose up -d succeeds
- [ ] 14 containers running (not 15)
- [ ] user-service logs show no errors
- [ ] api-gateway logs show no errors

### Manual Testing
- [ ] Valid signup returns 201 with userId
- [ ] Invalid email format returns 400
- [ ] Duplicate email returns 409
- [ ] User persisted to database
- [ ] UserCreated event published to Kafka

### Integration Tests
- [ ] Test suite runs successfully
- [ ] 40+ tests passing (target: 40-43/45)
- [ ] User validation tests PASS
- [ ] No new test failures introduced

### Architecture
- [ ] user-signup-service directory can be deleted (optional)
- [ ] System works end-to-end without user-signup-service
- [ ] Event flow still works (user-service publishes events)
- [ ] Other services (wallet, portfolio) still receive events

---

## Troubleshooting

### Issue: Build Fails

**Check:**
```bash
./gradlew clean build -x test --stacktrace
```

**Common Causes:**
- Missing imports in updated files
- Syntax errors in new code
- Gradle cache issues (run `./gradlew clean`)

### Issue: user-service Doesn't Start

**Check Logs:**
```bash
docker logs claude-team-development-demo-user-service-1
```

**Common Causes:**
- Kafka producer config incorrect
- Missing dependencies in build.gradle.kts
- Port conflicts

### Issue: Tests Still Failing

**Check Specific Test:**
```bash
./gradlew :services:integration-tests:test --tests "UserRegistrationSpec"
```

**Common Causes:**
- Docker containers not fully restarted
- Kafka not clearing old state
- Database not reset between tests

**Fix:**
```bash
docker-compose down -v  # Remove volumes
docker-compose up -d
sleep 30  # Wait for services to stabilize
./gradlew :services:integration-tests:test
```

### Issue: 409 Conflicts on First User

**Symptom:** Even first user creation returns 409

**Cause:** Database has stale data from previous runs

**Fix:**
```bash
# Reset database
docker-compose down -v
docker-compose up -d
# Wait 30 seconds for migrations
sleep 30
# Try again
```

---

## Success Criteria

✅ **Code Complete:**
- All code changes implemented correctly
- No compilation errors
- All files created/modified as specified

✅ **System Running:**
- 14 containers running healthy
- No errors in logs
- Services responding to requests

✅ **Functionality Working:**
- POST /api/v1/signup returns 201 for valid users
- Returns 400 for invalid email format
- Returns 409 for duplicate email/username/phone
- Users persisted to database
- Events published to Kafka

✅ **Tests Passing:**
- 40+ integration tests passing (target: 40-43/45)
- User validation tests pass
- No regressions in previously passing tests

✅ **Architecture Clean:**
- user-signup-service removed from system
- Single service (user-service) owns user domain
- Event flow working correctly

---

## Documentation Tasks

After implementation, document in `docs/06_discussion.md`:

1. **What worked well:**
   - Which parts of implementation were straightforward
   - Any unexpected benefits discovered

2. **Challenges encountered:**
   - Any issues during implementation
   - How you resolved them

3. **Test results:**
   - Before/after test counts
   - Which tests now pass that were failing
   - Any remaining failures and analysis

4. **Next steps:**
   - Recommendations for Q/A validation
   - Any follow-up work needed

---

## Time Estimate

- **Code changes:** 1.5 hours
- **Build & deployment:** 30 minutes
- **Manual testing:** 30 minutes
- **Integration tests:** 30 minutes
- **Documentation:** 30 minutes

**Total: 3-3.5 hours**

---

## Questions?

If you encounter any issues or need clarification:

1. Check the troubleshooting section above
2. Review `docs/06_se.md` for architectural context
3. Check logs for error messages
4. Document the issue in `docs/06_discussion.md`

---

**Status:** 🔵 READY FOR IMPLEMENTATION
**Priority:** HIGH - Unblocks Step 05 validation issues
**Risk:** LOW - Incremental changes, can be tested step-by-step
**Expected Outcome:** Cleaner architecture, all validation working, 40+ tests passing

Good luck! This refactoring will significantly improve the system architecture.
