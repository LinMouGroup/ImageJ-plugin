package io.github.zhengfangfang0304.synspt.optical;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.engine.DefaultSimulationEngine;
import io.github.zhengfangfang0304.synspt.engine.SimulationProgressListener;
import io.github.zhengfangfang0304.synspt.engine.SimulationResult;
import io.github.zhengfangfang0304.synspt.noise.CameraFrame;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MicroscopyFrameGeneratorTest {

    @Test
    public void phaseThreeSceneProducesReproducibleNoisyCameraFrames() {
        SimulationConfig config = SimulationConfig.builder()
                .imageWidth(32)
                .imageHeight(24)
                .frames(3)
                .particleNumber(2)
                .randomSeed(40404L)
                .build();
        SimulationResult simulation = new DefaultSimulationEngine().generate(
                config,
                SimulationProgressListener.NONE
        );
        MicroscopyFrameGenerator generator = new MicroscopyFrameGenerator();
        ExpectedPhotonFrame expected = generator.renderExpectedPhotons(simulation, 0);
        CameraFrame first = generator.generateCameraFrame(simulation, 0);
        CameraFrame repeated = generator.generateCameraFrame(simulation, 0);
        CameraFrame nextFrame = generator.generateCameraFrame(simulation, 1);

        assertEquals(32, expected.getWidth());
        assertEquals(24, expected.getHeight());
        assertTrue(sum(expected) > 32.0 * 24.0
                * config.getMicroscopeConfig().getBackgroundPhotons());
        assertArrayEquals(first.copyPixels(), repeated.copyPixels());
        assertTrue(!Arrays.equals(first.copyPixels(), nextFrame.copyPixels()));
    }

    private double sum(ExpectedPhotonFrame frame) {
        double result = 0.0;
        for (double value : frame.copyExpectedPhotons()) {
            result += value;
        }
        return result;
    }
}
