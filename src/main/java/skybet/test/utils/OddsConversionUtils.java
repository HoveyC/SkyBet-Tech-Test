/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybet.test.utils;

import java.math.BigDecimal;
import skybet.test.pojo.FractionalOdds;

/**
 *
 * @author chrishovey
 */
public class OddsConversionUtils {

    /**
     *
     * This method accepts the bet as a decimal format subtracts 1 for the stake
     * calls the convertDecimalToFraction method and returns a fractional odds
     * object.
     *
     * @param odds this represents bet odds, 1 will be subtracted for the stake.
     * @return FractionalOdds pojo object.
     */
    public static FractionalOdds convertDecimalBetToFraction(Double odds) {
        if (odds == null) {
            throw new IllegalArgumentException();
        }
        BigDecimal x = new BigDecimal(odds.toString()).subtract(new BigDecimal(1));
        int[] numDen = convertDecimalToFraction(x);
        return FractionalOdds.builder()
                .numerator(numDen[0])
                .denominator(numDen[1])
                .build();
    }

    /**
     *
     * @param x this represents the bet in a big decimal format
     * @return an int array containing numerator and denominator representing
     * the decimal bet as a fraction.
     */
    private static int[] convertDecimalToFraction(BigDecimal x) {
        BigDecimal den = BigDecimal.TEN.pow(getNumberOfDecimals(x)); // denominator
        BigDecimal num = (den.multiply(x)); // numerator
        Rational r = new Rational(num.intValue(), den.intValue());
        int[] rf = {r.numerator(), r.denominator()};
        return rf;
    }

    /**
     * This method converts FractionalOdds object into bet odds represented in
     * decimal format.
     *
     * @param fo FractionalOdds object to be converted to double.
     * @return double representing a bet in a decimal format.
     */
    public static double convertFractionToDecimalBet(FractionalOdds fo) {
        return (double) fo.getNumerator() / fo.getDenominator() + 1;
    }

    /**
     *
     * @param b big decimal
     * @return interger value of the number of decimal places in a big decimal
     */
    public static int getNumberOfDecimals(BigDecimal b) {
        return Math.max(0, b.stripTrailingZeros().scale());
    }
}
