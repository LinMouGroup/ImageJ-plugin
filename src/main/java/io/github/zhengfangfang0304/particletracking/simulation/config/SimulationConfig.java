package io.github.zhengfangfang0304.particletracking.simulation.config;

import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;

import java.util.EnumSet;

/**
 * Parameters shared by every synthetic microscopy experiment.
 *
 * <p>Motion-specific values are held by their dedicated configuration
 * objects and are not duplicated here.</p>
 */
public class SimulationConfig {

    public int width = 256;

    public int height = 256;

    public int frames = 100;

    public int particleNumber = 20;

    public double psfSigma = 2.0;

    public double noiseSigma = 8.0;

    private ImagingConfig imagingConfig = new ImagingConfig();

    private MotionSelectionConfig motionSelectionConfig =
            new MotionSelectionConfig(
                    EnumSet.of(MotionType.NORMAL_DIFFUSION)
            );

    public static SimulationConfig defaultConfig() {
        return new SimulationConfig();
    }

    public ImagingConfig getImagingConfig() {
        return imagingConfig;
    }

    public void setImagingConfig(ImagingConfig imagingConfig) {
        if (imagingConfig == null) {
            throw new IllegalArgumentException(
                    "Imaging config cannot be null."
            );
        }
        this.imagingConfig = imagingConfig;
    }

    public MotionSelectionConfig getMotionSelectionConfig() {
        return motionSelectionConfig;
    }

    public void setMotionSelectionConfig(
            MotionSelectionConfig motionSelectionConfig
    ) {
        if (motionSelectionConfig == null) {
            throw new IllegalArgumentException(
                    "Motion selection config cannot be null."
            );
        }
        this.motionSelectionConfig = motionSelectionConfig;
    }

    /**
     * Validates global experiment and motion-selection parameters.
     */
    public void validate() {
        validateCommonParameters();
        if (motionSelectionConfig == null) {
            throw new IllegalArgumentException(
                    "Motion selection config cannot be null."
            );
        }
        motionSelectionConfig.validate();
        if (particleNumber
                < motionSelectionConfig
                .getSelectedMotionTypes()
                .size()) {
            throw new IllegalArgumentException(
                    "Particle number must be at least the number "
                            + "of selected motion types."
            );
        }
    }

    private void validateCommonParameters() {
        if (width <= 0) {
            throw new IllegalArgumentException(
                    "Width must be greater than zero."
            );
        }
        if (height <= 0) {
            throw new IllegalArgumentException(
                    "Height must be greater than zero."
            );
        }
        if (frames <= 0) {
            throw new IllegalArgumentException(
                    "Frames must be greater than zero."
            );
        }
        if (particleNumber <= 0) {
            throw new IllegalArgumentException(
                    "Particle number must be greater than zero."
            );
        }
        if (psfSigma <= 0.0) {
            throw new IllegalArgumentException(
                    "PSF sigma must be greater than zero."
            );
        }
        if (noiseSigma < 0.0) {
            throw new IllegalArgumentException(
                    "Noise sigma cannot be negative."
            );
        }
        if (imagingConfig == null) {
            throw new IllegalArgumentException(
                    "Imaging config cannot be null."
            );
        }
    }
}
