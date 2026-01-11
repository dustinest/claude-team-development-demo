# 01_dev.md - Instructions for Developer

## Overview

This document provides detailed implementation instructions for building the Fractional Stock Trading Platform. You will implement 10 microservices, a React frontend, and supporting infrastructure.

## Prerequisites

Before starting, ensure you have:
- Java 21 JDK installed
- Gradle 8.x installed
- Docker and Docker Compose installed
- Node.js 18+ and npm installed (for frontend)
- IDE with Java 21 support (IntelliJ IDEA recommended)

## Project Structure

Create the following directory structure:

```
<project-root>/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── docker-compose.yml
├── README.md
├── docs/
│   ├── 01_setup.md
│   ├── 01_discussion.md
│   ├── 01_se.md
│   ├── 01_dev.md (this file)
│   └── 01_summary.md
├── services/
│   ├── api-gateway/
│   ├── user-service/
│   ├── wallet-service/
│   ├── trading-service/
│   ├── portfolio-service/
│   ├── transaction-history-service/
│   ├── fee-service/
│   ├── securities-pricing-service/
│   ├── currency-exchange-service/
│   └── user-signup-service/
├── shared/
│   ├── common-domain/
│   └── common-events/
└── frontend/
    └── trading-platform-ui/
```

## Phase 1: Project Setup

### Step 1.1: Root Gradle Configuration

Create `settings.gradle.kts`:

```kotlin
rootProject.name = "fractional-trading-platform"

include(
    // Shared modules
    "shared:common-domain",
    "shared:common-events",

    // Services
    "services:api-gateway",
    "services:user-service",
    "services:wallet-service",
    "services:trading-service",
    "services:portfolio-service",
    "services:transaction-history-service",
    "services:fee-service",
    "services:securities-pricing-service",
    "services:currency-exchange-service",
    "services:user-signup-service"
)
```

Create root `build.gradle.kts`:

```kotlin
plugins {
    java
    id("io.quarkus") version "3.6.4" apply false
}

allprojects {
    group = "com.trading.platform"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        // Common dependencies for all modules
        val implementation by configurations
        val testImplementation by configurations

        // Logging
        implementation("org.slf4j:slf4j-api:2.0.9")

        // Testing - Spock/Groovy
        testImplementation("org.spockframework:spock-core:2.4-M1-groovy-4.0")
        testImplementation("org.apache.groovy:groovy-all:4.0.15")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
```

Create `gradle.properties`:

```properties
quarkusVersion=3.6.4
quarkusPlatformGroupId=io.quarkus.platform
quarkusPlatformArtifactId=quarkus-bom
kotlinVersion=1.9.21
```

### Step 1.2: Shared Modules

#### Shared: common-domain

Create `shared/common-domain/build.gradle.kts`:

```kotlin
plugins {
    `java-library`
}

dependencies {
    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.0")
}
```

Create domain objects in `shared/common-domain/src/main/java/com/trading/platform/domain/`:

**Currency.java**:
```java
package com.trading.platform.domain;

public enum Currency {
    USD, EUR, GBP
}
```

**SecurityType.java**:
```java
package com.trading.platform.domain;

public enum SecurityType {
    STOCK, STOCK_INDEX, BOND_INDEX
}
```

**TradeType.java**:
```java
package com.trading.platform.domain;

public enum TradeType {
    BUY, SELL
}
```

**OrderType.java**:
```java
package com.trading.platform.domain;

public enum OrderType {
    BY_AMOUNT,    // User specifies money amount
    BY_QUANTITY   // User specifies security quantity
}
```

**TradeStatus.java**:
```java
package com.trading.platform.domain;

public enum TradeStatus {
    PENDING, COMPLETED, FAILED
}
```

**TransactionType.java**:
```java
package com.trading.platform.domain;

public enum TransactionType {
    DEPOSIT, WITHDRAWAL, BUY, SELL, CURRENCY_EXCHANGE
}
```

