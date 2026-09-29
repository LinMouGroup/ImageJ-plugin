package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.noise.SnrDefinition;

/** Immutable user-level noise request for one simulation. */
public final class NoiseConfig {

    public static final double DEFAULT_TARGET_SNR = 10.0;

    private final double targetSnr;
    private final SnrDefinition snrDefinition;

    public NoiseConfig(double targetSnr, SnrDefinition snrDefinition) {
        if (Double.isNaN(targetSnr)
                || Double.isInfinite(targetSnr)
                || targetSnr <= 0.0) {
            throw new IllegalArgumentException(
                    "Target SNR must be finite and positive."
            );
        }
        if (snrDefinition == null) {
            throw new IllegalArgumentException("SNR definition cannot be null.");
        }
        this.targetSnr = targetSnr;
        this.snrDefinition = snrDefinition;
    }

    public static NoiseConfig defaultConfig() {
        return new NoiseConfig(
                DEFAULT_TARGET_SNR,
                SnrDefinition.PEAK_PIXEL_SIGNAL
        );
    }

    public double getTargetSnr() {
        return targetSnr;
    }

    public SnrDefinition getSnrDefinition() {
        return snrDefinition;
    }
}
