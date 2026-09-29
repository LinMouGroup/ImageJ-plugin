package io.github.zhengfangfang0304.particletracking.simulation.physics;

import io.github.zhengfangfang0304.particletracking.simulation.config.ImagingConfig;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionProfile;

/**
 * Converts dimensionless model trajectories into physical and image scales.
 *
 * <p>The scaler always returns new arrays. Mathematical trajectories remain
 * unchanged and can therefore still be consumed independently.</p>
 */
public class PhysicalScaler {

    /**
     * Applies FBM physical scaling directly from a resolved motion profile.
     *
     * @param dimensionlessTrajectory standard dimensionless FBM path
     * @param motionProfile resolved Hurst and generalized diffusion values
     * @param imagingConfig acquisition time calibration
     * @return stochastic trajectory expressed in micrometres
     */
    public double[] scaleFractionalBrownian(
            double[] dimensionlessTrajectory,
            MotionProfile motionProfile,
            ImagingConfig imagingConfig
    ) {
        validateTrajectory(dimensionlessTrajectory);
        if (motionProfile == null) {
            throw new IllegalArgumentException(
                    "Motion profile cannot be null."
            );
        }
        validateImagingConfig(imagingConfig);

        double scale = Math.sqrt(
                2.0
                        * motionProfile
                        .getGeneralizedDiffusionCoefficient()
        ) * Math.pow(
                imagingConfig.getFrameIntervalSeconds(),
                motionProfile.getHurstExponent()
        );
        return scaledCopy(dimensionlessTrajectory, scale);
    }

    /**
     * Converts a physical trajectory in micrometres to pixel displacement.
     */
    public double[] toPixels(
            double[] physicalTrajectoryUm,
            ImagingConfig imagingConfig
    ) {
        validateTrajectory(physicalTrajectoryUm);
        validateImagingConfig(imagingConfig);

        return scaledCopy(
                physicalTrajectoryUm,
                1.0 / imagingConfig.getPixelSizeUmPerPixel()
        );
    }

    //新建一个数组，长度和source一样，元素是source的元素乘以scale
    private double[] scaledCopy(
            double[] source,
            double scale
    ) {
        double[] result = new double[source.length];

        for (int i = 0; i < source.length; i++) {
            result[i] = source[i] * scale;
        }

        return result;
    }

    //检验数据是否合法
    private void validateTrajectory(double[] trajectory) {
        if (trajectory == null) {
            throw new IllegalArgumentException(
                    "Trajectory cannot be null."
            );
        }

        for (double value : trajectory) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException(
                        "Trajectory values must be finite."
                );
            }
        }
    }

    private void validateImagingConfig(
            ImagingConfig imagingConfig
    ) {
        if (imagingConfig == null) {
            throw new IllegalArgumentException(
                    "Imaging config cannot be null."
            );
        }
    }
}
