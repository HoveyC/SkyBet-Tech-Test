package skybet.test.rs;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import skybet.test.pojo.DecimalBet;
import skybet.test.pojo.DecimalPlacedBet;
import skybet.test.pojo.FractionalBet;
import skybet.test.pojo.FractionalOdds;
import skybet.test.pojo.JsonError;

/**
 * This is our JUnit test for our verticle. The test uses vertx-unit, so we
 * declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class BetsRestServiceTest {

    private Vertx vertx;
    private Integer port;

    /**
     * Before executing our test, let's deploy our verticle.
     * <p/>
     * This method instantiates a new Vertx and deploy the verticle. Then, it
     * waits in the verticle has successfully completed its start sequence
     * (thanks to `context.asyncAssertSuccess`).
     *
     * @param context the test context.
     */
    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        // Let's configure the verticle to listen on the 'test' port (randomly picked).
        // We create deployment options and set the _configuration_ json object:
//        ServerSocket socket = new ServerSocket(0);
//        port = socket.getLocalPort();
//        socket.close();
        // DeploymentOptions options = new DeploymentOptions();
        // We pass the options as the second parameter of the deployVerticle method.
        vertx.deployVerticle(BetsRestService.class.getName(), context.asyncAssertSuccess());
    }

    /**
     * This method, called after our test, just cleanup everything by closing
     * the vert.x instance
     *
     * @param context the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void checkAvailable(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(8080, "localhost", "/available", response -> {
            context.assertEquals(response.statusCode(), 200);
            context.assertEquals(response.headers().get("content-type"), "application/json; charset=utf-8");
            async.complete();
        });

    }

    @Test
    public void checkBetsBadRequest(TestContext context) {
        Async async = context.async();
        FractionalBet fb = FractionalBet.builder()
                .betId(1l)
                .odds(FractionalOdds.builder()
                        .numerator(1)
                        .denominator(10)
                        .build())
                .stake(10)
                .build();
        vertx.createHttpClient().post(8080, "localhost", "/bets", r -> {
            context.assertEquals(r.statusCode(), 400);
            context.assertEquals(r.headers().get("content-type"), "application/json; charset=utf-8");
            context.assertEquals(r.statusMessage(), "Bad Request");
            r.bodyHandler(b -> {
                context.assertEquals(b.toString("UTF-8"),
                        JsonError.builder()
                        .errorCode(400)
                        .errorMessage("Bad Request")
                        .build()
                        .toJsonString());
            });
            async.complete();
        }).setChunked(true).putHeader("content-type", "application/json; charset=utf-8").write(fb.toJsonString()).end();

    }

    @Test
    public void checkBetsBadRequestNullOdds(TestContext context) {
        Async async = context.async();
        DecimalBet db = DecimalBet.builder()
                .betId(1l)
                .odds(null)
                .stake(10)
                .build();
        vertx.createHttpClient().post(8080, "localhost", "/bets", r -> {
            context.assertEquals(r.statusCode(), 400);
            context.assertEquals(r.headers().get("content-type"), "application/json; charset=utf-8");
            context.assertEquals(r.statusMessage(), "Bad Request");
            r.bodyHandler(b -> {
                context.assertEquals(b.toString("UTF-8"),
                        JsonError.builder()
                        .errorCode(400)
                        .errorMessage("Bad Request")
                        .build()
                        .toJsonString());
            });
            async.complete();
        }).setChunked(true).putHeader("content-type", "application/json; charset=utf-8").write(db.toJsonString()).end();

    }

    @Test
    public void checkBetsIamATeaPot(TestContext context) {
        Async async = context.async();
        DecimalBet db = DecimalBet.builder()
                .betId(1l)
                .odds(9.0)
                .stake(10)
                .build();
        vertx.createHttpClient().post(8080, "localhost", "/bets", r -> {
            context.assertEquals(r.statusCode(), 418);
            context.assertEquals(r.headers().get("content-type"), "application/json; charset=utf-8");
            context.assertEquals(r.statusMessage(), "I'm a teapot");
            r.bodyHandler(b -> {
                context.assertEquals(b.toString("UTF-8"),
                        new JsonObject().put("error", "Incorrect Odds").encode());
            });
            async.complete();
        }).setChunked(true).putHeader("content-type", "application/json; charset=utf-8").write(db.toJsonString()).end();

    }

    @Test
    public void checkBetsSuccess(TestContext context) {
        Async async = context.async();
        DecimalBet db = DecimalBet.builder()
                .betId(1l)
                .odds(11.0)
                .stake(10)
                .build();
        vertx.createHttpClient().post(8080, "localhost", "/bets", r -> {
            context.assertEquals(r.statusCode(), 201);
            context.assertEquals(r.headers().get("content-type"), "application/json; charset=utf-8");
            context.assertEquals(r.statusMessage(), "Created");
            r.bodyHandler(b -> {
                context.assertNotNull(Json.decodeValue(b.toString("UTF-8"), DecimalPlacedBet.class).getTransactionId());
            });
            async.complete();
        }).setChunked(true).putHeader("content-type", "application/json; charset=utf-8").write(db.toJsonString()).end();

    }

}
