package io.github.zhengfangfang0304.particletracking.simulation.generators;

import ij.ImagePlus;
import io.github.zhengfangfang0304.particletracking.simulation.config.MotionSelectionConfig;
import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticDataset;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticParticle;
import io.github.zhengfangfang0304.particletracking.simulation.motion.ParticleMotionAssignment;
import io.github.zhengfangfang0304.particletracking.simulation.rendering.GaussianSpotRenderer;

import java.util.List;
import java.util.Random;

/**
 * Coordinates particle initialization, trajectory generation, and rendering.
 */
public class SyntheticDatasetGenerator {

    private final ParticleInitializer particleInitializer;

    private final TrajectoryGenerator trajectoryGenerator;

    private final GaussianSpotRenderer renderer;

    private final ParticleMotionAssigner particleMotionAssigner;

    public SyntheticDatasetGenerator() {
        this(
                new ParticleInitializer(),
                new TrajectoryGenerator(),
                new GaussianSpotRenderer(),
                new ParticleMotionAssigner()
        );
    }

    public SyntheticDatasetGenerator(
            ParticleInitializer particleInitializer,
            TrajectoryGenerator trajectoryGenerator,
            GaussianSpotRenderer renderer
    ) {
        this(
                particleInitializer,
                trajectoryGenerator,
                renderer,
                new ParticleMotionAssigner()
        );
    }

    public SyntheticDatasetGenerator(
            ParticleInitializer particleInitializer,
            TrajectoryGenerator trajectoryGenerator,
            GaussianSpotRenderer renderer,
            ParticleMotionAssigner particleMotionAssigner
    ) {
        if (particleInitializer == null) {
            throw new IllegalArgumentException(
                    "Particle initializer cannot be null."
            );
        }
        if (trajectoryGenerator == null) {
            throw new IllegalArgumentException(
                    "Trajectory generator cannot be null."
            );
        }
        if (renderer == null) {
            throw new IllegalArgumentException(
                    "Renderer cannot be null."
            );
        }
        if (particleMotionAssigner == null) {
            throw new IllegalArgumentException(
                    "Particle motion assigner cannot be null."
            );
        }

        this.particleInitializer = particleInitializer;
        this.trajectoryGenerator = trajectoryGenerator;
        this.renderer = renderer;
        this.particleMotionAssigner = particleMotionAssigner;
    }

    /**
     * Generates a dataset with reproducible particle-level motion profiles.
     *
     * <p>The resolved assignments are attached before trajectories are
     * generated through the profile-driven unified FBM path.</p>
     *
     * @param config common simulation and imaging parameters
     * @param masterSeed seed for particle initialization and assignments
     * @return generated dataset with one assignment on every particle
     */
    public SyntheticDataset generate(
            SimulationConfig config,
            long masterSeed
    ) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Simulation config cannot be null."
            );
        }
        config.validate();
        MotionSelectionConfig motionSelectionConfig =
                config.getMotionSelectionConfig();

        Random random = new Random(masterSeed);
        List<SyntheticParticle> particles =
                particleInitializer.initializeParticles(
                        config,
                        random
                );
        List<ParticleMotionAssignment> assignments =
                particleMotionAssigner.assign(
                        config.particleNumber,
                        motionSelectionConfig,
                        masterSeed
                );

        for (int particleIndex = 0;
             particleIndex < particles.size();
             particleIndex++) {
            SyntheticParticle particle = particles.get(particleIndex);
            particle.setMotionAssignment(
                    assignments.get(particleIndex)
            );
            trajectoryGenerator.populateTrajectory(
                    particle,
                    config,
                    assignments.get(particleIndex)
            );
        }

        ImagePlus image = renderer.render(particles, config);
        return new SyntheticDataset(
                particles,
                image,
                config.getImagingConfig()
        );
    }
}
