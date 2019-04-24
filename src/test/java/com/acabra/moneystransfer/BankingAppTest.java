package com.acabra.moneystransfer;

import io.restassured.response.Response;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

public class BankingAppTest extends IntegrationTest {

    @Test
    public void base_test_running() {
        Response response = given().when().get("/test");
        response.then().statusCode(200);
        response.then().body(containsString("Demo bank app running"));
    }
}
