package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.particle.Particle;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;

import org.junit.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SimulationPlanFactoryTest {

    @Test
    public void realizesCompositionAssignmentsAndParametersBeforeExecution() {
        SimulationConfig config = SimulationConfig.builder()
                .particleNumber(100)
                .selectedMotionTypes(EnumSet.of(
                        MotionType.BROWNIAN,
                        MotionType.FBM,
                        MotionType.CTRW
                ))
                .randomSeed(1234L)
                .build();

        SimulationPlan plan = new SimulationPlanFactory().createPlan(config);
        EnumSet<MotionType> realized = EnumSet.noneOf(MotionType.class);
        assertSame(config, plan.getRequestConfig());
        assertEquals(100, plan.getParticles().size());
        assertEquals(3, plan.getMotionComposition().getComposition().size());
        assertEquals(3, plan.getPsfSizeComposition().getRatios().size());
        for (int particleIndex = 0;
                particleIndex < plan.getParticles().size(); particleIndex++) {
            Particle particle = plan.getParticles().get(particleIndex);
            realized.add(particle.getMotionType());
            assertTrue(config.getSelectedMotionTypes().contains(
                    particle.getMotionType()
            ));
            assertEquals(
                    plan.getPhotonCalibration().getParticlePhotonBudget(
                            particleIndex
                    ),
                    particle.getImagingParameters().getEmitterPhotonsPerFrame(),
                    0.0
            );
        }
        assertEquals(config.getSelectedMotionTypes(), realized);
    }

    @Test
    public void fixedRootSeedProducesIdenticalRealizedPlan() {
        SimulationConfig config = SimulationConfig.builder()
                .particleNumber(16)
                .selectedMotionTypes(EnumSet.allOf(MotionType.class))
                .randomSeed(4455L)
                .build();
        SimulationPlanFactory factory = new SimulationPlanFactory();
        SimulationPlan first = factory.createPlan(config);
        SimulationPlan second = factory.createPlan(config);

        assertEquals(first.getMotionComposition().getComposition(),
                second.getMotionComposition().getComposition());
        assertEquals(first.getPsfSizeComposition().getRatios(),
                second.getPsfSizeComposition().getRatios());
        assertEquals(
                first.getPhotonCalibration().getPhotonScale(),
                second.getPhotonCalibration().getPhotonScale(),
                0.0
        );
        assertEquals(
                first.getPhotonCalibration().getParticlePhotonBudgets(),
                second.getPhotonCalibration().getParticlePhotonBudgets()
        );
        for (int particleIndex = 0;
                particleIndex < first.getParticles().size(); particleIndex++) {
            Particle left = first.getParticles().get(particleIndex);
            Particle right = second.getParticles().get(particleIndex);
            assertEquals(left.getMotionType(), right.getMotionType());
            assertEquals(
                    left.getImagingParameters().getPsfParameters().getSizeClass(),
                    right.getImagingParameters().getPsfParameters().getSizeClass()
            );
            assertSegmentsEqual(left.getStateSegments(), right.getStateSegments());
        }
    }

    @Test
    public void differentRootSeedsProduceDifferentPsfSizeCompositions() {
        SimulationConfig firstConfig = SimulationConfig.builder()
                .particleNumber(100)
                .selectedMotionTypes(EnumSet.of(MotionType.BROWNIAN))
                .randomSeed(111L)
                .build();
        SimulationConfig secondConfig = SimulationConfig.builder()
                .particleNumber(100)
                .selectedMotionTypes(EnumSet.of(MotionType.BROWNIAN))
                .randomSeed(222L)
                .build();
        SimulationPlanFactory factory = new SimulationPlanFactory();
        assertTrue(!factory.createPlan(firstConfig).getPsfSizeComposition()
                .getRatios().equals(factory.createPlan(secondConfig)
                        .getPsfSizeComposition().getRatios()));
    }

    private void assertSegmentsEqual(
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
