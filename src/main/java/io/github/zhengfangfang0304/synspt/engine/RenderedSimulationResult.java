package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.scene.Scene;

import ij.ImagePlus;

/** Complete in-memory synthetic dataset passed to export and GUI layers. */
public final class RenderedSimulationResult {

    private final SimulationResult simulationResult;
    private final ImagePlus imagePlus;

    public RenderedSimulationResult(
            SimulationResult simulationResult,
            ImagePlus imagePlus
    ) {
        if (simulationResult == null || imagePlus == null) {
            throw new IllegalArgumentException(
                    "Simulation result and ImagePlus cannot be null."
            );
        }
        SimulationConfig config =
                simulationResult.getPlan().getRequestConfig();
        if (imagePlus.getWidth() != config.getImageWidth()
                || imagePlus.getHeight() != config.getImageHeight()
                || imagePlus.getStackSize() != config.getFrames()
                || imagePlus.getBitDepth()
                != config.getMicroscopeConfig().getBitDepth()) {
            throw new IllegalArgumentException(
                    "ImagePlus dimensions and bit depth must match the simulation."
            );
        }
        this.simulationResult = simulationResult;
        this.imagePlus = imagePlus;
    }

    public SimulationResult getSimulationResult() {
        return simulationResult;
    }

    public SimulationPlan getPlan() {
        return simulationResult.getPlan();
    }

    public Scene getScene() {
        return simulationResult.getScene();
    }

    public ImagePlus getImagePlus() {
        return imagePlus;
    }
}
