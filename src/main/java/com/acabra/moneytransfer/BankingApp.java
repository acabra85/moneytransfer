package com.acabra.moneytransfer;

import com.acabra.moneytransfer.api.Router;
import com.acabra.moneytransfer.control.Controller;
import com.acabra.moneytransfer.dao.AccountDAO;
import com.acabra.moneytransfer.dao.TransferDAO;
import com.acabra.moneytransfer.dao.h2.AccountDAOH2Impl;
import com.acabra.moneytransfer.dao.h2.H2Sql2oHelper;
import com.acabra.moneytransfer.dao.h2.TransferDAOH2Impl;
import com.acabra.moneytransfer.service.AccountService;
import com.acabra.moneytransfer.service.AccountServiceImpl;
import com.acabra.moneytransfer.service.TransferService;
import com.acabra.moneytransfer.service.TransferServiceImpl;
import org.sql2o.Sql2o;

public class BankingApp {

    private final Router router;

    public static void main(String... args) {
        BankingApp bankingApp = new BankingApp(H2Sql2oHelper.ofLocalKeepOpenSql2o());
        bankingApp.start();
    }

    public BankingApp(Sql2o sql2o) {
        AccountDAO accountDAO = new AccountDAOH2Impl(sql2o);
        AccountService accountService = new AccountServiceImpl(accountDAO);
        TransferDAO transferDAO = new TransferDAOH2Impl(sql2o);
        TransferService transferService = new TransferServiceImpl(transferDAO, accountDAO);
        Controller controller = new Controller(accountService, transferService);
        this.router = new Router(controller);
    }

    void start() {
        this.router.registerRoutes();
    }

}
