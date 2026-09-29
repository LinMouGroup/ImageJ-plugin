package io.github.zhengfangfang0304.particletracking.simulation.generators;

import io.github.zhengfangfang0304.particletracking.simulation.config.ImagingConfig;
import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfileResolver;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;
import io.github.zhengfangfang0304.particletracking.simulation.motion.ParticleMotionAssignment;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ProfileDrivenTrajectoryGenerationTest {

    private static final double TOLERANCE = 1.0e-12;

    private final TrajectoryGenerator generator =
            new TrajectoryGenerator();

    private final MotionProfileResolver resolver =
            new MotionProfileResolver();

    @Test
    void profileDrivenTrajectoryIsReproducible() {
        SimulationConfig config = config();
        ParticleMotionAssignment assignment = assignment(
                MotionType.SUBDIFFUSION,
                555L
        );

        List<Point2D.Double> first = generator.generateTrajectory(
                new Point2D.Double(100.0, 100.0),
                config,
                assignment
        );
        List<Point2D.Double> second = generator.generateTrajectory(
                new Point2D.Double(100.0, 100.0),
                config,
                assignment
        );

        assertEquals(first.size(), second.size());
        for (int sample = 0; sample < first.size(); sample++) {
            assertEquals(
                    first.get(sample).x,
                    second.get(sample).x,
                    TOLERANCE
            );
            assertEquals(
                    first.get(sample).y,
                    second.get(sample).y,
                    TOLERANCE
            );
        }
    }

    @Test
    void directedProfileAddsPhysicalDriftAfterFbmScaling() {
        SimulationConfig config = config();
        long trajectorySeed = 909L;
        List<Point2D.Double> superdiffusion =
                generator.generateTrajectory(
                        new Point2D.Double(100.0, 100.0),
                        config,
                        assignment(
                                MotionType.SUPERDIFFUSION,
                                trajectorySeed
                        )
                );
        List<Point2D.Double> directed =
                generator.generateTrajectory(
                        new Point2D.Double(100.0, 100.0),
                        config,
                        assignment(
                                MotionType.DIRECTED_ANOMALOUS_DIFFUSION,
                                trajectorySeed
                        )
                );

        for (int sample = 0; sample < directed.size(); sample++) {
            assertEquals(
                    sample * 0.5,
                    directed.get(sample).x
                            - superdiffusion.get(sample).x,
                    TOLERANCE
            );
            assertEquals(
                    superdiffusion.get(sample).y,
                    directed.get(sample).y,
                    TOLERANCE
            );
        }
        assertNotEquals(
                superdiffusion.get(4).x,
                directed.get(4).x
        );
    }

    private SimulationConfig config() {
        SimulationConfig config = SimulationConfig.defaultConfig();
        config.width = 512;
        config.height = 512;
        config.frames = 5;
        config.particleNumber = 1;
        config.setImagingConfig(new ImagingConfig(1.0, 0.5));
        return config;
    }

    private ParticleMotionAssignment assignment(
            MotionType motionType,
            long seed
    ) {
        return new ParticleMotionAssignment(
                resolver.resolve(motionType),
                seed
        );
    }
}
