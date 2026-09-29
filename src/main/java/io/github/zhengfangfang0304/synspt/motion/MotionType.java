package io.github.zhengfangfang0304.synspt.motion;

import java.util.EnumSet;

/**
 * User-selectable basic motion models and the composite multi-state model.
 */
public enum MotionType {

    BROWNIAN("Brownian Motion"),
    DIRECTED("Directed Motion"),
    CONFINED("Confined Diffusion"),
    FBM("Fractional Brownian Motion"),
    CTRW("Continuous-Time Random Walk"),
    LEVY_WALK("Lévy Walk"),
    SBM("Scaled Brownian Motion"),
    ATTM("Annealed Transient Time Motion"),
    MULTI_STATE("Multi-state Motion");

    private static final EnumSet<MotionType> BASE_TYPES = EnumSet.range(
            BROWNIAN,
            ATTM
    );
    
    private final String displayName;

    MotionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isComposite() {
        return this == MULTI_STATE;
    }

    public static EnumSet<MotionType> baseTypes() {
        return EnumSet.copyOf(BASE_TYPES);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
