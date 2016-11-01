package de.s2.gsim.testutils;

import java.util.function.Supplier;

public class NormalDistributedUtil {

	public static boolean sample(double sampleSize, double samples, double svar, double expected, Supplier<Double> func) {
        double err = 2.576 * (svar / (Math.sqrt((double) sampleSize)));// 99%
        int countExpected = 0;

        for (int i = 0; i < samples; i++) {
			double val = func.get();
			if (val <= (expected + err) && val >= (expected - err)) {
				countExpected++;
			}

		}


        double minimumExpectedCorrectSamples = ((double) samples) * 0.99;

		return Math.abs(countExpected) - minimumExpectedCorrectSamples == expected;
	}

}