**MoneyCalculator.java** (Critical for customer-favorable rounding):
```java
package com.trading.platform.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyCalculator {
    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 6;

    // Round money amounts to 2 decimal places
    public static BigDecimal roundMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    // Round in customer's favor for purchases (round up quantity)
    public static BigDecimal roundQuantityForBuy(BigDecimal quantity) {
        return quantity.setScale(MONEY_SCALE, RoundingMode.UP);
    }

    // Round in customer's favor for sales (round up proceeds)
    public static BigDecimal roundAmountForSell(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.UP);
    }

    // Exchange rate precision
    public static BigDecimal roundRate(BigDecimal rate) {
        return rate.setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    // Convert currency
    public static BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        return roundMoney(amount.multiply(rate));
    }
}
```

#### Shared: common-events

Create `shared/common-events/build.gradle.kts`:

```kotlin
plugins {
    `java-library`
}

dependencies {
    implementation(project(":shared:common-domain"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.0")
}
```

Create event classes in `shared/common-events/src/main/java/com/trading/platform/events/`:

**BaseEvent.java**:
```java
package com.trading.platform.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UserCreatedEvent.class, name = "UserCreated"),
    @JsonSubTypes.Type(value = WalletUpdatedEvent.class, name = "WalletUpdated"),
    @JsonSubTypes.Type(value = DepositCompletedEvent.class, name = "DepositCompleted"),
    @JsonSubTypes.Type(value = WithdrawalCompletedEvent.class, name = "WithdrawalCompleted"),
    @JsonSubTypes.Type(value = CurrencyExchangedEvent.class, name = "CurrencyExchanged"),
    @JsonSubTypes.Type(value = TradeCompletedEvent.class, name = "TradeCompleted"),
    @JsonSubTypes.Type(value = TradeFailedEvent.class, name = "TradeFailed")
})
public abstract class BaseEvent {
    private String eventId = UUID.randomUUID().toString();
    private Instant timestamp = Instant.now();

    // Getters and setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
```

**UserCreatedEvent.java**:
```java
package com.trading.platform.events;

import java.util.UUID;

public class UserCreatedEvent extends BaseEvent {
    private UUID userId;
    private String email;
    private String username;
    private String phoneNumber;

    // Constructors, getters, setters
    public UserCreatedEvent() {}

    public UserCreatedEvent(UUID userId, String email, String username, String phoneNumber) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    // Getters and setters omitted for brevity - add them all
}
```

**TradeCompletedEvent.java**:
```java
package com.trading.platform.events;

import com.trading.platform.domain.Currency;
import com.trading.platform.domain.TradeType;
import java.math.BigDecimal;
import java.util.UUID;

public class TradeCompletedEvent extends BaseEvent {
    private UUID tradeId;
    private UUID userId;
    private String symbol;
    private TradeType tradeType;
    private BigDecimal quantity;
    private BigDecimal pricePerUnit;
    private Currency currency;
    private BigDecimal totalAmount;
    private BigDecimal fees;

    // Constructors, getters, setters - add them all
}
```

Create similar event classes for:
- **WalletUpdatedEvent**
- **DepositCompletedEvent**
- **WithdrawalCompletedEvent**
- **CurrencyExchangedEvent**
- **TradeFailedEvent**

### Step 1.3: Service Template

Each microservice follows this structure:

```
service-name/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── java/com/trading/platform/<service>/
│   │   │   ├── entity/         # JPA entities
│   │   │   ├── repository/     # Repositories
│   │   │   ├── service/        # Business logic
│   │   │   ├── resource/       # REST endpoints (JAX-RS)
│   │   │   ├── messaging/      # Kafka consumers/producers
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   └── config/         # Configuration classes
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   └── db/migration/   # Flyway migrations
│   │   └── docker/
│   │       └── Dockerfile.jvm
│   └── test/
│       ├── groovy/com/trading/platform/<service>/
│       │   └── (Spock tests)
│       └── resources/
│           └── application-test.properties
└── README.md
```

Standard `build.gradle.kts` for Quarkus services:

```kotlin
plugins {
    java
    id("io.quarkus")
}

dependencies {
    // Quarkus BOM
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.6.4"))

    // Quarkus extensions
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-kafka")

    // Shared modules
    implementation(project(":shared:common-domain"))
    implementation(project(":shared:common-events"))

    // Testing
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-test-h2")
}

tasks.test {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
```

## Phase 2: Implement Mock Services

Start with mock services as they have no dependencies.

