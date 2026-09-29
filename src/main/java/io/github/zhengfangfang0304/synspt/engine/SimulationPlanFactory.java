package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.MotionCompositionConfig;
import io.github.zhengfangfang0304.synspt.config.MotionCompositionGenerator;
import io.github.zhengfangfang0304.synspt.config.RandomMotionCompositionGenerator;
import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfigValidator;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.particle.ParticleFactory;
import io.github.zhengfangfang0304.synspt.noise.PhotonCalibrationResult;
import io.github.zhengfangfang0304.synspt.noise.SnrNoiseCalibrator;
import io.github.zhengfangfang0304.synspt.optical.ParticleAppearancePlan;
import io.github.zhengfangfang0304.synspt.util.SeedDerivation;

import java.util.List;

/** Builds the complete realized plan before trajectory generation starts. */
public final class SimulationPlanFactory {

    private final MotionCompositionGenerator motionCompositionGenerator;
    private final ParticleFactory particleFactory;
    private final SnrNoiseCalibrator snrNoiseCalibrator;

    public SimulationPlanFactory() {
        this(
                new RandomMotionCompositionGenerator(),
                new ParticleFactory(),
                new SnrNoiseCalibrator()
        );
    }

    public SimulationPlanFactory(
            MotionCompositionGenerator motionCompositionGenerator,
            ParticleFactory particleFactory,
            SnrNoiseCalibrator snrNoiseCalibrator
    ) {
        if (motionCompositionGenerator == null
                || particleFactory == null
                || snrNoiseCalibrator == null) {
            throw new IllegalArgumentException(
                    "Plan factory dependencies cannot be null."
            );
        }
        this.motionCompositionGenerator = motionCompositionGenerator;
        this.particleFactory = particleFactory;
        this.snrNoiseCalibrator = snrNoiseCalibrator;
    }

    public SimulationPlan createPlan(SimulationConfig requestConfig) {
        SimulationConfigValidator.validate(requestConfig);
        MotionCompositionConfig composition =
                motionCompositionGenerator.generate(
                        requestConfig.getSelectedMotionTypes(),
                        requestConfig.getRandomCompositionConfig(),
                        SeedDerivation.motionCompositionSeed(
                                requestConfig.getRandomSeed()
                        )
                );
        ParticleAppearancePlan appearancePlan =
                particleFactory.createAppearancePlan(requestConfig);
        PhotonCalibrationResult photonCalibration = snrNoiseCalibrator.calibrate(
                requestConfig.getNoiseConfig(),
                requestConfig.getMicroscopeConfig(),
                appearancePlan.getSamples()
        );
        List<Particle> particles = particleFactory.createParticles(
                requestConfig,
                composition,
                appearancePlan,
                photonCalibration
        );
        return new SimulationPlan(
                requestConfig,
                composition,
                appearancePlan,
                particles,
                photonCalibration
        );
    }

}
