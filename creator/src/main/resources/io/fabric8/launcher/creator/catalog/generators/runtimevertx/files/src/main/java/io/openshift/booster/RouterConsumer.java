package io.openshift.booster;

import java.util.function.Consumer;

import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.Router;
import rx.Completable;

public abstract class RouterConsumer implements Consumer<Router> {

  protected final Vertx vertx;

  protected RouterConsumer(Vertx vertx) {
    this.vertx = vertx;
  }

  public abstract Completable start();

}
