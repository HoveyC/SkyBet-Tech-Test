package skybet.test.utils;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import skybet.test.pojo.FractionalOdds;

@RunWith(VertxUnitRunner.class)
public class OddsConverstionUtilsTest {

    @Test
    public void convertFractionToDecimalBetTest(TestContext context) {
        Double expected = 11.0;
        FractionalOdds fo = FractionalOdds.builder()
                .numerator(10)
                .denominator(1).build();
        Double d = OddsConversionUtils.convertFractionToDecimalBet(fo);
        context.assertEquals(expected, d);

    }

    @Test
    public void convertDecimalToFractionTest(TestContext context) {
        FractionalOdds expected = FractionalOdds.builder()
                .numerator(10)
                .denominator(1).build();
        FractionalOdds fo = OddsConversionUtils.convertDecimalBetToFraction(11.00);

        context.assertEquals(expected, fo);
    }

}
