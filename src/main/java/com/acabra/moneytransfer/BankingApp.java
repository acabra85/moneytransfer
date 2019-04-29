package com.acabra.moneytransfer;

import com.acabra.moneytransfer.api.Router;
import com.acabra.moneytransfer.control.Controller;
import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.TransferDAO;
import com.acabra.moneytransfer.dao.h2.AccountDAOH2Impl;
import com.acabra.moneytransfer.dao.h2.H2Sql2oHelper;
import com.acabra.moneytransfer.dao.h2.TransferDAOH2Impl;
import com.acabra.moneytransfer.utils.JsonHelper;
import org.sql2o.Sql2o;

public class BankingApp {

    private final Router router;

    public static void main(String... args) {
        BankingApp bankingApp = new BankingApp();
        bankingApp.start();
    }

    public BankingApp () {
        Sql2o sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o(); //BD Connection
        AccountDAO accountDAO = new AccountDAOH2Impl(sql2o);
        TransferDAO TransferDAO = new TransferDAOH2Impl(sql2o);
        JsonHelper jsonHelper = JsonHelper.getInstance();
        Controller controller = new Controller(accountDAO, TransferDAO, jsonHelper);
        this.router = new Router(controller, jsonHelper);
    }

    private void start() {
        this.router.registerRoutes();
    }
}
