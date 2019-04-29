package com.acabra.moneytransfer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.acabra.moneytransfer.api.Router;
import com.acabra.moneytransfer.control.Controller;
import com.acabra.moneytransfer.dao.h2.AccountDAOH2Impl;
import com.acabra.moneytransfer.dao.h2.H2Sql2oHelper;
import com.acabra.moneytransfer.dao.h2.TransferDAOH2Impl;
import org.sql2o.Sql2o;

public class BankingApp {

    private final Router router;

    public static void main(String... args) {
        BankingApp bankingApp = new BankingApp();
        bankingApp.start();
    }

    public BankingApp () {
        Sql2o sql2o = H2Sql2oHelper.ofLocalKeepOpenSql2o();
        AccountDAOH2Impl accountDAOH2 = new AccountDAOH2Impl(sql2o);
        TransferDAOH2Impl transferDAOH2 = new TransferDAOH2Impl(sql2o);
        Gson gson = new GsonBuilder().create();
        Controller controller = new Controller(accountDAOH2, transferDAOH2, gson);
        this.router = new Router(controller, gson);
    }

    private void start() {
        this.router.registerRoutes();
    }
}