### Service 1: Securities Pricing Service (Mock)

**Entity: `Security.java`**:
```java
package com.trading.platform.pricing.entity;

import com.trading.platform.domain.SecurityType;
import java.math.BigDecimal;
import java.time.Instant;

public class Security {
    private String symbol;
    private String name;
    private SecurityType type;
    private BigDecimal currentPrice;
    private Instant lastUpdated;

    // Constructor, getters, setters
}
```

**Service: `PricingService.java`**:
```java
package com.trading.platform.pricing.service;

import com.trading.platform.domain.SecurityType;
import com.trading.platform.pricing.entity.Security;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import io.quarkus.scheduler.Scheduled;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PricingService {
    private final Map<String, Security> securities = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        // Initialize 10 stocks
        addSecurity("AAPL", "Apple Inc.", SecurityType.STOCK, new BigDecimal("175.50"));
        addSecurity("GOOGL", "Alphabet Inc.", SecurityType.STOCK, new BigDecimal("140.25"));
        addSecurity("MSFT", "Microsoft Corp.", SecurityType.STOCK, new BigDecimal("380.00"));
        addSecurity("AMZN", "Amazon.com Inc.", SecurityType.STOCK, new BigDecimal("155.75"));
        addSecurity("TSLA", "Tesla Inc.", SecurityType.STOCK, new BigDecimal("245.30"));
        addSecurity("META", "Meta Platforms", SecurityType.STOCK, new BigDecimal("485.00"));
        addSecurity("NVDA", "NVIDIA Corp.", SecurityType.STOCK, new BigDecimal("495.25"));
        addSecurity("JPM", "JPMorgan Chase", SecurityType.STOCK, new BigDecimal("180.50"));
        addSecurity("JNJ", "Johnson & Johnson", SecurityType.STOCK, new BigDecimal("160.00"));
        addSecurity("V", "Visa Inc.", SecurityType.STOCK, new BigDecimal("275.75"));

        // Initialize 5 stock indexes
        addSecurity("SPY", "S&P 500 ETF", SecurityType.STOCK_INDEX, new BigDecimal("480.00"));
        addSecurity("QQQ", "NASDAQ 100 ETF", SecurityType.STOCK_INDEX, new BigDecimal("410.50"));
        addSecurity("DIA", "Dow Jones ETF", SecurityType.STOCK_INDEX, new BigDecimal("380.25"));
        addSecurity("IWM", "Russell 2000 ETF", SecurityType.STOCK_INDEX, new BigDecimal("198.75"));
        addSecurity("VTI", "Total Market ETF", SecurityType.STOCK_INDEX, new BigDecimal("245.00"));

        // Initialize 5 bond indexes
        addSecurity("AGG", "US Aggregate Bond", SecurityType.BOND_INDEX, new BigDecimal("102.50"));
        addSecurity("TLT", "20+ Year Treasury", SecurityType.BOND_INDEX, new BigDecimal("95.75"));
        addSecurity("BND", "Total Bond Market", SecurityType.BOND_INDEX, new BigDecimal("78.25"));
        addSecurity("LQD", "Investment Grade Corp", SecurityType.BOND_INDEX, new BigDecimal("110.00"));
        addSecurity("HYG", "High Yield Corp", SecurityType.BOND_INDEX, new BigDecimal("82.50"));
    }

    private void addSecurity(String symbol, String name, SecurityType type, BigDecimal price) {
        Security security = new Security();
        security.setSymbol(symbol);
        security.setName(name);
        security.setType(type);
        security.setCurrentPrice(price);
        security.setLastUpdated(Instant.now());
        securities.put(symbol, security);
    }

    @Scheduled(every = "30s")
    public void updatePrices() {
        securities.values().forEach(security -> {
            // Fluctuate ±2%
            double fluctuation = 1.0 + (random.nextDouble() * 0.04 - 0.02);
            BigDecimal newPrice = security.getCurrentPrice()
                .multiply(BigDecimal.valueOf(fluctuation))
                .setScale(2, java.math.RoundingMode.HALF_UP);
            security.setCurrentPrice(newPrice);
            security.setLastUpdated(Instant.now());
        });
    }

    public List<Security> getAllSecurities() {
        return new ArrayList<>(securities.values());
    }

    public Optional<Security> getSecurity(String symbol) {
        return Optional.ofNullable(securities.get(symbol));
    }
}
```

