package io.github.zhengfangfang0304.synspt.motion.parameters;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Map;

/** Parameters for scaled Brownian motion. */
public final class SbmParameters implements MotionParameters {

    private final double alpha;
    private final double generalizedDiffusionUm2PerSecondAlpha;

    public SbmParameters(
            double alpha,
            double generalizedDiffusionUm2PerSecondAlpha
    ) {
        if (!ParameterSupport.isFinite(alpha) || alpha <= 0.0 || alpha > 2.0) {
            throw new IllegalArgumentException("SBM alpha must be in (0, 2].");
        }
        ParameterSupport.requirePositive(
                generalizedDiffusionUm2PerSecondAlpha,
                "Generalized diffusion coefficient"
        );
        this.alpha = alpha;
        this.generalizedDiffusionUm2PerSecondAlpha =
                generalizedDiffusionUm2PerSecondAlpha;
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.SBM;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getGeneralizedDiffusionUm2PerSecondAlpha() {
        return generalizedDiffusionUm2PerSecondAlpha;
    }

    @Override
    public Map<String, String> toMetadata() {
        return ParameterSupport.metadata(
                "alpha", ParameterSupport.number(alpha),
                "K_alpha_um2_per_s_alpha",
                ParameterSupport.number(generalizedDiffusionUm2PerSecondAlpha)
        );
    }
}
