package com.acabra.moneystransfer;

import com.acabra.moneytransfer.BankingApp;
import io.restassured.RestAssured;
import org.junit.BeforeClass;

public class IntegrationTest {

    @BeforeClass
    public static void setup() {
        RestAssured.port = 4567;
        RestAssured.basePath = "/";
        RestAssured.baseURI = "http://localhost";
        BankingApp.main();
    }
}