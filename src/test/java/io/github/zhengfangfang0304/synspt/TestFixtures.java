package io.github.zhengfangfang0304.synspt;

import io.github.zhengfangfang0304.synspt.motion.parameters.BrownianParameters;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;
import io.github.zhengfangfang0304.synspt.optical.ParticleImagingParameters;
import io.github.zhengfangfang0304.synspt.optical.ParticlePsfParameters;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;
import io.github.zhengfangfang0304.synspt.optical.PsfSizeClass;
import io.github.zhengfangfang0304.synspt.particle.Particle;

import java.util.Collections;

/** Shared immutable test fixtures. */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static Particle particle(
            int particleId,
            int frameCount,
            double diffusion
    ) {
        BrownianParameters parameters = new BrownianParameters(diffusion);
        return new Particle(
                particleId,
                MotionType.BROWNIAN,
                Collections.singletonList(new StateSegment(
                        particleId,
                        0,
                        0,
                        frameCount - 1,
                        MotionType.BROWNIAN,
                        parameters
                )),
                imaging()
        );
    }

    public static ParticleImagingParameters imaging() {
        return new ParticleImagingParameters(
                new ParticlePsfParameters(
                        PsfSizeClass.MEDIUM,
                        PsfShapeType.CIRCULAR_GAUSSIAN,
                        7.5,
                        1.5,
                        1.5,
                        0.0
                ),
                1000.0
        );
    }
}
