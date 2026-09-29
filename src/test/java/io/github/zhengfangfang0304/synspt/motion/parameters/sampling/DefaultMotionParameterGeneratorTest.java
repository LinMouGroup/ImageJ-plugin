package io.github.zhengfangfang0304.synspt.motion.parameters.sampling;

import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.parameters.AttmParameters;
import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultMotionParameterGeneratorTest {

    @Test
    public void everyModelHasAReproducibleDedicatedSampler() {
        DefaultMotionParameterGenerator generator =
                new DefaultMotionParameterGenerator();
        MotionParameterDistributionConfig config =
                MotionParameterDistributionConfig.defaultConfig();
        for (MotionType type : MotionType.baseTypes()) {
            MotionParameters first = generator.generate(
                    type, config, 1.0 / 30.0, 1234L + type.ordinal()
            );
            MotionParameters repeated = generator.generate(
                    type, config, 1.0 / 30.0, 1234L + type.ordinal()
            );
            MotionParameters different = generator.generate(
                    type, config, 1.0 / 30.0,
                    (1234L + type.ordinal()) * 0x9E3779B97F4A7C15L
            );
            assertEquals(type, first.getMotionType());
            assertEquals(first.toMetadata(), repeated.toMetadata());
            assertFalse(first.toMetadata().equals(different.toMetadata()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCompositeMotionBecauseItHasNoEquationParameters() {
        new DefaultMotionParameterGenerator().generate(
                MotionType.MULTI_STATE,
                MotionParameterDistributionConfig.defaultConfig(),
                1.0 / 30.0,
                12L
        );
    }

    @Test
    public void attmSamplerAlwaysSatisfiesRegimeOneConstraints() {
        DefaultMotionParameterGenerator generator =
                new DefaultMotionParameterGenerator();
        for (long index = 1L; index <= 200L; index++) {
            AttmParameters parameters = (AttmParameters) generator.generate(
                    MotionType.ATTM,
                    MotionParameterDistributionConfig.defaultConfig(),
                    0.02,
                    index * 0x9E3779B97F4A7C15L
            );
            assertTrue(parameters.getDiffusivityExponentSigma()
                    < parameters.getDwellExponentGamma());
            assertTrue(parameters.getDwellExponentGamma()
                    < parameters.getDiffusivityExponentSigma() + 1.0);
            assertEquals(
                    parameters.getAlpha(),
                    parameters.getDiffusivityExponentSigma()
                            / parameters.getDwellExponentGamma(),
                    1.0e-12
            );
        }
    }
}
