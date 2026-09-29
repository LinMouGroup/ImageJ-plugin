package io.github.zhengfangfang0304.synspt.motion.parameters;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Map;

/** Parameters for fractional Brownian motion. */
public final class FbmParameters implements MotionParameters {

    private final double hurstExponent;
    private final double generalizedDiffusionUm2PerSecondAlpha;

    public FbmParameters(
            double hurstExponent,
            double generalizedDiffusionUm2PerSecondAlpha
    ) {
        ParameterSupport.requireOpenUnitInterval(hurstExponent, "Hurst exponent");
        ParameterSupport.requirePositive(
                generalizedDiffusionUm2PerSecondAlpha,
                "Generalized diffusion coefficient"
        );
        this.hurstExponent = hurstExponent;
        this.generalizedDiffusionUm2PerSecondAlpha =
                generalizedDiffusionUm2PerSecondAlpha;
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.FBM;
    }

    public double getHurstExponent() {
        return hurstExponent;
    }

    public double getAlpha() {
        return 2.0 * hurstExponent;
    }

    public double getGeneralizedDiffusionUm2PerSecondAlpha() {
        return generalizedDiffusionUm2PerSecondAlpha;
    }

    @Override
    public Map<String, String> toMetadata() {
        return ParameterSupport.metadata(
                "H", ParameterSupport.number(hurstExponent),
                "alpha", ParameterSupport.number(getAlpha()),
                "K_H_um2_per_s_alpha",
                ParameterSupport.number(generalizedDiffusionUm2PerSecondAlpha)
        );
    }
}
