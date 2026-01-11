package com.trading.platform.exchange.service;

import com.trading.platform.domain.Currency;
import com.trading.platform.domain.MoneyCalculator;
import com.trading.platform.exchange.entity.ExchangeRate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ExchangeRateService {
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRateService.class);

    private final Map<String, ExchangeRate> rates = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        LOG.info("Initializing currency exchange service with mock rates");

        addRate(Currency.USD, Currency.EUR, new BigDecimal("0.920000"));
        addRate(Currency.USD, Currency.GBP, new BigDecimal("0.790000"));
        addRate(Currency.EUR, Currency.USD, new BigDecimal("1.090000"));
        addRate(Currency.EUR, Currency.GBP, new BigDecimal("0.860000"));
        addRate(Currency.GBP, Currency.USD, new BigDecimal("1.270000"));
        addRate(Currency.GBP, Currency.EUR, new BigDecimal("1.160000"));

        LOG.info("Initialized {} exchange rates", rates.size());
    }

    private void addRate(Currency from, Currency to, BigDecimal rate) {
        ExchangeRate exchangeRate = new ExchangeRate(from, to, rate);
        rates.put(from + "_" + to, exchangeRate);
        LOG.debug("Added exchange rate: {} to {} = {}", from, to, rate);
    }

    @Scheduled(every = "60s")
    public void updateRates() {
        LOG.debug("Updating exchange rates");
        rates.values().forEach(rate -> {
            // Fluctuate ±0.5%
            double fluctuation = 1.0 + (random.nextDouble() * 0.01 - 0.005);
            BigDecimal newRate = rate.getRate()
                .multiply(BigDecimal.valueOf(fluctuation))
                .setScale(6, RoundingMode.HALF_UP);
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

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        return getRate(from, to)
            .map(rate -> MoneyCalculator.convert(amount, rate))
            .orElse(null);
    }
}
