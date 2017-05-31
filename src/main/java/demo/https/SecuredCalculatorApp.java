package demo.https;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple verticle to demonstrate a very simple HTTPS calculator
 *
 * @author kameshs
 */
@Slf4j
public class SecuredCalculatorApp extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) throws Exception {

        Router router = Router.router(vertx);

        router.route("/api/calculator*").handler(BodyHandler.create());

        HttpServerOptions httpsOptions = new HttpServerOptions()
            .setSsl(true);

        //set the self-signed certificates
        httpsOptions.setPemKeyCertOptions(new PemKeyCertOptions()
            .setKeyPath("key.pem")
            .setCertPath("cert.pem"));

        //Some routes
        router.route("/").handler(rc -> {

            HttpServerResponse response = rc.response();
            response
                .putHeader("content-type", "text/html")
                .end("<html><body>" +
                    "<h1>Hello from vert.x!</h1>" +
                    "<p>version = " + rc.request().version() + "</p>" +
                    "</body></html>");

        });

        //routes like /api/add/1/2 will result in n1+n2
        router.route("/api/calculator/add/:n1/:n2").handler(this::add);
        //routes like /api/sub/1/2 - will result in n2 - n1
        router.route("/api/calculator/sub/:n1/:n2").handler(this::sub);

        vertx.createHttpServer(httpsOptions)
            .requestHandler(router::accept)
            .listen(8443, result -> {
                if (result.succeeded()) {
                    log.info("Calculator Server Started Successfully");
                    fut.complete();
                } else {
                    log.error("Failed to start calculator server", result.cause());
                    fut.fail(result.cause());
                }
            });
    }

    /**
     * simple method to do two number addition
     *
     * @param routingContext
     */

    private void add(RoutingContext routingContext) {
        log.info("Calculator::ADD");
        HttpServerRequest request = routingContext.request();
        int n1 = Integer.parseInt(request.getParam("n1"));
        int n2 = Integer.parseInt(request.getParam("n2"));

        JsonObject resultJson = new JsonObject()
            .put("n1", n1)
            .put("n2", n2)
            .put("operation", "Addition")
            .put("result", (n1 + n2));

        routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(200)
            .end(resultJson.encodePrettily());

    }

    /**
     * simple method to do two number subtraction
     *
     * @param routingContext
     */
    private void sub(RoutingContext routingContext) {

        log.info("Calculator::SUB");

        HttpServerRequest request = routingContext.request();
        int n1 = Integer.parseInt(request.getParam("n1"));
        int n2 = Integer.parseInt(request.getParam("n2"));

        JsonObject resultJson = new JsonObject()
            .put("n1", n1)
            .put("n2", n2)
            .put("operation", "Subtraction")
            .put("result", (n2 - n1));

        routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(200)
            .end(resultJson.encodePrettily());

    }
}
