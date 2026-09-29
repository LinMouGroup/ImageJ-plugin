package io.github.zhengfangfang0304.particletracking.simulation.generators;

import io.github.zhengfangfang0304.particletracking.simulation.models.fbm.FractionalBrownianMotion;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfile;

import java.util.Random;

/**
 * Generates dimensionless FBM paths from resolved motion profiles.
 *
 * <p>Every semantic motion type reaches this same implementation. The Hurst
 * exponent is read from the profile; this generator never branches on motion
 * type. K_H remains in the physical scaling layer. Constant drift remains a
 * physical velocity and is therefore applied after stochastic physical
 * scaling by the trajectory orchestration layer.</p>
 */
public final class FBMTrajectoryGenerator {

    /**
     * Generates one dimensionless one-dimensional FBM trajectory.
     *
     * @param numberOfIncrements number of trajectory increments
     * @param motionProfile resolved FBM parameters
     * @param random random source dedicated to the particle trajectory
     * @return {@code numberOfIncrements + 1} samples beginning at zero
     */
    public double[] generate(
            int numberOfIncrements,
            MotionProfile motionProfile,
            Random random
    ) {
        if (numberOfIncrements < 0) {
            throw new IllegalArgumentException(
                    "Number of increments cannot be negative."
            );
        }
        if (motionProfile == null) {
            throw new IllegalArgumentException(
                    "Motion profile cannot be null."
            );
        }
        if (random == null) {
            throw new IllegalArgumentException(
                    "Random generator cannot be null."
            );
        }

        return new FractionalBrownianMotion(
                motionProfile.getHurstExponent(),
                random
        ).generate(numberOfIncrements);
    }
}
