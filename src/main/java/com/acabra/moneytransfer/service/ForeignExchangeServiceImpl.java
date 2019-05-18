package com.acabra.moneytransfer.service;

import com.acabra.moneytransfer.model.Currency;
import com.acabra.moneytransfer.request.TransferRequest;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class ForeignExchangeServiceImpl implements ForeignExchangeService {

    private final Map<Currency, Map<Currency, Deque<BigDecimal>>> EXCHANGE_RATE_MAP = new ConcurrentHashMap<>();
    private final int EXCHANGE_MAX_HISTORY_SIZE = 10;
    private static final String CURRENCY_DEFAULTS = "exchange_rates.txt";

    private static ForeignExchangeServiceImpl instance = new ForeignExchangeServiceImpl();

    static {
        InputStream resourceAsStream = ForeignExchangeServiceImpl.class.getClassLoader().getResourceAsStream(CURRENCY_DEFAULTS);
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = bf.readLine()) != null) {
                String[] exchangeLine = line.split(",");
                Currency source = Currency.getCurrencyFromCode(exchangeLine[0]);
                Currency destination = Currency.getCurrencyFromCode(exchangeLine[1]);
                BigDecimal exchangeRate = new BigDecimal(exchangeLine[2]);
                instance.updateExchangeRate(source, destination, exchangeRate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ForeignExchangeServiceImpl() {
        //initialize map
    }

    public static ForeignExchangeServiceImpl getInstance() {
        return instance;
    }

    @Override
    public BigDecimal retrieveExchangeRate(Currency sourceCurrency, Currency destinationCurrency) {
        if (sourceCurrency != destinationCurrency) {
            Map<Currency, Deque<BigDecimal>> sourceMap = EXCHANGE_RATE_MAP.get(sourceCurrency);
            if (null != sourceMap) {
                Deque<BigDecimal> rate = sourceMap.get(destinationCurrency);
                if (null != rate && rate.size() > 0) {
                    return rate.getLast();
                }
            }
            throw new NoSuchElementException(String.format("Unable to find exchange rate between currencies [%s -> %s]",
                    sourceCurrency.code, destinationCurrency.code));
        }
        return BigDecimal.ONE;
    }

    @Override
    public void updateExchangeRate(Currency source, Currency destination, BigDecimal exchangeRate) {
        if (source != destination) {
            updateExchangeRateInternal(source, destination, exchangeRate);
            updateExchangeRateInternal(destination, source, BigDecimal.ONE.divide(exchangeRate, 4, BigDecimal.ROUND_HALF_DOWN));
        }
    }

    @Override
    public BigDecimal convertAmount(Currency source, Currency destination, BigDecimal amount) {
        return this.retrieveExchangeRate(source, destination).multiply(amount);
    }

    private void updateExchangeRateInternal(Currency source, Currency destination, BigDecimal exchangeRate) {
        Map<Currency, Deque<BigDecimal>> currencyBigDecimalMap = EXCHANGE_RATE_MAP.get(source);
        if (null != currencyBigDecimalMap) {
            Deque<BigDecimal> exchangeRateQueue = currencyBigDecimalMap.get(destination);
            if (null != exchangeRateQueue) {
                exchangeRateQueue.addLast(exchangeRate);
                if (exchangeRateQueue.size() > EXCHANGE_MAX_HISTORY_SIZE) {
                    exchangeRateQueue.removeFirst();
                }
            } else {
                currencyBigDecimalMap.put(destination, new ArrayDeque<BigDecimal>(){{
                    addLast(exchangeRate);
                }});
            }
        } else {
            HashMap<Currency, Deque<BigDecimal>> value = new HashMap<Currency, Deque<BigDecimal>>(){{
                put(destination, new ArrayDeque<BigDecimal>(){{
                    addLast(exchangeRate);
                }});
            }};
            EXCHANGE_RATE_MAP.put(source, value);
        }
    }
}
