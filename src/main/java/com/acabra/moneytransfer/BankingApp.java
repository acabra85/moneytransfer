package com.acabra.moneytransfer;

import static spark.Spark.get;

public class BankingApp {

    public static void main(String... args) {
        get("/test", (req, res) -> "Demo bank app running");
    }
}
