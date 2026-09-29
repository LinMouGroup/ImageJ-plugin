package io.github.zhengfangfang0304.synspt.motion.parameters;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Map;

/** Parameters for free Brownian diffusion. */
public final class BrownianParameters implements MotionParameters {

    private final double diffusionUm2PerSecond;

    public BrownianParameters(double diffusionUm2PerSecond) {
        ParameterSupport.requirePositive(diffusionUm2PerSecond, "Diffusion coefficient");
        this.diffusionUm2PerSecond = diffusionUm2PerSecond;
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.BROWNIAN;
    }

    public double getDiffusionUm2PerSecond() {
        return diffusionUm2PerSecond;
    }

    @Override
    public Map<String, String> toMetadata() {
        return ParameterSupport.metadata(
                "D_um2_per_s", ParameterSupport.number(diffusionUm2PerSecond)
        );
    }
}
