package io.github.zhengfangfang0304.synspt.config;

/** Hidden configuration for stochastic motion-composition generation. */
public final class RandomCompositionConfig {

    public static final double DEFAULT_CONCENTRATION = 8.0;

    private final double concentration;

    public RandomCompositionConfig(double concentration) {
        if (Double.isNaN(concentration)
                || Double.isInfinite(concentration)
                || concentration <= 0.0) {
            throw new IllegalArgumentException(
                    "Dirichlet concentration must be finite and positive."
            );
        }
        this.concentration = concentration;
    }

    public static RandomCompositionConfig defaultConfig() {
        return new RandomCompositionConfig(DEFAULT_CONCENTRATION);
    }

    public double getConcentration() {
        return concentration;
    }
}
