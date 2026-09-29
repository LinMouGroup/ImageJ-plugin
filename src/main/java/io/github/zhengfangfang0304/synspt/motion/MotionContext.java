package io.github.zhengfangfang0304.synspt.motion;

/**
 * Per-particle execution context supplied to a motion model.
 */
public final class MotionContext {

    private final int frames;
    private final double frameIntervalSeconds;
    private final long randomSeed;

    public MotionContext(int frames, double frameIntervalSeconds, long randomSeed) {
        if (frames <= 0) {
            throw new IllegalArgumentException("Frames must be positive.");
        }
        if (Double.isNaN(frameIntervalSeconds)
                || Double.isInfinite(frameIntervalSeconds)
                || frameIntervalSeconds <= 0.0) {
            throw new IllegalArgumentException("Frame interval must be finite and positive.");
        }
        this.frames = frames;
        this.frameIntervalSeconds = frameIntervalSeconds;
        this.randomSeed = randomSeed;
    }

    public int getFrames() {
        return frames;
    }

    public double getFrameIntervalSeconds() {
        return frameIntervalSeconds;
    }

    public long getRandomSeed() {
        return randomSeed;
    }
}