**Resource: `PricingResource.java`**:
```java
package com.trading.platform.pricing.resource;

import com.trading.platform.pricing.service.PricingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/pricing")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PricingResource {
    @Inject
    PricingService pricingService;

    @GET
    @Path("/securities")
    public Response getAllSecurities() {
        return Response.ok(pricingService.getAllSecurities()).build();
    }

    @GET
    @Path("/securities/{symbol}")
    public Response getSecurity(@PathParam("symbol") String symbol) {
        return pricingService.getSecurity(symbol)
            .map(s -> Response.ok(s).build())
            .orElse(Response.status(404).build());
    }
}
```

**`application.properties`**:
```properties
quarkus.http.port=8081
quarkus.application.name=securities-pricing-service

# Swagger
quarkus.smallrye-openapi.path=/openapi
quarkus.swagger-ui.always-include=true

# Health
quarkus.health.extensions.enabled=true
```

**Dockerfile.jvm**:
```dockerfile
FROM registry.access.redhat.com/ubi8/openjdk-21:1.18
COPY build/quarkus-app/lib/ /deployments/lib/
COPY build/quarkus-app/*.jar /deployments/
COPY build/quarkus-app/app/ /deployments/app/
COPY build/quarkus-app/quarkus/ /deployments/quarkus/
EXPOSE 8080
CMD ["java", "-jar", "/deployments/quarkus-run.jar"]
```

### Service 2: Currency Exchange Service (Mock)

Similar structure to Securities Pricing Service:

**Entity: `ExchangeRate.java`**:
```java
package com.trading.platform.exchange.entity;

import com.trading.platform.domain.Currency;
import java.math.BigDecimal;
import java.time.Instant;

public class ExchangeRate {
    private Currency fromCurrency;
    private Currency toCurrency;
    private BigDecimal rate;
    private Instant lastUpdated;

    // Constructor, getters, setters
}
```

**Service: `ExchangeRateService.java`**:
```java
package com.trading.platform.exchange.service;

import com.trading.platform.domain.Currency;
import com.trading.platform.exchange.entity.ExchangeRate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import io.quarkus.scheduler.Scheduled;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ExchangeRateService {
    private final Map<String, ExchangeRate> rates = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        addRate(Currency.USD, Currency.EUR, new BigDecimal("0.920000"));
        addRate(Currency.USD, Currency.GBP, new BigDecimal("0.790000"));
        addRate(Currency.EUR, Currency.USD, new BigDecimal("1.090000"));
        addRate(Currency.EUR, Currency.GBP, new BigDecimal("0.860000"));
        addRate(Currency.GBP, Currency.USD, new BigDecimal("1.270000"));
        addRate(Currency.GBP, Currency.EUR, new BigDecimal("1.160000"));
    }

    private void addRate(Currency from, Currency to, BigDecimal rate) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setFromCurrency(from);
        exchangeRate.setToCurrency(to);
        exchangeRate.setRate(rate);
        exchangeRate.setLastUpdated(Instant.now());
        rates.put(from + "_" + to, exchangeRate);
    }

    @Scheduled(every = "60s")
    public void updateRates() {
        rates.values().forEach(rate -> {
            // Fluctuate ±0.5%
            double fluctuation = 1.0 + (random.nextDouble() * 0.01 - 0.005);
            BigDecimal newRate = rate.getRate()
                .multiply(BigDecimal.valueOf(fluctuation))
                .setScale(6, java.math.RoundingMode.HALF_UP);
            rate.setRate(newRate);
            rate.setLastUpdated(Instant.now());
        });
    }

    public Optional<BigDecimal> getRate(Currency from, Currency to) {
        if (from == to) {
            return Optional.of(BigDecimal.ONE);
        }
        return Optional.ofNullable(rates.get(from + "_" + to))
            .map(ExchangeRate::getRate);
    }

    public List<ExchangeRate> getAllRates() {
        return new ArrayList<>(rates.values());
    }
}
```

