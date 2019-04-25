package com.acabra.moneytransfer.dao.h2;

import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.model.Account;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.util.List;

public class AccountDAOH2Impl implements AccountDAO {

    private final Sql2o sql2o;

    //SQL DDL
    static final String CREATE_TABLE_ACCOUNT =
            "CREATE TABLE account(" +
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
}
