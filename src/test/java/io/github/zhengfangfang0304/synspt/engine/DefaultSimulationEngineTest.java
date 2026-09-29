package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DefaultSimulationEngineTest {

    @Test
    public void generatesSceneUsingAllSelectedModelsAsParticleAssignments() {
        SimulationConfig config = allModelsConfig();
        SimulationResult result = new DefaultSimulationEngine().generate(
                config,
                SimulationProgressListener.NONE
        );
        EnumSet<MotionType> realized = EnumSet.noneOf(MotionType.class);
        assertSame(config, result.getPlan().getRequestConfig());
        assertEquals(17, result.getScene().getParticleTracks().size());
        assertEquals(64, result.getScene().getFrameCount());
        assertEquals(9, result.getPlan().getMotionComposition()
                .getComposition().size());
        for (ParticleTrack track : result.getScene().getParticleTracks()) {
            realized.add(track.getParticle().getMotionType());
        }
        assertEquals(EnumSet.allOf(MotionType.class), realized);
    }

    @Test
    public void completeEngineRunIsExactlySeedReproducible() {
        SimulationConfig config = allModelsConfig();
        DefaultSimulationEngine engine = new DefaultSimulationEngine();
        SimulationResult first = engine.generate(
                config,
                SimulationProgressListener.NONE
        );
        SimulationResult second = engine.generate(
                config,
                SimulationProgressListener.NONE
        );

        for (int index = 0;
                index < first.getScene().getParticleTracks().size(); index++) {
            ParticleTrack left = first.getScene().getParticleTracks().get(index);
            ParticleTrack right = second.getScene().getParticleTracks().get(index);
            assertEquals(left.getParticle().getMotionType(),
                    right.getParticle().getMotionType());
            assertSegmentsEqual(
                    left.getParticle().getStateSegments(),
                    right.getParticle().getStateSegments()
            );
            assertArrayEquals(
                    left.getTrajectory().copyXUm(),
                    right.getTrajectory().copyXUm(),
                    0.0
            );
            assertArrayEquals(
                    left.getTrajectory().copyYUm(),
                    right.getTrajectory().copyYUm(),
                    0.0
            );
        }
    }

    @Test
    public void reportsMonotonicProgressFromZeroToOneHundred() {
        final List<Integer> percentages = new ArrayList<Integer>();
        new DefaultSimulationEngine().generate(
                allModelsConfig(),
                new SimulationProgressListener() {
                    @Override
                    public void onProgress(int percent, String message) {
                        percentages.add(percent);
                        assertTrue(message != null && !message.isEmpty());
                    }
                }
        );

        assertEquals(0, percentages.get(0).intValue());
        assertEquals(100, percentages.get(percentages.size() - 1).intValue());
        for (int index = 1; index < percentages.size(); index++) {
            assertTrue(percentages.get(index) >= percentages.get(index - 1));
        }
    }

    @Test(expected = SimulationCancelledException.class)
    public void stopsCooperativelyBeforeTheFirstTrackWhenCancelled() {
        final boolean[] cancelled = new boolean[] {false};
        new DefaultSimulationEngine().generate(
                allModelsConfig(),
                new SimulationProgressListener() {
                    @Override
                    public void onProgress(int percent, String message) {
                        if (percent >= 5) {
                            cancelled[0] = true;
                        }
                    }
                },
                new SimulationCancellationToken() {
                    @Override
                    public boolean isCancellationRequested() {
                        return cancelled[0];
                    }
                }
        );
    }

    private SimulationConfig allModelsConfig() {
        return SimulationConfig.builder()
                .imageWidth(64)
                .imageHeight(48)
                .frames(64)
                .particleNumber(17)
                .selectedMotionTypes(EnumSet.allOf(MotionType.class))
                .randomSeed(20260819L)
                .build();
    }

    private void assertSegmentsEqual(
            List<StateSegment> left,
            List<StateSegment> right
    ) {
        assertEquals(left.size(), right.size());
        for (int index = 0; index < left.size(); index++) {
            assertEquals(left.get(index).getStartFrame(),
                    right.get(index).getStartFrame());
            assertEquals(left.get(index).getEndFrame(),
                    right.get(index).getEndFrame());
            assertEquals(left.get(index).getMotionType(),
                    right.get(index).getMotionType());
            assertEquals(left.get(index).getMotionParameters().toMetadata(),
                    right.get(index).getMotionParameters().toMetadata());
        }
    }
}
