package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.sampling.DefaultMotionParameterGenerator;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MotionModelFactoryTest {

    @Test
    public void everyApprovedModelIsCreatedAndSeedReproducible() {
        double frameInterval = 1.0 / 30.0;
        DefaultMotionParameterGenerator generator =
                new DefaultMotionParameterGenerator();
        for (MotionType motionType : MotionType.baseTypes()) {
            MotionParameters parameters = generator.generate(
                    motionType,
                    MotionParameterDistributionConfig.defaultConfig(),
                    frameInterval,
                    200L + motionType.ordinal()
            );
            MotionModel model = MotionModelFactory.create(parameters);
            MotionContext context = new MotionContext(
                    128,
                    frameInterval,
                    800L + motionType.ordinal()
            );

            Trajectory first = model.generate(context);
            Trajectory second = model.generate(context);

            assertEquals(motionType, model.getType());
            assertEquals(128, first.length());
            assertEquals(0.0, first.getXUm(0), 0.0);
            assertEquals(0.0, first.getYUm(0), 0.0);
            assertArrayEquals(first.copyXUm(), second.copyXUm(), 0.0);
            assertArrayEquals(first.copyYUm(), second.copyYUm(), 0.0);
        }
    }
}
