package io.github.zhengfangfang0304.synspt.particle;

import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;
import io.github.zhengfangfang0304.synspt.optical.ParticleImagingParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable identity, realized motion state plan, and imaging properties.
 */
public final class Particle {

    private final int particleId;
    private final MotionType motionType;
    private final List<StateSegment> stateSegments;
    private final ParticleImagingParameters imagingParameters;

    public Particle(
            int particleId,
            MotionType motionType,
            List<StateSegment> stateSegments,
            ParticleImagingParameters imagingParameters
    ) {
        if (particleId <= 0) {
            throw new IllegalArgumentException("Particle ID must be positive.");
        }
        if (motionType == null || stateSegments == null
                || imagingParameters == null) {
            throw new IllegalArgumentException(
                    "Motion type, state segments, and imaging parameters cannot be null."
            );
        }
        this.particleId = particleId;
        this.motionType = motionType;
        this.stateSegments = validateAndCopyStateSegments(
                particleId,
                motionType,
                stateSegments
        );
        this.imagingParameters = imagingParameters;
    }

    public int getParticleId() {
        return particleId;
    }

    public MotionType getMotionType() {
        return motionType;
    }

    public List<StateSegment> getStateSegments() {
        return stateSegments;
    }

    public StateSegment getStateSegmentForFrame(int frame) {
        if (frame < 0 || frame >= getFrameCount()) {
            throw new IllegalArgumentException("Frame is outside the state plan.");
        }
        for (StateSegment segment : stateSegments) {
            if (segment.containsFrame(frame)) {
                return segment;
            }
        }
        throw new IllegalStateException("State plan does not cover the frame.");
    }

    public int getFrameCount() {
        return stateSegments.get(stateSegments.size() - 1).getEndFrame() + 1;
    }

    public ParticleImagingParameters getImagingParameters() {
        return imagingParameters;
    }

    private List<StateSegment> validateAndCopyStateSegments(
            int expectedParticleId,
            MotionType topLevelType,
            List<StateSegment> input
    ) {
        if (input.isEmpty()) {
            throw new IllegalArgumentException(
                    "A particle must contain at least one state segment."
            );
        }
        if (topLevelType.isComposite()) {
            if (input.size() < 2 || input.size() > 5) {
                throw new IllegalArgumentException(
                        "Multi-state particles must contain two to five states."
                );
            }
        } else if (input.size() != 1) {
            throw new IllegalArgumentException(
                    "A basic-motion particle must contain exactly one state."
            );
        }

        List<StateSegment> copy = new ArrayList<StateSegment>(input.size());
        int expectedStart = 0;
        for (int index = 0; index < input.size(); index++) {
            StateSegment segment = input.get(index);
            if (segment == null
                    || segment.getParticleId() != expectedParticleId
                    || segment.getStateIndex() != index
                    || segment.getStartFrame() != expectedStart) {
                throw new IllegalArgumentException(
                        "Particle states must be ordered, contiguous, and match its ID."
                );
            }
            if (!topLevelType.isComposite()
                    && segment.getMotionType() != topLevelType) {
                throw new IllegalArgumentException(
                        "A basic-motion particle state must match its motion type."
                );
            }
            copy.add(segment);
            expectedStart = segment.getEndFrame() + 1;
        }
        return Collections.unmodifiableList(copy);
    }
}
