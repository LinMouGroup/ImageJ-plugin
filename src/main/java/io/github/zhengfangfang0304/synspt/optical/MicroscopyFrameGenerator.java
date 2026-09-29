package io.github.zhengfangfang0304.synspt.optical;

import io.github.zhengfangfang0304.synspt.engine.SimulationResult;
import io.github.zhengfangfang0304.synspt.noise.CameraFrame;
import io.github.zhengfangfang0304.synspt.noise.CameraNoiseModel;
import io.github.zhengfangfang0304.synspt.noise.NoiseModel;
import io.github.zhengfangfang0304.synspt.util.SeedDerivation;

/** Executes optical rendering and camera noise for one simulation frame. */
public final class MicroscopyFrameGenerator {

    private final OpticalRenderer opticalRenderer;
    private final NoiseModel noiseModel;

    public MicroscopyFrameGenerator() {
        this(new CompositeOpticalRenderer(), new CameraNoiseModel());
    }

    public MicroscopyFrameGenerator(
            OpticalRenderer opticalRenderer,
            NoiseModel noiseModel
    ) {
        if (opticalRenderer == null || noiseModel == null) {
            throw new IllegalArgumentException(
                    "Optical renderer and noise model cannot be null."
            );
        }
        this.opticalRenderer = opticalRenderer;
        this.noiseModel = noiseModel;
    }

    public ExpectedPhotonFrame renderExpectedPhotons(
            SimulationResult simulationResult,
            int frameIndex
    ) {
        requireResult(simulationResult);
        return opticalRenderer.renderFrame(
                simulationResult.getScene(),
                frameIndex,
                simulationResult.getPlan().getRequestConfig().getMicroscopeConfig()
        );
    }

    public CameraFrame generateCameraFrame(
            SimulationResult simulationResult,
            int frameIndex
    ) {
        ExpectedPhotonFrame expectedPhotonFrame = renderExpectedPhotons(
                simulationResult,
                frameIndex
        );
        return noiseModel.apply(
                expectedPhotonFrame,
                simulationResult.getPlan().getRequestConfig().getMicroscopeConfig(),
                SeedDerivation.cameraNoiseSeed(
                        simulationResult.getPlan().getRequestConfig().getRandomSeed(),
                        frameIndex
                )
        );
    }

    private void requireResult(SimulationResult simulationResult) {
        if (simulationResult == null) {
            throw new IllegalArgumentException("Simulation result cannot be null.");
        }
    }
}
