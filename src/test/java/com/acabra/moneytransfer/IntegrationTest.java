package com.acabra.moneytransfer;

import io.restassured.RestAssured;
import org.junit.BeforeClass;

public class IntegrationTest {

    @BeforeClass
    public static void setup() {
        RestAssured.port = 4567;
        RestAssured.basePath = "/api";
        RestAssured.baseURI = "http://localhost";
        BankingApp.main();
    }
}