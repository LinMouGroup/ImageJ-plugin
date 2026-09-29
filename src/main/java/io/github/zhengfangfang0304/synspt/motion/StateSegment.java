package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.motion.parameters.MotionParameters;

/**
 * Immutable inclusive frame interval governed by one basic motion model.
 */
public final class StateSegment {

    private final int particleId;
    private final int stateIndex;
    private final int startFrame;
    private final int endFrame;
    private final MotionType motionType;
    private final MotionParameters motionParameters;

    public StateSegment(
            int particleId,
            int stateIndex,
            int startFrame,
            int endFrame,
            MotionType motionType,
            MotionParameters motionParameters
    ) {
        if (particleId <= 0) {
            throw new IllegalArgumentException("Particle ID must be positive.");
        }
        if (stateIndex < 0) {
            throw new IllegalArgumentException("State index cannot be negative.");
        }
        if (startFrame < 0 || endFrame < startFrame) {
            throw new IllegalArgumentException(
                    "State frame interval must be non-negative and non-empty."
            );
        }
        if (motionType == null || motionParameters == null) {
            throw new IllegalArgumentException(
                    "State motion type and parameters cannot be null."
            );
        }
        if (motionType.isComposite()) {
            throw new IllegalArgumentException(
                    "A state segment must use one basic motion model."
            );
        }
        if (motionParameters.getMotionType() != motionType) {
            throw new IllegalArgumentException(
                    "State motion parameters must match its motion type."
            );
        }
        this.particleId = particleId;
        this.stateIndex = stateIndex;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.motionType = motionType;
        this.motionParameters = motionParameters;
    }

    public int getParticleId() {
        return particleId;
    }

    public int getStateIndex() {
        return stateIndex;
    }

    public int getStartFrame() {
        return startFrame;
    }

    public int getEndFrame() {
        return endFrame;
    }

    public int getDurationFrames() {
        return endFrame - startFrame + 1;
    }

    public MotionType getMotionType() {
        return motionType;
    }

    public MotionParameters getMotionParameters() {
        return motionParameters;
    }

    public boolean containsFrame(int frame) {
        return frame >= startFrame && frame <= endFrame;
    }
}
