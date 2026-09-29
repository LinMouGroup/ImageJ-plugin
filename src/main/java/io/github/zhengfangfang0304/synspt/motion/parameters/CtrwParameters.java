package io.github.zhengfangfang0304.synspt.motion.parameters;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Map;

/** Parameters for a subdiffusive continuous-time random walk. */
public final class CtrwParameters implements MotionParameters {

    private final double alpha;
    private final double diffusionUm2PerSecond;
    private final double minimumWaitingTimeSeconds;

    public CtrwParameters(
            double alpha,
            double diffusionUm2PerSecond,
            double minimumWaitingTimeSeconds
    ) {
        ParameterSupport.requireOpenUnitInterval(alpha, "CTRW alpha");
        ParameterSupport.requirePositive(diffusionUm2PerSecond, "Diffusion coefficient");
        ParameterSupport.requirePositive(
                minimumWaitingTimeSeconds,
                "Minimum waiting time"
        );
        this.alpha = alpha;
        this.diffusionUm2PerSecond = diffusionUm2PerSecond;
        this.minimumWaitingTimeSeconds = minimumWaitingTimeSeconds;
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.CTRW;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getDiffusionUm2PerSecond() {
        return diffusionUm2PerSecond;
    }

    public double getMinimumWaitingTimeSeconds() {
        return minimumWaitingTimeSeconds;
    }

    @Override
    public Map<String, String> toMetadata() {
        return ParameterSupport.metadata(
                "alpha", ParameterSupport.number(alpha),
                "D_um2_per_s", ParameterSupport.number(diffusionUm2PerSecond),
                "tau0_s", ParameterSupport.number(minimumWaitingTimeSeconds)
        );
    }
}
