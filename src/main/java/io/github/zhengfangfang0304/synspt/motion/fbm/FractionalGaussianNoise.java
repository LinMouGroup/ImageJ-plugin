package io.github.zhengfangfang0304.synspt.motion.fbm;

import java.util.Random;

/**
 * Facade that prefers Davies-Harte and automatically falls back to Hosking.
 */
public final class FractionalGaussianNoise {

    private final double hurst;
    private final DaviesHarteGenerator daviesHarteGenerator;
    private final HoskingGenerator hoskingGenerator;
    private boolean hoskingFallbackUsed;

    public FractionalGaussianNoise(double hurst) {
        this(hurst, new DaviesHarteGenerator(), new HoskingGenerator());
    }

    FractionalGaussianNoise(
            double hurst,
            DaviesHarteGenerator daviesHarteGenerator,
            HoskingGenerator hoskingGenerator
    ) {
        if (Double.isNaN(hurst)
                || Double.isInfinite(hurst)
                || hurst <= 0.0
                || hurst >= 1.0) {
            throw new IllegalArgumentException("Hurst exponent must be in (0, 1).");
        }
        if (daviesHarteGenerator == null || hoskingGenerator == null) {
            throw new IllegalArgumentException("FGN generators cannot be null.");
        }
        this.hurst = hurst;
        this.daviesHarteGenerator = daviesHarteGenerator;
        this.hoskingGenerator = hoskingGenerator;
    }

    public double[] generate(int length, Random random) {
        if (length < 0) {
            throw new IllegalArgumentException("Noise length cannot be negative.");
        }
        if (random == null) {
            throw new IllegalArgumentException("Random source cannot be null.");
        }

        hoskingFallbackUsed = false;
        try {
            return daviesHarteGenerator.generate(length, hurst, random);
        } catch (DaviesHarteException exception) {
            hoskingFallbackUsed = true;
            return hoskingGenerator.generate(length, hurst, random);
        }
    }

    public boolean wasHoskingFallbackUsed() {
        return hoskingFallbackUsed;
    }
}
