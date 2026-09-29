package io.github.zhengfangfang0304.synspt.config;

/**
 * Central validation and memory preflight for simulation requests.
 */
public final class SimulationConfigValidator {

    public static final int MAX_IMAGE_DIMENSION = 4096;
    public static final int MAX_FRAMES = 10000;
    public static final int MAX_PARTICLES = 100000;

    private static final long BYTES_PER_OUTPUT_PIXEL = 2L;
    private static final long BYTES_PER_EXPECTED_PHOTON_PIXEL = 8L;
    private static final long BYTES_PER_TRAJECTORY_POINT = 16L;

    private SimulationConfigValidator() {
    }

    public static void validate(SimulationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Simulation config cannot be null.");
        }
        requireRange(config.getImageWidth(), 1, MAX_IMAGE_DIMENSION, "Image width");
        requireRange(config.getImageHeight(), 1, MAX_IMAGE_DIMENSION, "Image height");
        requireRange(config.getFrames(), 1, MAX_FRAMES, "Frames");
        requireRange(config.getParticleNumber(), 1, MAX_PARTICLES, "Particle number");

        int selectedModelCount = config.getSelectedMotionTypes().size();
        if (selectedModelCount == 0) {
            throw new IllegalArgumentException("At least one motion model must be selected.");
        }
        if (config.getParticleNumber() < selectedModelCount) {
            throw new IllegalArgumentException(
                    "Particle number must be at least the number of selected motion models."
            );
        }
        if (config.getSelectedMotionTypes().contains(
                io.github.zhengfangfang0304.synspt.motion.MotionType.MULTI_STATE
        ) && config.getFrames() < 2) {
            throw new IllegalArgumentException(
                    "Multi-state motion requires at least two frames."
            );
        }
        if (config.getMicroscopeConfig() == null) {
            throw new IllegalArgumentException("Microscope config cannot be null.");
        }
        if (config.getNoiseConfig() == null) {
            throw new IllegalArgumentException("Noise config cannot be null.");
        }
        if (config.getSpotShape() == null) {
            throw new IllegalArgumentException("Spot shape cannot be null.");
        }
        if (config.getAppearanceDistributionConfig() == null) {
            throw new IllegalArgumentException(
                    "Appearance distribution config cannot be null."
            );
        }
        if (config.getMotionParameterDistributionConfig() == null) {
            throw new IllegalArgumentException(
                    "Motion parameter distribution config cannot be null."
            );
        }
        if (config.getRandomCompositionConfig() == null) {
            throw new IllegalArgumentException(
                    "Random composition config cannot be null."
            );
        }

        validatePhysicalFieldOfView(config);
        validateEstimatedMemory(config);
    }

    private static void validatePhysicalFieldOfView(SimulationConfig config) {
        double pixelSizeUm = config.getMicroscopeConfig().getPixelSizeUm();
        double widthUm = config.getImageWidth() * pixelSizeUm;
        double heightUm = config.getImageHeight() * pixelSizeUm;
        if (Double.isNaN(widthUm)
                || Double.isInfinite(widthUm)
                || Double.isNaN(heightUm)
                || Double.isInfinite(heightUm)) {
            throw new IllegalArgumentException(
                    "Physical field-of-view dimensions must be finite."
            );
        }
    }

    private static void validateEstimatedMemory(SimulationConfig config) {
        long pixelsPerFrame = (long) config.getImageWidth() * config.getImageHeight();
        long imageBytes = pixelsPerFrame * config.getFrames() * BYTES_PER_OUTPUT_PIXEL;
        long workFrameBytes = pixelsPerFrame * BYTES_PER_EXPECTED_PHOTON_PIXEL;
        long trajectoryBytes = (long) config.getParticleNumber()
                * config.getFrames() * BYTES_PER_TRAJECTORY_POINT;
        long estimatedBytes = imageBytes + workFrameBytes + trajectoryBytes;
        long safeHeapBudget = Runtime.getRuntime().maxMemory() * 7L / 10L;

        if (estimatedBytes > safeHeapBudget) {
            throw new IllegalArgumentException(
                    "Requested simulation is estimated to require "
                            + estimatedBytes + " bytes, exceeding the safe heap budget of "
                            + safeHeapBudget + " bytes."
            );
        }
    }

    private static void requireRange(int value, int minimum, int maximum, String name) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be between " + minimum + " and " + maximum + "."
            );
        }
    }
}
