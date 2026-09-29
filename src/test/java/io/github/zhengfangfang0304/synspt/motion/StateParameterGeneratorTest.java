package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class StateParameterGeneratorTest {

    @Test
    public void sameModelInDifferentStatesReceivesIndependentParameters() {
        StateParameterGenerator generator = new StateParameterGenerator();
        MotionParameterDistributionConfig config =
                MotionParameterDistributionConfig.defaultConfig();
        MotionParameters first = generator.generate(
                MotionType.BROWNIAN, config, 1.0 / 30.0, 81L, 1, 0
        );
        MotionParameters second = generator.generate(
                MotionType.BROWNIAN, config, 1.0 / 30.0, 81L, 1, 1
        );
        MotionParameters repeated = generator.generate(
                MotionType.BROWNIAN, config, 1.0 / 30.0, 81L, 1, 0
        );

        assertFalse(first.toMetadata().equals(second.toMetadata()));
        assertEquals(first.toMetadata(), repeated.toMetadata());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCompositeStateParameters() {
        new StateParameterGenerator().generate(
                MotionType.MULTI_STATE,
                MotionParameterDistributionConfig.defaultConfig(),
                1.0 / 30.0,
                1L,
                1,
                0
        );
    }
}
