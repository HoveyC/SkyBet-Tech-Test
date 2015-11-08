package skybet.test.rs;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.List;
import java.util.stream.Collectors;
import skybet.test.pojo.DecimalBet;
import skybet.test.pojo.DecimalEvent;
import skybet.test.pojo.DecimalPlacedBet;
import skybet.test.pojo.FractionalBet;
import skybet.test.pojo.FractionalEvent;
import skybet.test.pojo.FractionalPlacedBet;
import skybet.test.pojo.JsonError;
import skybet.test.utils.OddsConversionUtils;

/**
 * This is the Main vertical which creates micro service that exposes two
 * request services /available and /bets.
 *
 * /available service proxies a request onto sky bet api, that return a json
 * array of all the available events which bet can be placed against with the
 * odds in fractional format. The Events are then converted from fractional odds
 * to decimal and the results then passed back as a response.
 *
 * /bets service accepts a bet placed in decimal format and converts the bet
 * into factional format so that it can passed to sky's /bet service. Skys /bet
 * service then returns a bet receipt in a fractional format which is then
 * converted into a decimal format and the results are then passed back as a
 * response
 *
 *
 * @author chrishovey
 */
public class BetsRestService extends AbstractVerticle {

    /**
     *
     * This method is called by vertx on startup and assigns request handlers to
     * /bets and /available. Any other requests will be sent back a 501 not
     * implemented error.
     */
    @Override
    public void start() {

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/bets").handler(routingContext -> {
            this.postBets(routingContext);
        });

        router.get("/available").handler(routingContext -> {
            HttpClient c = vertx.createHttpClient();
            c.getNow(80, "skybettechtestapi.herokuapp.com", "/available", r -> {
                this.getAvailable(r, routingContext);
            });
        });
        //Catch all other requests not served
        router.route().handler(routingContext -> {
            routingContext.response()
                    .setStatusCode(501)
                    .setStatusMessage("Not Implemented")
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(JsonError.builder()
                            .errorCode(501)
                            .errorMessage("Not Implemented")
                            .build()
                            .toJsonString());

        });
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);

    }

    /**
     *
     * This method deserialises the request body into a decimal bet and builds a
     * fractional bet to be sent to the submitBet Method. if the request body
     * cant be deserialised then a bad request response will be returned
     *
     * @param routingContext Represents the context for the handling of a
     * request
     */
    private void postBets(RoutingContext routingContext) {
        try {
            DecimalBet db = Json.decodeValue(routingContext.getBodyAsString(), DecimalBet.class);
            //check the odds been null;
            submitBet(routingContext, FractionalBet.builder()
                    .betId(db.getBetId())
                    .odds(OddsConversionUtils.convertDecimalBetToFraction(db.getOdds()))
                    .stake(db.getStake())
                    .build());

        } catch (DecodeException | IllegalArgumentException de) {
            routingContext.response()
                    .setStatusCode(400)
                    .setStatusMessage("Bad Request")
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(JsonError.builder()
                            .errorCode(400)
                            .errorMessage("Bad Request")
                            .build()
                            .toJsonString());
        }

    }

    /**
     *
     * This method proxies a bet request to sky /bet service submitting a bet
     * placed in a fractional format if the service does not return the expected
     * 201 created status then the error status is returned. If the response
     * from sky cannot be parsed then a 400 bed request is sent back as a
     * response
     *
     * @param routingContext routingContext Represents the context for the
     * handling of a request
     * @param fb fractional bet object representing a bet placed in a fractional
     * format
     */
    private void submitBet(RoutingContext routingContext, FractionalBet fb) {
        HttpClient c = vertx.createHttpClient();
        c.post(80, "skybettechtestapi.herokuapp.com", "/bets", r -> {
            r.bodyHandler(buffer -> {
                if (r.statusCode() != 201) {
                    routingContext.response()
                            .setStatusCode(r.statusCode())
                            .setStatusMessage(r.statusMessage())
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(buffer);
                } else {
                    try {
                        FractionalPlacedBet pb = Json.decodeValue(buffer.toString("UTF-8"), FractionalPlacedBet.class);
                        routingContext.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(DecimalPlacedBet.builder()
                                        .betId(pb.getBetId())
                                        .event(pb.getEvent())
                                        .name(pb.getName())
                                        .odds(OddsConversionUtils.convertFractionToDecimalBet(fb.getOdds()))
                                        .stake(pb.getStake())
                                        .transactionId(pb.getTransactionId())
                                        .build()
                                        .toJsonString());
                    } catch (DecodeException | IllegalArgumentException de) {
                        routingContext.response()
                                .setStatusCode(400)
                                .setStatusMessage("Bad Request")
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(JsonError.builder()
                                        .errorCode(400)
                                        .errorMessage("Bad Request")
                                        .build()
                                        .toJsonString());
                    }
                }
            });
        }).setChunked(true).putHeader("content-type", "application/json; charset=utf-8").write(fb.toJsonString()).end();
    }

    /**
     *
     * This method expects a response body of events with odds in a fractional
     * format and converts them into events with odds in a decimal format.
     *
     *
     * @param r HttpClientRespose with a body containing available events
     * @param routingContext routingContext Represents the context for the
     * handling of a request
     */
    private void getAvailable(HttpClientResponse r, RoutingContext routingContext) {
        r.bodyHandler(buffer -> {
            JsonArray t = new JsonArray(buffer.toString("UTF-8"));
            List<DecimalEvent> result = t.stream()
                    .map(f -> {
                        //trycatch the decodeValue
                        return Json.decodeValue(((JsonObject) f).encode(), FractionalEvent.class);
                    }).map(f -> {
                        return DecimalEvent.builder()
                        .betId(f.getBetId())
                        .event(f.getEvent())
                        .name(f.getName())
                        .odds(OddsConversionUtils.convertFractionToDecimalBet(f.getOdds()))
                        .build();
                    }).collect(Collectors.toList());

            routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(result));
        });
    }
}