**Resource: `ExchangeRateResource.java`**:
```java
package com.trading.platform.exchange.resource;

import com.trading.platform.domain.Currency;
import com.trading.platform.exchange.service.ExchangeRateService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/exchange")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExchangeRateResource {
    @Inject
    ExchangeRateService exchangeRateService;

    @GET
    @Path("/rates")
    public Response getAllRates() {
        return Response.ok(exchangeRateService.getAllRates()).build();
    }

    @GET
    @Path("/rates/{from}/{to}")
    public Response getRate(@PathParam("from") Currency from, @PathParam("to") Currency to) {
        return exchangeRateService.getRate(from, to)
            .map(rate -> Response.ok(Map.of("rate", rate)).build())
            .orElse(Response.status(404).build());
    }
}
```

### Service 3: User Signup Service (Mock)

**DTO: `SignupRequest.java`**:
```java
package com.trading.platform.signup.dto;

public class SignupRequest {
    private String email;
    private String username;
    private String phoneNumber;

    // Getters, setters
}
```

**Service: `SignupService.java`**:
```java
package com.trading.platform.signup.service;

import com.trading.platform.events.UserCreatedEvent;
import com.trading.platform.signup.dto.SignupRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import java.util.UUID;

@ApplicationScoped
public class SignupService {
    @Channel("user-events-out")
    Emitter<UserCreatedEvent> userEventsEmitter;

    public UUID signup(SignupRequest request) {
        UUID userId = UUID.randomUUID();

        UserCreatedEvent event = new UserCreatedEvent(
            userId,
            request.getEmail(),
            request.getUsername(),
            request.getPhoneNumber()
        );

        userEventsEmitter.send(event);

        return userId;
    }
}
```

**Resource: `SignupResource.java`**:
```java
package com.trading.platform.signup.resource;

import com.trading.platform.signup.dto.SignupRequest;
import com.trading.platform.signup.service.SignupService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/v1/signup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SignupResource {
    @Inject
    SignupService signupService;

    @POST
    public Response signup(SignupRequest request) {
        UUID userId = signupService.signup(request);
        return Response.ok(Map.of("userId", userId)).build();
    }
}
```

**`application.properties`**:
```properties
quarkus.http.port=8084
quarkus.application.name=user-signup-service

# Kafka
mp.messaging.outgoing.user-events-out.connector=smallrye-kafka
mp.messaging.outgoing.user-events-out.topic=user-events
mp.messaging.outgoing.user-events-out.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
kafka.bootstrap.servers=localhost:9092
```

### Service 4: Fee Service

**Entity: `FeeRule.java`**:
```java
package com.trading.platform.fee.entity;

import com.trading.platform.domain.Currency;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "fee_rules")
public class FeeRule extends PanacheEntityBase {
    @Id
    @GeneratedValue
    private UUID id;

    private String ruleType; // "TRADING" or "EXCHANGE"

    private String symbol; // For trading fees (null for exchange)

    @Enumerated(EnumType.STRING)
    private Currency fromCurrency; // For exchange fees

    @Enumerated(EnumType.STRING)
    private Currency toCurrency; // For exchange fees

    private BigDecimal fixedFee;
    private BigDecimal percentageFee;

    // Getters, setters
}
```

**Service: `FeeCalculationService.java`**:
```java
package com.trading.platform.fee.service;

import com.trading.platform.domain.Currency;
import com.trading.platform.fee.entity.FeeRule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ApplicationScoped
public class FeeCalculationService {

    @PostConstruct
    public void initDefaultFees() {
        // Initialize default fee rules
        // For simplicity, use 1% + $0.50 for all trades
        // and 0.5% + $0.25 for currency exchanges
    }

    public BigDecimal calculateTradingFee(String symbol, BigDecimal amount) {
        // Fetch fee rule from database
        // For now, use default: 1% + $0.50
        BigDecimal percentageFee = amount.multiply(new BigDecimal("0.01"));
        BigDecimal fixedFee = new BigDecimal("0.50");
        return percentageFee.add(fixedFee).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateExchangeFee(Currency from, Currency to, BigDecimal amount) {
        // For now, use default: 0.5% + $0.25
        BigDecimal percentageFee = amount.multiply(new BigDecimal("0.005"));
        BigDecimal fixedFee = new BigDecimal("0.25");
        return percentageFee.add(fixedFee).setScale(2, RoundingMode.HALF_UP);
    }
}
```

