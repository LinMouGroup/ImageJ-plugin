package io.github.zhengfangfang0304.particletracking.simulation.models.fbm;

import java.util.Random;

/**
 * Generates correlated Gaussian increments for fractional Brownian motion.
 *
 * <p>Davies-Harte is always attempted first. An invalid circulant embedding
 * automatically falls back to Hosking; callers do not select the algorithm.</p>
 */
public class FractionalGaussianNoise {

    private final double hurst;

    private final Random random;

    private final DaviesHarteGenerator daviesHarteGenerator;

    private final HoskingGenerator hoskingGenerator;

    private boolean hoskingFallbackUsed;

    /**
     * Creates FGN directly from the dimensionless Hurst exponent.
     */
    public FractionalGaussianNoise(double hurst) {
        this(hurst, new Random());
    }

    /**
     * Creates reproducible FGN directly from H.
     */
    public FractionalGaussianNoise(
            double hurst,
            Random random
    ) {
        this(
                hurst,
                random,
                new DaviesHarteGenerator(),
                new HoskingGenerator()
        );
    }

    FractionalGaussianNoise(
            double hurst,
            Random random,
            DaviesHarteGenerator daviesHarteGenerator,
            HoskingGenerator hoskingGenerator
    ) {
        initializeValidation(
                hurst,
                random,
                daviesHarteGenerator,
                hoskingGenerator
        );

        this.hurst = hurst;
        this.random = random;
        this.daviesHarteGenerator = daviesHarteGenerator;
        this.hoskingGenerator = hoskingGenerator;
    }

    private void initializeValidation(
            double hurst,
            Random random,
            DaviesHarteGenerator daviesHarteGenerator,
            HoskingGenerator hoskingGenerator
    ) {
        if (!Double.isFinite(hurst)
                || !(hurst > 0.0 && hurst < 1.0)) {
            throw new IllegalArgumentException(
                    "Hurst exponent must be finite and between zero and one."
            );
        }
        if (random == null) {
            throw new IllegalArgumentException(
                    "Random generator cannot be null."
            );
        }
        if (daviesHarteGenerator == null) {
            throw new IllegalArgumentException(
                    "Davies-Harte generator cannot be null."
            );
        }
        if (hoskingGenerator == null) {
            throw new IllegalArgumentException(
                    "Hosking generator cannot be null."
            );
        }

    }

    /**
     * Generates fractional Gaussian noise increments.
     */
    public double[] generate(int length) {
        if (length < 0) {
            throw new IllegalArgumentException(
                    "Noise length cannot be negative."
            );
        }

        hoskingFallbackUsed = false;
        try {
            return daviesHarteGenerator.generate(
                    length,
                    hurst,
                    random
            );
        } catch (DaviesHarteException exception) {
            hoskingFallbackUsed = true;
            return hoskingGenerator.generate(
                    length,
                    hurst,
                    random
            );
        }
    }

    boolean usedHoskingFallback() {
        return hoskingFallbackUsed;
    }
}
