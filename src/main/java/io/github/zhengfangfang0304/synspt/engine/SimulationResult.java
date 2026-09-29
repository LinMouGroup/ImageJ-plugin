package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.scene.Scene;

/**
 * Immutable result of the trajectory/scene simulation stage.
 *
 * <p>Optical rendering consumes this object. RenderedSimulationResult owns
 * the ImagePlus, keeping the stochastic trajectory engine independent of
 * ImageJ image types.</p>
 */
public final class SimulationResult {

    private final SimulationPlan plan;
    private final Scene scene;

    public SimulationResult(
            SimulationPlan plan,
            Scene scene
    ) {
        if (plan == null || scene == null) {
            throw new IllegalArgumentException(
                    "Simulation plan and scene cannot be null."
            );
        }
        if (plan.getRequestConfig().getImageWidth() != scene.getWidthPixels()
                || plan.getRequestConfig().getImageHeight() != scene.getHeightPixels()
                || plan.getRequestConfig().getFrames() != scene.getFrameCount()
                || plan.getParticles().size() != scene.getParticleTracks().size()
                || Double.compare(
                        plan.getRequestConfig().getMicroscopeConfig().getPixelSizeUm(),
                        scene.getPixelSizeUm()
                ) != 0) {
            throw new IllegalArgumentException(
                    "Simulation plan and scene dimensions must be consistent."
            );
        }
        this.plan = plan;
        this.scene = scene;
    }

    public SimulationPlan getPlan() {
        return plan;
    }

    public Scene getScene() {
        return scene;
    }
}
