package io.github.zhengfangfang0304.synspt.motion;

import io.github.zhengfangfang0304.synspt.particle.Trajectory;
import io.github.zhengfangfang0304.synspt.util.SeedDerivation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates one continuous displacement trajectory by executing its basic
 * state segments in order. This composite is deliberately not a MotionModel.
 */
public final class MultiStateMotion {

    private final List<StateSegment> segments;

    public MultiStateMotion(List<StateSegment> segments) {
        if (segments == null || segments.size() < 2) {
            throw new IllegalArgumentException(
                    "Multi-state motion requires at least two segments."
            );
        }
        this.segments = validateAndCopy(segments);
    }

    public List<StateSegment> getSegments() {
        return segments;
    }

    public Trajectory generate(MotionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Motion context cannot be null.");
        }
        int frameCount = segments.get(segments.size() - 1).getEndFrame() + 1;
        if (context.getFrames() != frameCount) {
            throw new IllegalArgumentException(
                    "Motion context frame count must match the state plan."
            );
        }

        double[] x = new double[frameCount];
        double[] y = new double[frameCount];
        int particleId = segments.get(0).getParticleId();
        for (StateSegment segment : segments) {
            boolean firstSegment = segment.getStateIndex() == 0;
            int generatedFrames = segment.getDurationFrames()
                    + (firstSegment ? 0 : 1);
            MotionModel model = MotionModelFactory.create(
                    segment.getMotionParameters()
            );
            Trajectory local = model.generate(new MotionContext(
                    generatedFrames,
                    context.getFrameIntervalSeconds(),
                    SeedDerivation.multiStateTrajectorySeed(
                            context.getRandomSeed(),
                            particleId,
                            segment.getStateIndex()
                    )
            ));
            double originX = firstSegment ? 0.0 : x[segment.getStartFrame() - 1];
            double originY = firstSegment ? 0.0 : y[segment.getStartFrame() - 1];
            int localOffset = firstSegment ? 0 : 1;
            for (int frame = segment.getStartFrame();
                    frame <= segment.getEndFrame(); frame++) {
                int localIndex = frame - segment.getStartFrame() + localOffset;
                x[frame] = originX + local.getXUm(localIndex);
                y[frame] = originY + local.getYUm(localIndex);
            }
        }
        return new Trajectory(x, y);
    }

    private List<StateSegment> validateAndCopy(List<StateSegment> input) {
        List<StateSegment> copy = new ArrayList<StateSegment>(input.size());
        int particleId = input.get(0) == null
                ? -1
                : input.get(0).getParticleId();
        int expectedStart = 0;
        for (int index = 0; index < input.size(); index++) {
            StateSegment segment = input.get(index);
            if (segment == null
                    || segment.getParticleId() != particleId
                    || segment.getStateIndex() != index
                    || segment.getStartFrame() != expectedStart) {
                throw new IllegalArgumentException(
                        "State segments must be contiguous, ordered, and belong to one particle."
                );
            }
            copy.add(segment);
            expectedStart = segment.getEndFrame() + 1;
        }
        return Collections.unmodifiableList(copy);
    }
}
