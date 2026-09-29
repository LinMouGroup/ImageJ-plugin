package io.github.zhengfangfang0304.particletracking.simulation.generators;

import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfile;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfileResolver;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FBMTrajectoryGeneratorTest {

    private final FBMTrajectoryGenerator generator =
            new FBMTrajectoryGenerator();

    private final MotionProfileResolver resolver =
            new MotionProfileResolver();

    @Test
    void everyMotionProfileUsesTheUnifiedFbmPath() {
        for (MotionType motionType : MotionType.values()) {
            MotionProfile profile = resolver.resolve(motionType);

            double[] trajectory = generator.generate(
                    32,
                    profile,
                    new Random(100L + motionType.ordinal())
            );

            assertEquals(33, trajectory.length);
            assertEquals(0.0, trajectory[0]);
        }
    }

    @Test
    void dedicatedRandomSeedReproducesDimensionlessTrajectory() {
        MotionProfile profile = resolver.resolve(
                MotionType.DIRECTED_ANOMALOUS_DIFFUSION
        );

        double[] first = generator.generate(
                64,
                profile,
                new Random(777L)
        );
        double[] second = generator.generate(
                64,
                profile,
                new Random(777L)
        );

        assertArrayEquals(first, second);
    }
}