**Resource: `FeeResource.java`**:
```java
package com.trading.platform.fee.resource;

import com.trading.platform.domain.Currency;
import com.trading.platform.fee.service.FeeCalculationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Map;

@Path("/api/v1/fees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeeResource {
    @Inject
    FeeCalculationService feeService;

    @GET
    @Path("/trading/{symbol}")
    public Response getTradingFee(@PathParam("symbol") String symbol,
                                   @QueryParam("amount") BigDecimal amount) {
        BigDecimal fee = feeService.calculateTradingFee(symbol, amount);
        return Response.ok(Map.of("fee", fee)).build();
    }

    @GET
    @Path("/exchange")
    public Response getExchangeFee(@QueryParam("from") Currency from,
                                    @QueryParam("to") Currency to,
                                    @QueryParam("amount") BigDecimal amount) {
        BigDecimal fee = feeService.calculateExchangeFee(from, to, amount);
        return Response.ok(Map.of("fee", fee)).build();
    }
}
```

## Phase 3: Implement Core Services

*Due to length constraints, I'll provide the structure for remaining services. The pattern is consistent.*

### Service 5: User Service

- JPA Entity: `UserEntity` with UUID, email, username, phoneNumber
- Kafka Consumer: Listens to `user-events`, saves users
- REST Resource: GET `/api/v1/users/{userId}`
- PostgreSQL database with Flyway migration
- Spock tests for service and repository layers

### Service 6: Wallet Service

- JPA Entity: `WalletBalance` (userId, currency, balance)
- Service: `WalletService` (deposit, withdraw, exchange, getBalances)
- Kafka Producer: Publishes wallet-updated, deposit-completed, withdrawal-completed, currency-exchanged events
- REST Resource: POST deposit/withdraw/exchange, GET balances
- PostgreSQL database

### Service 7: Trading Service (Orchestrator)

- JPA Entity: `Trade` (tradeId, userId, symbol, type, quantity, price, etc.)
- Service: `TradingService` orchestrates:
  1. Get current price (REST call to Pricing Service)
  2. Calculate fees (REST call to Fee Service)
  3. Get exchange rate if needed (REST call to Exchange Service)
  4. Calculate final amounts (use MoneyCalculator)
  5. Validate funds (REST call to Wallet Service)
  6. Execute trade (save to DB)
  7. Publish trade-completed event
- Kafka Producer: trade-completed, trade-failed events
- REST Resource: POST buy/sell trades
- PostgreSQL database

### Service 8: Portfolio Service

- JPA Entity: `Holding` (userId, symbol, quantity, avgPrice, currency)
- Kafka Consumer: Listens to trade-completed, updates holdings
- Service: `PortfolioService` (calculate current value, profit/loss)
- REST Resource: GET portfolio, value, profit-loss
- PostgreSQL database

### Service 9: Transaction History Service

- JPA Entity: `Transaction` (transactionId, userId, type, amount, fees, metadata)
- Kafka Consumer: Listens to ALL events (wallet, trading), records transactions
- REST Resource: GET transaction history with filters
- PostgreSQL database (append-only pattern)

### Service 10: API Gateway

- Quarkus REST Client to call all services
- Aggregates responses
- Routes requests to appropriate services
- OpenAPI documentation aggregation
- CORS configuration for frontend

