package com.acabra.moneytransfer.api;

import com.acabra.moneytransfer.control.Controller;
import com.acabra.moneytransfer.response.MessageResponse;
import com.acabra.moneytransfer.utils.JsonHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.NoSuchElementException;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.internalServerError;
import static spark.Spark.path;
import static spark.Spark.post;

public class Router {

    private final Controller controller;
    private final Logger logger = LoggerFactory.getLogger(Router.class);
    private final JsonHelper jsonHelper = JsonHelper.getInstance();

    public Router(Controller controller) {
        this.controller = controller;
    }

    public void registerRoutes() {
        before("/*", (request, response) -> logger.info("{} {}", request.requestMethod(), request.uri()));
        path("/api", () -> {

            path("/accounts", () -> {
                post("/new", controller::createAccount, jsonHelper::toJson);

                path("/:accountId", () -> {
                    get("", controller::getAccountById, jsonHelper::toJson);

                    get("/transfers", controller::retrieveTransfersByAccountId, jsonHelper::toJson);
                });

                get("", controller::getAccounts, jsonHelper::toJson);
                get("/", controller::getAccounts, jsonHelper::toJson);
            });

            path("/transfers", () -> {
                post("/new", controller::transfer, jsonHelper::toJson);

                get("", controller::getTransfers, jsonHelper::toJson);

                get("/:accountId", controller::retrieveTransfersByAccountId, jsonHelper::toJson);
            });

        });

        exception(Exception.class, (ex, req, res) -> {
            processResponseException(ex, res);
            res.type(MimeTypes.Type.APPLICATION_JSON.toString());
            logger.error(ex.getMessage(), ex);
        });

        internalServerError((req, res) -> {
            res.type(MimeTypes.Type.APPLICATION_JSON_UTF_8.toString());
            logger.error("internal server error {} {} ", req.requestMethod(), req.uri());
            return plainJsonMessage("We are sorry.");
        });

        after(((request, response) -> response.type(MimeTypes.Type.APPLICATION_JSON_UTF_8.toString())));
    }

    private void processResponseException(Exception ex, Response res) {
        res.status(HttpStatus.BAD_REQUEST_400);
        String body = plainJsonMessage("failed unable to parse the object");
        try {
            body = jsonHelper.toJson(new MessageResponse<>(controller.getCallId(), HttpStatus.BAD_REQUEST_400, true, ex.getMessage(), null));
            if (ex instanceof NoSuchElementException) {
                res.status(HttpStatus.NOT_FOUND_404);
                body = jsonHelper.toJson(new MessageResponse<>(controller.getCallId(), HttpStatus.NOT_FOUND_404, true, ex.getMessage(), null));
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        res.body(body);
    }

    private String plainJsonMessage(String message) {
        return String.format("{\"message\":\"%s\"}", message);
    }
}
