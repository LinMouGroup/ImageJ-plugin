package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;

import org.junit.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultiStateGeneratorTest {

    @Test
    public void createsTwoToFiveBasicStatesCoveringTheWholeTrajectory() {
        SimulationConfig config = multiStateConfig(100, 4422L);
        List<StateSegment> states = new MultiStateGenerator().generate(config, 1);

        assertTrue(states.size() >= 2 && states.size() <= 5);
        assertEquals(0, states.get(0).getStartFrame());
        assertEquals(99, states.get(states.size() - 1).getEndFrame());
        int expectedStart = 0;
        for (int index = 0; index < states.size(); index++) {
            StateSegment state = states.get(index);
            assertEquals(1, state.getParticleId());
            assertEquals(index, state.getStateIndex());
            assertEquals(expectedStart, state.getStartFrame());
            assertTrue(MotionType.baseTypes().contains(state.getMotionType()));
            assertEquals(state.getMotionType(),
                    state.getMotionParameters().getMotionType());
            expectedStart = state.getEndFrame() + 1;
        }
    }

    @Test
    public void fixedRootSeedReproducesTypesBoundariesAndParameters() {
        SimulationConfig config = multiStateConfig(80, 991L);
        MultiStateGenerator generator = new MultiStateGenerator();
        List<StateSegment> first = generator.generate(config, 4);
        List<StateSegment> repeated = generator.generate(config, 4);

        assertEquals(first.size(), repeated.size());
        for (int index = 0; index < first.size(); index++) {
            assertEquals(first.get(index).getStartFrame(),
                    repeated.get(index).getStartFrame());
            assertEquals(first.get(index).getEndFrame(),
                    repeated.get(index).getEndFrame());
            assertEquals(first.get(index).getMotionType(),
                    repeated.get(index).getMotionType());
            assertEquals(first.get(index).getMotionParameters().toMetadata(),
                    repeated.get(index).getMotionParameters().toMetadata());
        }
    }

    @Test
    public void shortTrajectoryStillGetsTwoPositiveStates() {
        List<StateSegment> states = new MultiStateGenerator().generate(
                multiStateConfig(3, 7L),
                1
        );
        assertEquals(2, states.size());
        assertEquals(0, states.get(0).getStartFrame());
        assertEquals(2, states.get(1).getEndFrame());
        assertTrue(states.get(0).getDurationFrames() >= 1);
        assertTrue(states.get(1).getDurationFrames() >= 1);
    }

    private SimulationConfig multiStateConfig(int frames, long seed) {
        return SimulationConfig.builder()
                .frames(frames)
                .particleNumber(1)
                .selectedMotionTypes(EnumSet.of(MotionType.MULTI_STATE))
                .randomSeed(seed)
                .build();
    }
}
