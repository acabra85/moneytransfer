package com.acabra.moneytransfer;

import com.acabra.moneytransfer.dao.h2.H2Sql2oHelper;
import com.acabra.moneytransfer.dto.TransferRequestDTO;
import com.acabra.moneytransfer.utils.JsonHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import java.math.BigDecimal;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import static io.restassured.RestAssured.given;

public class BankingAppTest {

    @BeforeClass
    public static void setup() {
        RestAssured.port = 4567;
        RestAssured.basePath = "/api";
        RestAssured.baseURI = "http://localhost";
        new BankingApp(H2Sql2oHelper.ofLocalKeepOpenSql2o()).start();
    }

    @Test
    public void health_check_application_up_and_running() throws JsonProcessingException {

        given()
            .pathParam("accountId", 1)
            .when().get("/accounts/{accountId}")
            .then().assertThat().statusCode(HttpStatus.NOT_FOUND_404);

        given()
            .when().get("/accounts")
            .then().assertThat().statusCode(HttpStatus.OK_200);

        given()
            .body("{\"initialBalance\": 1000}")
            .when().post("/accounts/new")
            .then().assertThat().statusCode(HttpStatus.CREATED_201);

        given()
            .body("{\"initialBalance\": 2000}")
            .when().post("/accounts/new")
            .then().assertThat().statusCode(HttpStatus.CREATED_201);

        given()
            .pathParam("accountId", 1)
            .when().get("/accounts/{accountId}")
            .then().assertThat().statusCode(HttpStatus.OK_200);

        given()
            .pathParam("accountId", 2)
            .when().get("/accounts/{accountId}")
            .then().assertThat().statusCode(HttpStatus.OK_200);

        given()
            .when().get("/transfers")
            .then().assertThat().statusCode(HttpStatus.OK_200);

        given()
            .body(JsonHelper.getInstance().toJson(new TransferRequestDTO(2, 1, new BigDecimal("2000"))))
            .when().post("/transfers/new")
            .then().assertThat().statusCode(HttpStatus.CREATED_201);

        given()
            .pathParam("accountId", 1)
            .when().get("/transfers/{accountId}")
            .then().assertThat().statusCode(HttpStatus.OK_200);
    }
}