## Phase 4: Docker Compose Configuration

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: trading
      POSTGRES_PASSWORD: trading
      POSTGRES_DB: trading
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  securities-pricing-service:
    build:
      context: ./services/securities-pricing-service
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8081:8080"
    environment:
      QUARKUS_HTTP_PORT: 8080

  currency-exchange-service:
    build:
      context: ./services/currency-exchange-service
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8082:8080"

  fee-service:
    build:
      context: ./services/fee-service
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8083:8080"
    depends_on:
      - postgres
    environment:
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/trading

  user-signup-service:
    build:
      context: ./services/user-signup-service
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8084:8080"
    depends_on:
      - kafka

  user-service:
    build:
      context: ./services/user-service
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8085:8080"
    depends_on:
      - postgres
      - kafka

  wallet-service:
    build:
      context: ./services/wallet-service
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8086:8080"
    depends_on:
      - postgres
      - kafka

  trading-service:
    build:
      context: ./services/trading-service
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8087:8080"
    depends_on:
      - postgres
      - kafka
      - securities-pricing-service
      - fee-service
      - currency-exchange-service

  portfolio-service:
    build:
      context: ./services/portfolio-service
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8088:8080"
    depends_on:
      - postgres
      - kafka

  transaction-history-service:
    build:
      context: ./services/transaction-history-service
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8089:8080"
    depends_on:
      - postgres
      - kafka

  api-gateway:
    build:
      context: ./services/api-gateway
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8080:8080"
    depends_on:
      - user-service
      - wallet-service
      - trading-service
      - portfolio-service
      - transaction-history-service
      - securities-pricing-service
      - currency-exchange-service
      - fee-service

  frontend:
    build:
      context: ./frontend/trading-platform-ui
      dockerfile: Dockerfile
    ports:
      - "3000:80"
    depends_on:
      - api-gateway

volumes:
  postgres-data:
```

## Phase 5: Frontend Implementation

### Setup React Project

```bash
cd frontend
npm create vite@latest trading-platform-ui -- --template react-ts
cd trading-platform-ui
npm install
npm install @mui/material @emotion/react @emotion/styled axios react-router-dom
```

### Project Structure

```
frontend/trading-platform-ui/
├── src/
│   ├── components/
│   │   ├── SignupForm.tsx
│   │   ├── WalletView.tsx
│   │   ├── MarketView.tsx
│   │   ├── TradingForm.tsx
│   │   ├── PortfolioView.tsx
│   │   └── TransactionsView.tsx
│   ├── services/
│   │   └── api.ts
│   ├── context/
│   │   └── UserContext.tsx
│   ├── App.tsx
│   └── main.tsx
├── Dockerfile
└── nginx.conf
```

### API Service (`src/services/api.ts`):

```typescript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const userApi = {
  signup: (data: { email: string; username: string; phoneNumber: string }) =>
    api.post('/api/v1/signup', data),
  getUser: (userId: string) => api.get(`/api/v1/users/${userId}`),
};

export const walletApi = {
  getBalances: (userId: string) => api.get(`/api/v1/wallets/${userId}/balances`),
  deposit: (userId: string, currency: string, amount: number) =>
    api.post(`/api/v1/wallets/${userId}/deposit`, { currency, amount }),
  withdraw: (userId: string, currency: string, amount: number) =>
    api.post(`/api/v1/wallets/${userId}/withdraw`, { currency, amount }),
  exchange: (userId: string, fromCurrency: string, toCurrency: string, amount: number) =>
    api.post(`/api/v1/wallets/${userId}/exchange`, { fromCurrency, toCurrency, amount }),
};

export const tradingApi = {
  getSecurities: () => api.get('/api/v1/securities'),
  getSecurity: (symbol: string) => api.get(`/api/v1/securities/${symbol}`),
  buy: (data: { userId: string; symbol: string; currency: string; orderType: string; amount?: number; quantity?: number }) =>
    api.post('/api/v1/trades/buy', data),
  sell: (data: { userId: string; symbol: string; currency: string; orderType: string; amount?: number; quantity?: number }) =>
    api.post('/api/v1/trades/sell', data),
};

export const portfolioApi = {
  getPortfolio: (userId: string) => api.get(`/api/v1/portfolios/${userId}`),
  getValue: (userId: string, currency: string) =>
    api.get(`/api/v1/portfolios/${userId}/value?currency=${currency}`),
  getProfitLoss: (userId: string, period: string) =>
    api.get(`/api/v1/portfolios/${userId}/profit-loss?period=${period}`),
};

