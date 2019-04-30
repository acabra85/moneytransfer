package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.AccountsTransferLock;
import com.acabra.moneytransfer.model.Account;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
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

    /*
        sql2o default executeAndFetch(<Object>.class) does not use class constructors hence fails to instantiate the
        transient Account lock. By manually creating the Account objects the class constructor instantiates all fields
        as expected.
     */
    ResultSetHandler<Account> ACCOUNT_RESULT_SET_HANDLER = (ResultSet resultSet) -> new Account(resultSet.getLong("id"), resultSet.getBigDecimal("balance"));

    public AccountDAOH2Impl(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Account createAccount(BigDecimal initialBalance) {
        try (Connection connection = sql2o.open()){
            Long accountId = connection.createQuery(CREATE_NEW_ACCOUNT)
                    .addParameter("account_balance", initialBalance)
                    .executeUpdate().getKey(Long.class);
            return new Account(accountId, initialBalance);
        }
    }

    @Override
    public Account retrieveAccountById(long id) {
        try(Connection connection = sql2o.open()) {
            return connection.createQuery(RETRIEVE_ACCOUNT_BY_ID)
                    .addParameter("account_id", id)
                    .executeAndFetchFirst(ACCOUNT_RESULT_SET_HANDLER);
        }
    }

    @Override
    public List<Account> retrieveAllAccounts() {
        try(Connection connection = sql2o.open()) {
            return connection.createQuery(RETRIEVE_ALL_ACCOUNTS)
                    .executeAndFetch(ACCOUNT_RESULT_SET_HANDLER);
        }
    }

    @Override
    public List<Account> retrieveAccountsByIds(List<Long> ids) {
        try(Connection connection = sql2o.open()) {
            return connection.createQuery(RETRIEVE_ACCOUNTS_BY_IDS)
                    .addParameter("ids", ids)
                    .executeAndFetch(ACCOUNT_RESULT_SET_HANDLER);
        }
    }

    @Override
    public AccountsTransferLock lockAccountsForTransfer(long sourceAccountId, long destinationAccountId) {
        Connection tx = sql2o.beginTransaction();
        try {
            List<Account> accounts = tx.createQuery(RETRIEVE_ACCOUNTS_FOR_TRANSFER)
                    .addParameter("sourceAccountId", sourceAccountId)
                    .addParameter("destinationAccountId", destinationAccountId)
                    .executeAndFetch(ACCOUNT_RESULT_SET_HANDLER);
            if (accounts.size() == 2) {
                Account source = accounts.get(0).getId() == sourceAccountId ? accounts.get(0) : accounts.get(1);
                Account destination = accounts.get(1).getId() == destinationAccountId ? accounts.get(1) : accounts.get(0);
                return new AccountsTransferLock(tx, source, destination);
            }
            throw new NoSuchElementException(String.format("Unable to find %d of the given account(s) [%d, %d] in the db: ", 2 - accounts.size(), sourceAccountId, destinationAccountId));
        } catch (Exception e) {
            tx.rollback();
            tx.close();
            throw e;
        }
    }

    @Override
    public void updateAccountBalanceTransactional(Account account, Connection tx) {
        tx.createQuery(UPDATE_ACCOUNT_BALANCE)
                .addParameter("balance", account.getBalance())
                .addParameter("id", account.getId())
                .executeUpdate();
    }

    @Override
    public void updateAccountBalance(Account account) {
        try (Connection cx = sql2o.open()) {
            cx.createQuery(UPDATE_ACCOUNT_BALANCE)
                    .addParameter("balance", account.getBalance())
                    .addParameter("id", account.getId())
                    .executeUpdate();
        }
    }
}
