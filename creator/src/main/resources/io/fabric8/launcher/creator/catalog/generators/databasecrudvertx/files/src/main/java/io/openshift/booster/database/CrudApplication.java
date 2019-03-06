package io.openshift.booster.database;

import java.util.NoSuchElementException;

import io.openshift.booster.RouterConsumer;
import io.openshift.booster.database.service.Store;
import io.openshift.booster.database.service.impl.JdbcProductStore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import rx.Completable;

import static io.openshift.booster.database.Errors.error;

public class CrudApplication extends RouterConsumer {

  private Store store;

  public CrudApplication(Vertx vertx) {
    super(vertx);
  }

  @Override
  public Completable start() {
    // Create a JDBC client
    JDBCClient jdbc = JDBCClient.createShared(vertx, new JsonObject()
      //{{if .databaseType==postgresql}}
      .put("url", "jdbc:postgresql://" + getEnv("DB_HOST", "localhost") + "/my_data")
      .put("jdbcUrl", "jdbc:postgresql://" + getEnv("DB_HOST", "localhost") + "/my_data")
      //{{else if .databaseType==mysql}}
      //.put("url", "jdbc:mysql://" + getEnv("DB_HOST", "localhost") + "/my_data?useSSL=false")
      //.put("jdbcUrl", "jdbc:mysql://" + getEnv("DB_HOST", "localhost") + "/my_data?useSSL=false")
      //{{end}}
      .put("user", getEnv("DB_USERNAME", "user"))
      .put("password", getEnv("DB_PASSWORD", "password"))
      .put("principal", getEnv("DB_USERNAME", "user"))
      .put("credential", getEnv("DB_PASSWORD", "password"))
    );
    this.store = new JdbcProductStore(jdbc);
    return DBInitHelper.initDatabase(vertx, jdbc);
  }

  @Override
  public void accept(Router router) {
    // perform validation of the :id parameter
    router.route("/api/fruits/:id").handler(this::validateId);
    // implement a basic REST CRUD mapping
    router.get("/api/fruits").handler(this::retrieveAll);
    router.post("/api/fruits").handler(this::addOne);
    router.get("/api/fruits/:id").handler(this::getOne);
    router.put("/api/fruits/:id").handler(this::updateOne);
    router.delete("/api/fruits/:id").handler(this::deleteOne);
  }

  private void validateId(RoutingContext ctx) {
    try {
      ctx.put("fruitId", Long.parseLong(ctx.pathParam("id")));
      // continue with the next handler in the route
      ctx.next();
    } catch (NumberFormatException e) {
      error(ctx, 400, "invalid id: " + e.getCause());
    }
  }

  private void retrieveAll(RoutingContext ctx) {
    HttpServerResponse response = ctx.response()
      .putHeader("Content-Type", "application/json");
    JsonArray res = new JsonArray();
    store.readAll()
      .subscribe(
        res::add,
        err -> error(ctx, 415, err),
        () -> response.end(res.encodePrettily())
      );
  }


  private void getOne(RoutingContext ctx) {
    HttpServerResponse response = ctx.response()
      .putHeader("Content-Type", "application/json");

    store.read(ctx.get("fruitId"))
      .subscribe(
        json -> response.end(json.encodePrettily()),
        err -> {
          if (err instanceof NoSuchElementException) {
            error(ctx, 404, err);
          } else if (err instanceof IllegalArgumentException) {
            error(ctx, 415, err);
          } else {
            error(ctx, 500, err);
          }
        }
      );
  }

  private void addOne(RoutingContext ctx) {
    JsonObject item;
    try {
      item = ctx.getBodyAsJson();
    } catch (RuntimeException e) {
      error(ctx, 415, "invalid payload");
      return;
    }

    if (item == null) {
      error(ctx, 415, "invalid payload");
      return;
    }

    store.create(item)
      .subscribe(
        json ->
          ctx.response()
            .putHeader("Location", "/api/fruits/" + json.getLong("id"))
            .putHeader("Content-Type", "application/json")
            .setStatusCode(201)
            .end(json.encodePrettily()),
        err -> writeError(ctx, err)
      );
  }

  private void updateOne(RoutingContext ctx) {
    JsonObject item;
    try {
      item = ctx.getBodyAsJson();
    } catch (RuntimeException e) {
      error(ctx, 415, "invalid payload");
      return;
    }

    if (item == null) {
      error(ctx, 415, "invalid payload");
      return;
    }

    store.update(ctx.get("fruitId"), item)
      .subscribe(
        () ->
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .setStatusCode(200)
            .end(item.put("id", ctx.<Long>get("fruitId")).encodePrettily()),
        err -> writeError(ctx, err)
      );
  }

  private void writeError(RoutingContext ctx, Throwable err) {
    if (err instanceof NoSuchElementException) {
      error(ctx, 404, err);
    } else if (err instanceof IllegalArgumentException) {
      error(ctx, 422, err);
    } else {
      error(ctx, 409, err);
    }
  }

  private void deleteOne(RoutingContext ctx) {
    store.delete(ctx.get("fruitId"))
      .subscribe(
        () ->
          ctx.response()
            .setStatusCode(204)
            .end(),
        err -> {
          if (err instanceof NoSuchElementException) {
            error(ctx, 404, err);
          } else {
            error(ctx, 415, err);
          }
        }
      );
  }

  private String getEnv(String key, String dv) {
    String s = System.getenv(key);
    if (s == null) {
      return dv;
    }
    return s;
  }
}
