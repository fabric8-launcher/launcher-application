package io.openshift.booster.http;

import java.util.Objects;

import io.openshift.booster.RouterConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import rx.Completable;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

public class HttpApplication extends RouterConsumer {

  public HttpApplication(Vertx vertx) {
    super(vertx);
  }

  protected static final String TEMPLATE = "Hello, %s!";

  @Override
  public void accept(Router router) {
    router.get("/api/greeting").handler(this::greeting);
  }

  @Override
  public Completable start() {
    return Completable.complete();
  }

  private void greeting(RoutingContext rc) {
    String name = Objects.toString(rc.request().getParam("name"), "World");

    JsonObject response = new JsonObject()
      .put("content", String.format(TEMPLATE, name));

    rc.response()
      .putHeader(CONTENT_TYPE.toString(), "application/json; charset=utf-8")
      .end(response.encodePrettily());
  }
}
