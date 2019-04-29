package com.acabra.moneytransfer.api;

import com.google.gson.Gson;
import com.acabra.moneytransfer.control.Controller;
import com.acabra.moneytransfer.response.MessageResponse;
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
    private final Gson gson;

    public Router(Controller controller, Gson gson) {
        this.controller = controller;
        this.gson = gson;
    }

    public void registerRoutes() {
        before("/*", (request, response) -> logger.info("{} {}", request.requestMethod(), request.uri()));
        path("/api", () -> {

            path("/accounts", () -> {
                post("/new", controller::createAccount, gson::toJson);

                path("/:accountId", () -> {
                    get("", controller::getAccountById, gson::toJson);

                    get("/transfers", controller::retrieveTransfersByAccountId, gson::toJson);
                });

                get("", controller::getAccounts, gson::toJson);
                get("/", controller::getAccounts, gson::toJson);
            });

            path("/transfers", () -> {
                post("/new", controller::transfer, gson::toJson);

                get("", controller::getTransfers, gson::toJson);

                get("/:accountId", controller::retrieveTransfersByAccountId, gson::toJson);
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
            return "{\"message\":\"We are sorry.\"}";
        });

        after(((request, response) -> response.type(MimeTypes.Type.APPLICATION_JSON_UTF_8.toString())));
    }

    private void processResponseException(Exception ex, Response res) {
        res.status(HttpStatus.BAD_REQUEST_400);
        String body = gson.toJson(new MessageResponse<>(controller.getCallId(), true, ex.getMessage(), null));
        if (ex instanceof NoSuchElementException) {
            res.status(HttpStatus.NOT_FOUND_404);
            body = gson.toJson(new MessageResponse<>(controller.getCallId(), true, ex.getMessage(), null));
        }
        res.body(body);
    }
}
