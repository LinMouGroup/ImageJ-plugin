package io.github.zhengfangfang0304.synspt.particle;

import io.github.zhengfangfang0304.synspt.config.MotionCompositionConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;
import io.github.zhengfangfang0304.synspt.noise.PhotonCalibrationResult;
import io.github.zhengfangfang0304.synspt.noise.SnrNoiseCalibrator;
import io.github.zhengfangfang0304.synspt.optical.ParticleAppearancePlan;
import io.github.zhengfangfang0304.synspt.optical.ParticleAppearanceSample;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;

import org.junit.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ParticleFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCompositionThatDoesNotMatchSelectedModels() {
        SimulationConfig config = SimulationConfig.builder()
                .particleNumber(2)
                .selectedMotionTypes(EnumSet.of(
                        MotionType.BROWNIAN,
                        MotionType.FBM
                ))
                .build();
        particles(
                new ParticleFactory(),
                config,
                composition(EnumSet.of(MotionType.BROWNIAN))
        );
    }

    @Test
    public void createsStableSequentialParticlesAndMotionParameters() {
        SimulationConfig config = SimulationConfig.builder()
                .particleNumber(16)
                .selectedMotionTypes(EnumSet.allOf(MotionType.class))
                .randomSeed(4455L)
                .build();
        ParticleFactory factory = new ParticleFactory();
        MotionCompositionConfig composition = composition(
                EnumSet.allOf(MotionType.class)
        );
        List<Particle> first = particles(factory, config, composition);
        List<Particle> second = particles(factory, config, composition);

        assertEquals(16, first.size());
        for (int index = 0; index < first.size(); index++) {
            Particle left = first.get(index);
            Particle right = second.get(index);
            assertEquals(index + 1, left.getParticleId());
            assertEquals(left.getMotionType(), right.getMotionType());
            assertStatePlansEqual(left.getStateSegments(), right.getStateSegments());
            assertEquals(
                    left.getImagingParameters().getPsfParameters().getShapeType(),
                    right.getImagingParameters().getPsfParameters().getShapeType()
            );
            assertEquals(
                    left.getImagingParameters().getPsfParameters().getSizeClass(),
                    right.getImagingParameters().getPsfParameters().getSizeClass()
            );
            assertEquals(
                    left.getImagingParameters().getEmitterPhotonsPerFrame(),
                    right.getImagingParameters().getEmitterPhotonsPerFrame(),
                    0.0
            );
        }
    }

    @Test
    public void assignsRequestedShapeToEveryParticle() {
        assertEveryParticleUses(PsfShapeType.CIRCULAR_GAUSSIAN);
        assertEveryParticleUses(PsfShapeType.ELLIPTICAL_GAUSSIAN);
    }

    private void assertEveryParticleUses(PsfShapeType shapeType) {
        SimulationConfig config = SimulationConfig.builder()
                .particleNumber(24)
                .selectedMotionTypes(EnumSet.of(MotionType.BROWNIAN))
                .spotShape(shapeType)
                .randomSeed(6161L)
                .build();
        List<Particle> generated = particles(
                new ParticleFactory(),
                config,
                composition(EnumSet.of(MotionType.BROWNIAN))
        );
        for (Particle particle : generated) {
            assertEquals(shapeType, particle.getImagingParameters()
                    .getPsfParameters().getShapeType());
        }
    }

    private List<Particle> particles(
            ParticleFactory factory,
            SimulationConfig config,
            MotionCompositionConfig composition
    ) {
        ParticleAppearancePlan appearancePlan =
                factory.createAppearancePlan(config);
        List<ParticleAppearanceSample> samples = appearancePlan.getSamples();
        PhotonCalibrationResult calibration = new SnrNoiseCalibrator().calibrate(
                config.getNoiseConfig(),
                config.getMicroscopeConfig(),
                samples
        );
        return factory.createParticles(
                config,
                composition,
                appearancePlan,
                calibration
        );
    }

    private MotionCompositionConfig composition(EnumSet<MotionType> types) {
        Map<MotionType, Double> ratios =
                new EnumMap<MotionType, Double>(MotionType.class);
        double ratio = 1.0 / types.size();
        for (MotionType type : types) {
            ratios.put(type, ratio);
        }
        return new MotionCompositionConfig(ratios);
    }

    private void assertStatePlansEqual(
            List<StateSegment> left,
            List<StateSegment> right
    ) {
        assertEquals(left.size(), right.size());
        for (int index = 0; index < left.size(); index++) {
            StateSegment leftState = left.get(index);
            StateSegment rightState = right.get(index);
            assertEquals(leftState.getStartFrame(), rightState.getStartFrame());
            assertEquals(leftState.getEndFrame(), rightState.getEndFrame());
            assertEquals(leftState.getMotionType(), rightState.getMotionType());
            assertEquals(leftState.getMotionParameters().toMetadata(),
                    rightState.getMotionParameters().toMetadata());
        }
    }
}
