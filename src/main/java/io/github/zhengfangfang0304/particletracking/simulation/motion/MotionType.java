package io.github.zhengfangfang0304.particletracking.simulation.motion;

/**
 * User-facing semantic categories for synthetic particle motion.
 *
 * <p>This enum deliberately contains no mathematical or physical model
 * parameters. Those values are resolved internally by
 * {@link MotionProfileResolver}.</p>
 */
public enum MotionType {

    NORMAL_DIFFUSION("Normal diffusion"),

    SUBDIFFUSION("Subdiffusion"),

    SUPERDIFFUSION("Superdiffusion"),

    DIRECTED_ANOMALOUS_DIFFUSION(
            "Directed anomalous diffusion"
    ),

    IMMOBILE("Immobile");

    private final String displayName;

    MotionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
