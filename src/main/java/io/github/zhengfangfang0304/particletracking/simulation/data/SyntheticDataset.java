package io.github.zhengfangfang0304.particletracking.simulation.data;

import ij.ImagePlus;
import io.github.zhengfangfang0304.particletracking.simulation.config.ImagingConfig;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Couples a rendered synthetic image stack with ground-truth trajectories.
 */
public class SyntheticDataset {

    private static final String SPATIAL_UNIT = "um";

    private static final String TIME_UNIT = "s";

    private final List<SyntheticParticle> particles;

    private final ImagePlus image;

    private final double pixelSizeUmPerPixel;

    private final double frameIntervalSeconds;

    private final Set<MotionType> motionTypes;

    /**
     * Creates a unified dataset whose motion metadata comes from particles.
     */
    public SyntheticDataset(
            List<SyntheticParticle> particles,
            ImagePlus image,
            ImagingConfig imagingConfig
    ) {
        if (particles == null) {
            throw new IllegalArgumentException(
                    "Synthetic particles cannot be null."
            );
        }
        if (image == null) {
            throw new IllegalArgumentException(
                    "Synthetic image cannot be null."
            );
        }
        if (imagingConfig == null) {
            throw new IllegalArgumentException(
                    "Imaging config cannot be null."
            );
        }
        this.particles = new ArrayList<>(particles);
        this.image = image;
        this.pixelSizeUmPerPixel =
                imagingConfig.getPixelSizeUmPerPixel();
        this.frameIntervalSeconds =
                imagingConfig.getFrameIntervalSeconds();
        EnumSet<MotionType> assignedMotionTypes =
                EnumSet.noneOf(MotionType.class);
        for (SyntheticParticle particle : particles) {
            if (particle.getMotionAssignment() != null) {
                assignedMotionTypes.add(
                        particle.getMotionAssignment()
                                .getMotionProfile()
                                .getMotionType()
                );
            }
        }
        this.motionTypes = Collections.unmodifiableSet(
                assignedMotionTypes
        );
    }

    public List<SyntheticParticle> getParticles() {
        return Collections.unmodifiableList(particles);
    }

    public ImagePlus getImage() {
        return image;
    }

    public double getPixelSizeUmPerPixel() {
        return pixelSizeUmPerPixel;
    }

    public double getFrameIntervalSeconds() {
        return frameIntervalSeconds;
    }

    public String getSpatialUnit() {
        return SPATIAL_UNIT;
    }

    public String getTimeUnit() {
        return TIME_UNIT;
    }

    /**
     * Returns the particle-level motion types present in this dataset.
     *
     * <p>An empty set identifies a dataset produced through the legacy path
     * without particle motion assignments.</p>
     */
    public Set<MotionType> getMotionTypes() {
        return motionTypes;
    }
}
