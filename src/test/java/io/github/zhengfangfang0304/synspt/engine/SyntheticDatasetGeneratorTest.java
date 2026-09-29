package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SyntheticDatasetGeneratorTest {

    @Test
    public void generatesAllModelsThroughARealImagePlus() {
        SimulationConfig config = config();
        RenderedSimulationResult result = new SyntheticDatasetGenerator().generate(
                config,
                SimulationProgressListener.NONE
        );

        assertSame(config, result.getPlan().getRequestConfig());
        assertEquals(9, result.getScene().getParticleTracks().size());
        assertEquals(3, result.getImagePlus().getStackSize());
        assertEquals(16, result.getImagePlus().getBitDepth());
    }

    @Test
    public void endToEndPixelsAndProgressAreSeedReproducible() {
        final List<Integer> percentages = new ArrayList<Integer>();
        SimulationConfig config = config();
        SyntheticDatasetGenerator generator = new SyntheticDatasetGenerator();
        RenderedSimulationResult first = generator.generate(
                config,
                new SimulationProgressListener() {
                    @Override
                    public void onProgress(int percent, String message) {
                        percentages.add(percent);
                    }
                }
        );
        RenderedSimulationResult second = generator.generate(
                config,
                SimulationProgressListener.NONE
        );

        for (int slice = 1; slice <= config.getFrames(); slice++) {
            assertArrayEquals(
                    (short[]) first.getImagePlus().getStack().getPixels(slice),
                    (short[]) second.getImagePlus().getStack().getPixels(slice)
            );
        }
        assertEquals(0, percentages.get(0).intValue());
        assertEquals(100, percentages.get(percentages.size() - 1).intValue());
        for (int index = 1; index < percentages.size(); index++) {
            assertTrue(percentages.get(index) >= percentages.get(index - 1));
        }
    }

    private SimulationConfig config() {
        return SimulationConfig.builder()
                .imageWidth(24)
                .imageHeight(20)
                .frames(3)
                .particleNumber(9)
                .selectedMotionTypes(EnumSet.allOf(MotionType.class))
                .randomSeed(60606L)
                .build();
    }
}
