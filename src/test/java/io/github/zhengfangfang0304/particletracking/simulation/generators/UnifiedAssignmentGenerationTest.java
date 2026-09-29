package io.github.zhengfangfang0304.particletracking.simulation.generators;

import io.github.zhengfangfang0304.particletracking.simulation.config.MotionSelectionConfig;
import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticDataset;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticParticle;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnifiedAssignmentGenerationTest {

    private final SyntheticDatasetGenerator generator =
            new SyntheticDatasetGenerator();

    @Test
    void singleTypeSelectionAssignsEveryGeneratedParticle() {
        SimulationConfig config = config(6);
        MotionSelectionConfig selection = new MotionSelectionConfig(
                EnumSet.of(MotionType.SUBDIFFUSION)
        );
        config.setMotionSelectionConfig(selection);

        SyntheticDataset dataset = generator.generate(
                config,
                101L
        );

        assertEquals(6, dataset.getParticles().size());
        for (SyntheticParticle particle : dataset.getParticles()) {
            assertNotNull(particle.getMotionAssignment());
            assertEquals(
                    MotionType.SUBDIFFUSION,
                    particle.getMotionAssignment()
                            .getMotionProfile()
                            .getMotionType()
            );
        }
    }

    @Test
    void multipleTypesProduceDifferentParticleProfiles() {
        SimulationConfig config = config(8);
        MotionSelectionConfig selection = new MotionSelectionConfig(
                EnumSet.of(
                        MotionType.NORMAL_DIFFUSION,
                        MotionType.SUBDIFFUSION,
                        MotionType.SUPERDIFFUSION,
                        MotionType.DIRECTED_ANOMALOUS_DIFFUSION
                )
        );
        config.setMotionSelectionConfig(selection);

        SyntheticDataset dataset = generator.generate(
                config,
                202L
        );
        Set<String> profileIds = new HashSet<>();
        for (SyntheticParticle particle : dataset.getParticles()) {
            profileIds.add(
                    particle.getMotionAssignment()
                            .getMotionProfile()
                            .getProfileId()
            );
        }

        assertEquals(4, profileIds.size());
    }

    @Test
    void masterSeedReproducesAssignmentsAndTrajectories() {
        MotionSelectionConfig selection = new MotionSelectionConfig(
                EnumSet.of(
                        MotionType.NORMAL_DIFFUSION,
                        MotionType.SUBDIFFUSION,
                        MotionType.SUPERDIFFUSION
                )
        );

        SimulationConfig firstConfig = config(6);
        SimulationConfig secondConfig = config(6);
        firstConfig.setMotionSelectionConfig(selection);
        secondConfig.setMotionSelectionConfig(selection);
        SyntheticDataset first = generator.generate(
                firstConfig,
                303L
        );
        SyntheticDataset second = generator.generate(
                secondConfig,
                303L
        );

        assertDatasetsReproducible(
                first.getParticles(),
                second.getParticles()
        );
    }

    @Test
    void generationReadsMotionSelectionFromSimulationConfig() {
        SimulationConfig config = config(3);
        MotionSelectionConfig selection = new MotionSelectionConfig(
                EnumSet.of(MotionType.NORMAL_DIFFUSION)
        );
        config.setMotionSelectionConfig(selection);

        SyntheticDataset dataset = generator.generate(
                config,
                404L
        );

        assertEquals(3, dataset.getParticles().size());
        assertEquals(
                selection.getSelectedMotionTypes(),
                dataset.getMotionTypes()
        );
    }

    private SimulationConfig config(int particleNumber) {
        SimulationConfig config = SimulationConfig.defaultConfig();
        config.width = 32;
        config.height = 32;
        config.frames = 4;
        config.particleNumber = particleNumber;
        config.psfSigma = 1.0;
        config.noiseSigma = 0.0;
        return config;
    }

    private void assertDatasetsReproducible(
            List<SyntheticParticle> first,
            List<SyntheticParticle> second
    ) {
        assertEquals(first.size(), second.size());
        for (int particleIndex = 0;
             particleIndex < first.size();
             particleIndex++) {
            SyntheticParticle firstParticle = first.get(particleIndex);
            SyntheticParticle secondParticle = second.get(particleIndex);
            assertEquals(
                    firstParticle.getMotionAssignment()
                            .getMotionProfile()
                            .getProfileId(),
                    secondParticle.getMotionAssignment()
                            .getMotionProfile()
                            .getProfileId()
            );
            assertEquals(
                    firstParticle.getMotionAssignment()
                            .getTrajectorySeed(),
                    secondParticle.getMotionAssignment()
                            .getTrajectorySeed()
            );
            assertEquals(
                    firstParticle.getTrajectory().size(),
                    secondParticle.getTrajectory().size()
            );
            for (int frame = 1;
                 frame <= firstParticle.getTrajectory().size();
                 frame++) {
                Point2D.Double firstPoint =
                        firstParticle.getTrajectory().get(frame);
                Point2D.Double secondPoint =
                        secondParticle.getTrajectory().get(frame);
                assertEquals(firstPoint.x, secondPoint.x);
                assertEquals(firstPoint.y, secondPoint.y);
                assertTrue(Double.isFinite(firstPoint.x));
                assertTrue(Double.isFinite(firstPoint.y));
            }
        }
    }
}
