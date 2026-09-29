package io.github.zhengfangfang0304.synspt.config;

import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Immutable input to one synthetic-data generation run.
 */
public final class SimulationConfig {

    public static final int DEFAULT_IMAGE_WIDTH = 256;
    public static final int DEFAULT_IMAGE_HEIGHT = 256;
    public static final int DEFAULT_FRAMES = 100;
    public static final int DEFAULT_PARTICLE_NUMBER = 100;
    public static final PsfShapeType DEFAULT_SPOT_SHAPE =
            PsfShapeType.CIRCULAR_GAUSSIAN;

    private final int imageWidth;
    private final int imageHeight;
    private final int frames;
    private final int particleNumber;
    private final EnumSet<MotionType> selectedMotionTypes;
    private final long randomSeed;
    private final MicroscopeConfig microscopeConfig;
    private final NoiseConfig noiseConfig;
    private final PsfShapeType spotShape;
    private final ParticleAppearanceDistributionConfig appearanceDistributionConfig;
    private final MotionParameterDistributionConfig motionParameterDistributionConfig;
    private final RandomCompositionConfig randomCompositionConfig;

    private SimulationConfig(Builder builder) {
        this.imageWidth = builder.imageWidth;
        this.imageHeight = builder.imageHeight;
        this.frames = builder.frames;
        this.particleNumber = builder.particleNumber;
        this.selectedMotionTypes = copyOf(builder.selectedMotionTypes);
        this.randomSeed = builder.randomSeed;
        this.microscopeConfig = builder.microscopeConfig;
        this.noiseConfig = builder.noiseConfig;
        this.spotShape = builder.spotShape;
        this.appearanceDistributionConfig = builder.appearanceDistributionConfig;
        this.motionParameterDistributionConfig =
                builder.motionParameterDistributionConfig;
        this.randomCompositionConfig = builder.randomCompositionConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SimulationConfig defaultConfig() {
        return builder().build();
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getFrames() {
        return frames;
    }

    public int getParticleNumber() {
        return particleNumber;
    }

    public EnumSet<MotionType> getSelectedMotionTypes() {
        return copyOf(selectedMotionTypes);
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public MicroscopeConfig getMicroscopeConfig() {
        return microscopeConfig;
    }

    public NoiseConfig getNoiseConfig() {
        return noiseConfig;
    }

    public PsfShapeType getSpotShape() {
        return spotShape;
    }

    public ParticleAppearanceDistributionConfig getAppearanceDistributionConfig() {
        return appearanceDistributionConfig;
    }

    public MotionParameterDistributionConfig getMotionParameterDistributionConfig() {
        return motionParameterDistributionConfig;
    }

    public RandomCompositionConfig getRandomCompositionConfig() {
        return randomCompositionConfig;
    }

    private static EnumSet<MotionType> copyOf(Set<MotionType> source) {
        if (source.isEmpty()) {
            return EnumSet.noneOf(MotionType.class);
        }
        return EnumSet.copyOf(source);
    }

    /**
     * Builder used by the GUI controller and integration tests.
     */
    public static final class Builder {

        private int imageWidth = DEFAULT_IMAGE_WIDTH;
        private int imageHeight = DEFAULT_IMAGE_HEIGHT;
        private int frames = DEFAULT_FRAMES;
        private int particleNumber = DEFAULT_PARTICLE_NUMBER;
        private EnumSet<MotionType> selectedMotionTypes =
                EnumSet.of(MotionType.BROWNIAN);
        private long randomSeed;
        private MicroscopeConfig microscopeConfig =
                MicroscopeConfig.defaultConfig();
        private NoiseConfig noiseConfig = NoiseConfig.defaultConfig();
        private PsfShapeType spotShape = DEFAULT_SPOT_SHAPE;
        private ParticleAppearanceDistributionConfig appearanceDistributionConfig =
                ParticleAppearanceDistributionConfig.defaultConfig();
        private MotionParameterDistributionConfig motionParameterDistributionConfig =
                MotionParameterDistributionConfig.defaultConfig();
        private RandomCompositionConfig randomCompositionConfig =
                RandomCompositionConfig.defaultConfig();

        public Builder imageWidth(int value) {
            this.imageWidth = value;
            return this;
        }

        public Builder imageHeight(int value) {
            this.imageHeight = value;
            return this;
        }

        public Builder frames(int value) {
            this.frames = value;
            return this;
        }

        public Builder particleNumber(int value) {
            this.particleNumber = value;
            return this;
        }

        public Builder selectedMotionTypes(Set<MotionType> values) {
            if (values == null) {
                throw new IllegalArgumentException("Selected motion types cannot be null.");
            }
            this.selectedMotionTypes = copyOf(values);
            return this;
        }

        public Builder randomSeed(long value) {
            this.randomSeed = value;
            return this;
        }

        public Builder microscopeConfig(MicroscopeConfig value) {
            if (value == null) {
                throw new IllegalArgumentException("Microscope config cannot be null.");
            }
            this.microscopeConfig = value;
            return this;
        }

        public Builder noiseConfig(NoiseConfig value) {
            if (value == null) {
                throw new IllegalArgumentException("Noise config cannot be null.");
            }
            this.noiseConfig = value;
            return this;
        }

        public Builder spotShape(PsfShapeType value) {
            if (value == null) {
                throw new IllegalArgumentException("Spot shape cannot be null.");
            }
            this.spotShape = value;
            return this;
        }

        public Builder appearanceDistributionConfig(
                ParticleAppearanceDistributionConfig value
        ) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Appearance distribution config cannot be null."
                );
            }
            this.appearanceDistributionConfig = value;
            return this;
        }

        public Builder motionParameterDistributionConfig(
                MotionParameterDistributionConfig value
        ) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Motion parameter distribution config cannot be null."
                );
            }
            this.motionParameterDistributionConfig = value;
            return this;
        }

        public Builder randomCompositionConfig(RandomCompositionConfig value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Random composition config cannot be null."
                );
            }
            this.randomCompositionConfig = value;
            return this;
        }

        public SimulationConfig build() {
            SimulationConfig config = new SimulationConfig(this);
            SimulationConfigValidator.validate(config);
            return config;
        }
    }
}
