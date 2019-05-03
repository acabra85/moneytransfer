package com.acabra.moneytransfer.model;

import com.acabra.moneytransfer.exception.InsufficientFundsException;
import com.acabra.moneytransfer.exception.InvalidTransferAmountException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import static java.math.BigDecimal.ZERO;

public class Account {

    private final long id;
    private BigDecimal balance;
    private final Currency currency;

    public transient final ReentrantLock lock = new ReentrantLock();

    public Account(long id, BigDecimal initialBalance, Currency currency) {
        this.id = id;
        this.balance = initialBalance;
        this.currency = currency;
    }

    public Account(long id, Currency currency) {
        this.id = id;
        this.balance = BigDecimal.ZERO;
        this.currency = currency;
    }

    public void withdraw(BigDecimal amount) {
        lock.lock();
        try {
            if (hasInsufficientFunds(amount)) {
                throw new InsufficientFundsException(String.format("Unable to withdraw %s from the account [%d] remaining balance %s", amount.toString(), this.id, balance.toString()));
            }
            balance = balance.subtract(amount);
        } finally {
            lock.unlock();
        }

    }

    public void deposit(BigDecimal amount) {
        lock.lock();
        try {
            if (invalidDepositAmount(amount)) {
                throw new InvalidTransferAmountException(String.format("The given deposit %s must be greater than zero", amount.toString()));
            }
            balance = balance.add(amount);
        } finally {
            lock.unlock();
        }
    }

    private boolean hasInsufficientFunds(BigDecimal amount) {
        return amount.compareTo(balance) > 0;
    }

    private boolean invalidDepositAmount(BigDecimal amount) {
        return ZERO.compareTo(amount) >= 0;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public long getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
