package io.github.zhengfangfang0304.synspt.particle;

import io.github.zhengfangfang0304.synspt.config.MotionCompositionConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.MultiStateGenerator;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.sampling.DefaultMotionParameterGenerator;
import io.github.zhengfangfang0304.synspt.motion.parameters.sampling.MotionParameterGenerator;
import io.github.zhengfangfang0304.synspt.noise.PhotonCalibrationResult;
import io.github.zhengfangfang0304.synspt.optical.ParticleAppearancePlan;
import io.github.zhengfangfang0304.synspt.optical.ParticleAppearanceSample;
import io.github.zhengfangfang0304.synspt.optical.ParticleAppearanceParameterGenerator;
import io.github.zhengfangfang0304.synspt.optical.ParticleImagingParameters;
import io.github.zhengfangfang0304.synspt.optical.PsfSizeClass;
import io.github.zhengfangfang0304.synspt.optical.PsfSizeClassAssigner;
import io.github.zhengfangfang0304.synspt.optical.PsfSizeComposition;
import io.github.zhengfangfang0304.synspt.optical.RandomPsfSizeCompositionGenerator;
import io.github.zhengfangfang0304.synspt.util.SeedDerivation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Creates immutable particle definitions from one validated configuration. */
public final class ParticleFactory {

    private final ParticleAppearanceParameterGenerator appearanceGenerator;
    private final RandomPsfSizeCompositionGenerator sizeCompositionGenerator;
    private final PsfSizeClassAssigner sizeClassAssigner;
    private final MotionParameterGenerator motionParameterGenerator;
    private final CompositionMotionAssigner motionAssigner;
    private final MultiStateGenerator multiStateGenerator;

    public ParticleFactory() {
        this(
                new ParticleAppearanceParameterGenerator(),
                new RandomPsfSizeCompositionGenerator(),
                new PsfSizeClassAssigner(),
                new DefaultMotionParameterGenerator(),
                new CompositionMotionAssigner(),
                new MultiStateGenerator()
        );
    }

    public ParticleFactory(
            ParticleAppearanceParameterGenerator appearanceGenerator,
            RandomPsfSizeCompositionGenerator sizeCompositionGenerator,
            PsfSizeClassAssigner sizeClassAssigner,
            MotionParameterGenerator motionParameterGenerator,
            CompositionMotionAssigner motionAssigner,
            MultiStateGenerator multiStateGenerator
    ) {
        if (appearanceGenerator == null
                || sizeCompositionGenerator == null
                || sizeClassAssigner == null
                || motionParameterGenerator == null
                || motionAssigner == null
                || multiStateGenerator == null) {
            throw new IllegalArgumentException(
                    "Particle factory dependencies cannot be null."
            );
        }
        this.appearanceGenerator = appearanceGenerator;
        this.sizeCompositionGenerator = sizeCompositionGenerator;
        this.sizeClassAssigner = sizeClassAssigner;
        this.motionParameterGenerator = motionParameterGenerator;
        this.motionAssigner = motionAssigner;
        this.multiStateGenerator = multiStateGenerator;
    }

    public ParticleAppearancePlan createAppearancePlan(
            SimulationConfig config
    ) {
        if (config == null) {
            throw new IllegalArgumentException("Simulation config cannot be null.");
        }
        PsfSizeComposition sizeComposition = sizeCompositionGenerator.generate(
                config.getAppearanceDistributionConfig()
                        .getPsfSizeMixtureConfig(),
                SeedDerivation.psfSizeCompositionSeed(config.getRandomSeed())
        );
        List<PsfSizeClass> sizeAssignments = sizeClassAssigner.assign(
                sizeComposition,
                config.getParticleNumber(),
                SeedDerivation.psfSizeAssignmentSeed(config.getRandomSeed())
        );
        List<ParticleAppearanceSample> samples =
                new ArrayList<ParticleAppearanceSample>(config.getParticleNumber());
        for (int index = 0; index < config.getParticleNumber(); index++) {
            int particleId = index + 1;
            samples.add(appearanceGenerator.generate(
                    config.getSpotShape(),
                    sizeAssignments.get(index),
                    sizeComposition,
                    config.getAppearanceDistributionConfig(),
                    SeedDerivation.psfGeometrySeed(
                            config.getRandomSeed(),
                            particleId
                    ),
                    SeedDerivation.brightnessSeed(
                            config.getRandomSeed(),
                            particleId
                    )
            ));
        }
        return new ParticleAppearancePlan(sizeComposition, samples);
    }

    public List<Particle> createParticles(
            SimulationConfig config,
            MotionCompositionConfig composition,
            ParticleAppearancePlan appearancePlan,
            PhotonCalibrationResult calibration
    ) {
        if (config == null || composition == null
                || appearancePlan == null || calibration == null) {
            throw new IllegalArgumentException(
                    "Particle creation inputs cannot be null."
            );
        }
        List<ParticleAppearanceSample> samples = appearancePlan.getSamples();
        if (samples.size() != config.getParticleNumber()
                || calibration.getParticlePhotonBudgets().size()
                != config.getParticleNumber()) {
            throw new IllegalArgumentException(
                    "Appearance and calibration counts must match particle count."
            );
        }
        if (!composition.getComposition().keySet().equals(
                config.getSelectedMotionTypes())) {
            throw new IllegalArgumentException(
                    "Composition must contain exactly the selected motion types."
            );
        }
        List<MotionType> assignments = motionAssigner.assign(
                composition,
                config.getParticleNumber(),
                SeedDerivation.motionAssignmentSeed(config.getRandomSeed())
        );
        List<Particle> particles = new ArrayList<Particle>(
                config.getParticleNumber()
        );

        for (int index = 0; index < config.getParticleNumber(); index++) {
            int particleId = index + 1;
            MotionType assignedType = assignments.get(index);
            List<StateSegment> stateSegments;
            if (assignedType.isComposite()) {
                stateSegments = multiStateGenerator.generate(config, particleId);
            } else {
                MotionParameters motionParameters = motionParameterGenerator.generate(
                        assignedType,
                        config.getMotionParameterDistributionConfig(),
                        config.getMicroscopeConfig().getFrameIntervalSeconds(),
                        SeedDerivation.motionParameterSeed(
                                config.getRandomSeed(),
                                particleId
                        )
                );
                stateSegments = Collections.singletonList(new StateSegment(
                        particleId,
                        0,
                        0,
                        config.getFrames() - 1,
                        assignedType,
                        motionParameters
                ));
            }
            ParticleImagingParameters imagingParameters =
                    new ParticleImagingParameters(
                            samples.get(index).getPsfParameters(),
                            calibration.getParticlePhotonBudget(index)
                    );
            particles.add(new Particle(
                    particleId,
                    assignedType,
                    stateSegments,
                    imagingParameters
            ));
        }
        return Collections.unmodifiableList(particles);
    }
}
