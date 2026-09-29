package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;
import io.github.zhengfangfang0304.synspt.particle.Trajectory;

/**
 * Contract implemented by every stochastic motion model.
 */
public interface MotionModel {

    MotionType getType();

    MotionParameters getParameters();

    /**
     * Generates a physical displacement trajectory in micrometres.
     */
    Trajectory generate(MotionContext context);
}
