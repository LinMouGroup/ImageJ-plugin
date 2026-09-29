package io.github.zhengfangfang0304.particletracking.simulation.models.fbm;

import java.util.Random;

/**
 * Generates fractional Gaussian noise with Hosking's recursive method.
 */
public class HoskingGenerator {

    private static final double VARIANCE_TOLERANCE = 1.0e-12;

    /**
     * Generates unit-time fractional Gaussian noise.
     *
     * @param length number of increments
     * @param hurst Hurst exponent in {@code (0, 1)}
     * @param random Gaussian random source
     * @return correlated Gaussian increments with unit marginal variance
     */
    public double[] generate(
            int length,
            double hurst,
            Random random
    ) {
        validateArguments(length, hurst, random);

        if (length == 0) {
            return new double[0];
        }

        DaviesHarteGenerator covariance =
                new DaviesHarteGenerator();
        double[] autocovariance = new double[length];

        for (int lag = 0; lag < length; lag++) {
            autocovariance[lag] =
                    covariance.autocovariance(lag, hurst);
        }

        double[] noise = new double[length];
        double[] coefficients = new double[length];
        double[] previousCoefficients = new double[length];
        double innovationVariance = autocovariance[0];

        noise[0] =
                Math.sqrt(innovationVariance)
                        * random.nextGaussian();

        for (int i = 1; i < length; i++) {
            System.arraycopy(
                    coefficients,
                    0,
                    previousCoefficients,
                    0,
                    i - 1
            );

            double reflectionCoefficient =
                    autocovariance[i];
            for (int j = 0; j < i - 1; j++) {
                reflectionCoefficient -=
                        previousCoefficients[j]
                                * autocovariance[i - j - 1];
            }
            reflectionCoefficient /= innovationVariance;
            coefficients[i - 1] = reflectionCoefficient;

            for (int j = 0; j < i - 1; j++) {
                coefficients[j] =
                        previousCoefficients[j]
                                - reflectionCoefficient
                                * previousCoefficients[i - j - 2];
            }

            innovationVariance *=
                    1.0
                            - reflectionCoefficient
                            * reflectionCoefficient;

            if (innovationVariance < -VARIANCE_TOLERANCE) {
                throw new IllegalStateException(
                        "Hosking innovation variance became negative."
                );
            }
            innovationVariance =
                    Math.max(0.0, innovationVariance);

            double conditionalMean = 0.0;
            for (int j = 0; j < i; j++) {
                conditionalMean +=
                        coefficients[j]
                                * noise[i - j - 1];
            }

            noise[i] =
                    conditionalMean
                            + Math.sqrt(innovationVariance)
                            * random.nextGaussian();
        }

        return noise;
    }

    private void validateArguments(
            int length,
            double hurst,
            Random random
    ) {
        if (length < 0) {
            throw new IllegalArgumentException(
                    "Noise length cannot be negative."
            );
        }
        if (!(hurst > 0.0 && hurst < 1.0)) {
            throw new IllegalArgumentException(
                    "Hurst exponent must be between zero and one."
            );
        }
        if (random == null) {
            throw new IllegalArgumentException(
                    "Random generator cannot be null."
            );
        }
    }
}
