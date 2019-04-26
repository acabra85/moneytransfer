package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.AccountsTransferLock;
import com.acabra.moneytransfer.model.Account;
import com.acabra.moneytransfer.model.TransferRequest;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.util.List;

public class AccountDAOH2Impl implements AccountDAO {

    private final Sql2o sql2o;

    //SQL DDL
    static final String CREATE_TABLE_ACCOUNT =
            "CREATE TABLE account (" +
                "account_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "account_balance DECIMAL(20, 4)" +
            ")";

    static final String CLEAN_DB = "DROP ALL OBJECTS DELETE FILES";

    //SQL DML
    private static final String RETRIEVE_ACCOUNT_BY_ID =
            "SELECT account_id as id, account_balance as balance " +
            "FROM account " +
            "WHERE account_id = :account_id";

    private static final String CREATE_NEW_ACCOUNT =
            "INSERT INTO account(account_balance) " +
                    "VALUES(:account_balance)";

    private static final String RETRIEVE_ALL_ACCOUNTS =
            "SELECT account_id as id, account_balance as balance " +
            "FROM account " +
            "ORDER BY account_id";

    private static final String RETRIEVE_ACCOUNTS_BY_IDS =
            "SELECT account_id as id, account_balance as balance " +
            "FROM account " +
            "WHERE account_id IN (:ids)";

    private static final String RETRIEVE_ACCOUNTS_FOR_TRANSFER =
            "SELECT account_id as id, account_balance as balance " +
            "FROM account " +
            "WHERE account_id IN (:sourceAccountId, :destinationAccountId) FOR UPDATE";

    private static final String UPDATE_ACCOUNT_BALANCE =
            "UPDATE account " +
            "SET account_balance = :balance " +
            "WHERE account_id = :id";

    public AccountDAOH2Impl(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Account retrieveAccountById(long id) {
        try(Connection connection = sql2o.open()) {
            return connection.createQuery(RETRIEVE_ACCOUNT_BY_ID)
                    .addParameter("account_id", id)
                    .executeAndFetchFirst(Account.class);
        }
    }

    @Override
    public List<Account> getAccounts() {
        try(Connection connection = sql2o.open()) {
            return connection.createQuery(RETRIEVE_ALL_ACCOUNTS)
                    .executeAndFetch(Account.class);
        }
    }

    @Override
    public List<Account> retrieveAccountsByIds(List<Long> ids) {
        try(Connection connection = sql2o.open()) {
            return connection.createQuery(RETRIEVE_ACCOUNTS_BY_IDS)
                    .addParameter("ids", ids)
                    .executeAndFetch(Account.class);
        }
    }

    @Override
    public Account createAccount(BigDecimal initialBalance) {
        try (Connection connection = sql2o.open()){
            Long accountId = connection.createQuery(CREATE_NEW_ACCOUNT)
                    .addParameter("account_balance", BigDecimal.ZERO.compareTo(initialBalance) >= 0 ? BigDecimal.ZERO : initialBalance )
                    .executeUpdate().getKey(Long.class);
            return new Account(accountId, initialBalance);
        }
    }

    @Override
    public AccountsTransferLock lockAccountsForTransfer(TransferRequest transferRequest) {
        long sourceAccountId = transferRequest.sourceAccountId, destinationAccountId = transferRequest.destinationAccountId;
        BigDecimal amount =transferRequest.transferAmount;
        Connection tx = sql2o.beginTransaction();
        try {
            List<Account> accounts = tx.createQuery(RETRIEVE_ACCOUNTS_FOR_TRANSFER)
                    .addParameter("sourceAccountId", sourceAccountId)
                    .addParameter("destinationAccountId", destinationAccountId)
                    .executeAndFetch(Account.class);
            if (accounts.size() == 2) {
                Account source = accounts.get(0).getId() == sourceAccountId ? accounts.get(0) : accounts.get(1);
                Account destination = accounts.get(1).getId() == destinationAccountId ? accounts.get(1) : accounts.get(0);
                source.withdraw(amount);
                updateBalanceTransactional(tx, source);
                destination.deposit(amount);
                updateBalanceTransactional(tx, destination);
                return new AccountsTransferLock(tx, source, destination);
            }
            throw new IllegalStateException(String.format("Unable to find the given accounts [%d, %d] in the db: ", sourceAccountId, destinationAccountId));
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    private void updateBalanceTransactional(Connection tx, Account source) {
        tx.createQuery(UPDATE_ACCOUNT_BALANCE)
                .addParameter("balance", source.getBalance())
                .addParameter("id", source.getId())
                .executeUpdate();
    }

    @Override
    public void updateAccountBalance(Account account) {
        try(Connection cx = sql2o.open()) {
            updateBalanceTransactional(cx, account);
        }
    }
}
