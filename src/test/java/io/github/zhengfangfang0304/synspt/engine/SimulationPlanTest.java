package io.github.zhengfangfang0304.synspt.engine;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

public class SimulationPlanTest {

    @Test
    public void retainsOriginalAppearanceSamplesForMetadata() {
        SimulationConfig config = SimulationConfig.defaultConfig();
        SimulationPlan plan = new SimulationPlanFactory().createPlan(config);
        io.github.zhengfangfang0304.synspt.optical.ParticleAppearancePlan expected =
                new io.github.zhengfangfang0304.synspt.particle.ParticleFactory().createAppearancePlan(config);
        assertEquals(plan.getParticles().size(), plan.getAppearancePlan().getSamples().size());
        for (int i = 0; i < plan.getParticles().size(); i++) {
            assertEquals(expected.getSamples().get(i).getBrightnessWeight(),
                    plan.getAppearancePlan().getSamples().get(i).getBrightnessWeight(), 0.0);
            assertSame(plan.getParticles().get(i).getImagingParameters().getPsfParameters(),
                    plan.getAppearancePlan().getSamples().get(i).getPsfParameters());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void exposesImmutableAppearanceSamples() {
        new SimulationPlanFactory().createPlan(SimulationConfig.defaultConfig())
                .getAppearancePlan().getSamples().clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void exposesImmutableRealizedParticleList() {
        SimulationConfig config = SimulationConfig.defaultConfig();
        SimulationPlan plan = new SimulationPlanFactory().createPlan(config);
        plan.getParticles().clear();
    }

    @Test
    public void keepsTheOriginalValidatedRequest() {
        SimulationConfig config = SimulationConfig.defaultConfig();
        SimulationPlan plan = new SimulationPlanFactory().createPlan(config);
        assertSame(config, plan.getRequestConfig());
    }
}
