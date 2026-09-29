package io.github.zhengfangfang0304.synspt.motion.fbm;

import java.util.Random;

/** Fractional Gaussian-noise sampler using Hosking recursion. */
public class HoskingGenerator {

    private static final double VARIANCE_TOLERANCE = 1.0e-12;

    public double[] generate(int length, double hurst, Random random) {
        validateArguments(length, hurst, random);
        if (length == 0) {
            return new double[0];
        }

        DaviesHarteGenerator covarianceFunction = new DaviesHarteGenerator();
        double[] covariance = new double[length];
        for (int lag = 0; lag < length; lag++) {
            covariance[lag] = covarianceFunction.autocovariance(lag, hurst);
        }

        double[] noise = new double[length];
        double[] coefficients = new double[length];
        double[] previousCoefficients = new double[length];
        double innovationVariance = covariance[0];
        noise[0] = Math.sqrt(innovationVariance) * random.nextGaussian();

        for (int sample = 1; sample < length; sample++) {
            System.arraycopy(
                    coefficients,
                    0,
                    previousCoefficients,
                    0,
                    sample - 1
            );

            double reflection = covariance[sample];
            for (int index = 0; index < sample - 1; index++) {
                reflection -= previousCoefficients[index]
                        * covariance[sample - index - 1];
            }
            reflection /= innovationVariance;
            coefficients[sample - 1] = reflection;

            for (int index = 0; index < sample - 1; index++) {
                coefficients[index] = previousCoefficients[index]
                        - reflection * previousCoefficients[sample - index - 2];
            }

            innovationVariance *= 1.0 - reflection * reflection;
            if (innovationVariance < -VARIANCE_TOLERANCE) {
                throw new IllegalStateException(
                        "Hosking innovation variance became negative."
                );
            }
            innovationVariance = Math.max(0.0, innovationVariance);

            double conditionalMean = 0.0;
            for (int index = 0; index < sample; index++) {
                conditionalMean += coefficients[index]
                        * noise[sample - index - 1];
            }
            noise[sample] = conditionalMean
                    + Math.sqrt(innovationVariance) * random.nextGaussian();
        }
        return noise;
    }

    private void validateArguments(int length, double hurst, Random random) {
        if (length < 0) {
            throw new IllegalArgumentException("Noise length cannot be negative.");
        }
        if (Double.isNaN(hurst)
                || Double.isInfinite(hurst)
                || hurst <= 0.0
                || hurst >= 1.0) {
            throw new IllegalArgumentException("Hurst exponent must be in (0, 1).");
        }
        if (random == null) {
            throw new IllegalArgumentException("Random source cannot be null.");
        }
    }
}