export const transactionApi = {
  getHistory: (userId: string, params?: { type?: string; from?: string; to?: string }) =>
    api.get(`/api/v1/transactions/${userId}`, { params }),
};
```

### Main App (`src/App.tsx`):

```typescript
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { UserProvider } from './context/UserContext';
import SignupPage from './pages/SignupPage';
import WalletPage from './pages/WalletPage';
import MarketPage from './pages/MarketPage';
import TradingPage from './pages/TradingPage';
import PortfolioPage from './pages/PortfolioPage';
import TransactionsPage from './pages/TransactionsPage';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <UserProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/wallet" element={<WalletPage />} />
            <Route path="/market" element={<MarketPage />} />
            <Route path="/trading" element={<TradingPage />} />
            <Route path="/portfolio" element={<PortfolioPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/" element={<Navigate to="/signup" replace />} />
          </Routes>
        </BrowserRouter>
      </UserProvider>
    </ThemeProvider>
  );
}

export default App;
```

### Dockerfile for Frontend:

```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### nginx.conf:

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://api-gateway:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

## Phase 6: Testing Requirements

### Spock Tests

Create Spock tests for each service:

**Example: `WalletServiceSpec.groovy`**:

```groovy
package com.trading.platform.wallet

import com.trading.platform.domain.Currency
import com.trading.platform.wallet.service.WalletService
import io.quarkus.test.junit.QuarkusTest
import spock.lang.Specification
import jakarta.inject.Inject

@QuarkusTest
class WalletServiceSpec extends Specification {

    @Inject
    WalletService walletService

    def "should create wallet balance for new user"() {
        given:
        def userId = UUID.randomUUID()
        def currency = Currency.USD
        def amount = new BigDecimal("100.00")

        when:
        walletService.deposit(userId, currency, amount)

        then:
        def balance = walletService.getBalance(userId, currency)
        balance == amount
    }

    def "should handle currency exchange correctly"() {
        given:
        def userId = UUID.randomUUID()
        walletService.deposit(userId, Currency.USD, new BigDecimal("100.00"))

        when:
        walletService.exchange(userId, Currency.USD, Currency.EUR, new BigDecimal("50.00"))

        then:
        def usdBalance = walletService.getBalance(userId, Currency.USD)
        def eurBalance = walletService.getBalance(userId, Currency.EUR)
        usdBalance < new BigDecimal("50.00") // fees applied
        eurBalance > BigDecimal.ZERO
    }
}
```

Create similar tests for:
- Trading calculations (fractional rounding)
- Portfolio value calculations
- Fee calculations
- Event publishing/consuming

## Phase 7: Build and Run

### Build All Services

```bash
./gradlew clean build
```

### Run with Docker Compose

```bash
docker-compose up --build
```

### Verify Services

- API Gateway: http://localhost:8080/q/swagger-ui
- Frontend: http://localhost:3000
- Securities Pricing: http://localhost:8081/q/swagger-ui
- Currency Exchange: http://localhost:8082/q/swagger-ui
- Fee Service: http://localhost:8083/q/swagger-ui

### Test End-to-End Flow

1. Signup a new user via frontend
2. Deposit funds in USD
3. View securities in market
4. Buy fractional shares
5. View portfolio
6. View transactions

## Critical Implementation Notes

1. **BigDecimal Usage**: ALWAYS use BigDecimal for money calculations, NEVER double or float
2. **Customer-Favorable Rounding**: Use `MoneyCalculator` utility methods
3. **Event Publishing**: Ensure events are published AFTER database transactions commit
4. **Error Handling**: Use RFC 7807 Problem Details for REST errors
5. **Correlation IDs**: Pass correlation IDs through service calls and events for tracing
6. **Database Migrations**: Use Flyway for all schema changes
7. **Health Checks**: Implement readiness/liveness probes for all services
8. **API Documentation**: Use OpenAPI annotations for all endpoints

## Deliverables for Q/A

Once implementation is complete:
1. Create `01_q_a.md` with:
   - Testing instructions
   - Expected behavior for each feature
   - Sample test data
   - Known limitations
2. Update `01_discussion.md` with any implementation decisions made
3. Update `01_summary.md` with implementation status

## Questions for Senior Engineer or Operator

If you encounter ambiguities during implementation:
- Technical implementation details
- Trade-offs between approaches
- Performance considerations
- Specific AWS service preferences

Ask questions early and document decisions in `01_discussion.md`.

---

**[As Senior Engineer]** This provides a comprehensive implementation guide. Focus on building incrementally: mock services first, then core services, then integration. Test thoroughly at each step. Good luck with the implementation!
