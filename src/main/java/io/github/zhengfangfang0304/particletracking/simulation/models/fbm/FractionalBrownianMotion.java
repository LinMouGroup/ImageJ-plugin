package io.github.zhengfangfang0304.particletracking.simulation.models.fbm;

import java.util.Random;

/**
 * Generates a one-dimensional fractional Brownian motion trajectory.
 *
 * <p>Correlated increments come from {@link FractionalGaussianNoise}; this
 * class only performs the cumulative sum. Every trajectory starts at
 * {@code 0.0}.</p>
 */
public class FractionalBrownianMotion {

    private final FractionalGaussianNoise noiseGenerator;

    /**
     * Creates dimensionless FBM directly from its Hurst exponent.
     */
    public FractionalBrownianMotion(double hurst) {
        this(hurst, new Random());
    }

    /**
     * Creates reproducible dimensionless FBM directly from H.
     */
    public FractionalBrownianMotion(
            double hurst,
            Random random
    ) {
        this(new FractionalGaussianNoise(hurst, random));
    }

    FractionalBrownianMotion(
            FractionalGaussianNoise noiseGenerator
    ) {
        if (noiseGenerator == null) {
            throw new IllegalArgumentException(
                    "FGN generator cannot be null."
            );
        }
        this.noiseGenerator = noiseGenerator;
    }

    /**
     * Generates {@code numberOfIncrements + 1} trajectory samples.
     *
     * @param numberOfIncrements number of correlated Gaussian increments
     * @return an FBM trajectory beginning with {@code 0.0}
     */
    public double[] generate(int numberOfIncrements) {
        if (numberOfIncrements < 0) {
            throw new IllegalArgumentException(
                    "Number of increments cannot be negative."
            );
        }

        double[] increments =
                noiseGenerator.generate(numberOfIncrements);
        double[] trajectory =
                new double[numberOfIncrements + 1];

        for (int i = 0; i < increments.length; i++) {
            trajectory[i + 1] = trajectory[i] + increments[i];
        }

        return trajectory;
    }

    boolean usedHoskingFallback() {
        return noiseGenerator.usedHoskingFallback();
    }
}
