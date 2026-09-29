package io.github.zhengfangfang0304.synspt.motion.parameters;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Map;

/** Parameters for diffusion inside a circular reflecting confinement. */
public final class ConfinedParameters implements MotionParameters {

    private final double diffusionUm2PerSecond;
    private final double radiusUm;
    private final int substepsPerFrame;

    public ConfinedParameters(
            double diffusionUm2PerSecond,
            double radiusUm,
            int substepsPerFrame
    ) {
        ParameterSupport.requirePositive(diffusionUm2PerSecond, "Diffusion coefficient");
        ParameterSupport.requirePositive(radiusUm, "Confinement radius");
        if (substepsPerFrame <= 0) {
            throw new IllegalArgumentException("Substeps per frame must be positive.");
        }
        this.diffusionUm2PerSecond = diffusionUm2PerSecond;
        this.radiusUm = radiusUm;
        this.substepsPerFrame = substepsPerFrame;
    }

    @Override
    public MotionType getMotionType() {
        return MotionType.CONFINED;
    }

    public double getDiffusionUm2PerSecond() {
        return diffusionUm2PerSecond;
    }

    public double getRadiusUm() {
        return radiusUm;
    }

    public int getSubstepsPerFrame() {
        return substepsPerFrame;
    }

    @Override
    public Map<String, String> toMetadata() {
        return ParameterSupport.metadata(
                "D_um2_per_s", ParameterSupport.number(diffusionUm2PerSecond),
                "radius_um", ParameterSupport.number(radiusUm),
                "substeps_per_frame", Integer.toString(substepsPerFrame),
                "boundary", "circular_rejecting_reflection"
        );
    }
}
