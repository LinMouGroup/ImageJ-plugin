package io.github.zhengfangfang0304.synspt.scene;

import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Immutable scene containing all ground-truth particle tracks.
 */
public final class Scene {

    private final int widthPixels;
    private final int heightPixels;
    private final double pixelSizeUm;
    private final int frameCount;
    private final List<ParticleTrack> particleTracks;

    public Scene(
            int widthPixels,
            int heightPixels,
            double pixelSizeUm,
            List<ParticleTrack> particleTracks
    ) {
        if (widthPixels <= 0 || heightPixels <= 0) {
            throw new IllegalArgumentException("Scene dimensions must be positive.");
        }
        if (Double.isNaN(pixelSizeUm)
                || Double.isInfinite(pixelSizeUm)
                || pixelSizeUm <= 0.0) {
            throw new IllegalArgumentException("Pixel size must be finite and positive.");
        }
        if (particleTracks == null || particleTracks.isEmpty()) {
            throw new IllegalArgumentException("Particle tracks cannot be null or empty.");
        }
        if (particleTracks.get(0) == null) {
            throw new IllegalArgumentException("Particle tracks cannot contain null.");
        }
        int expectedFrameCount = particleTracks.get(0).getTrajectory().length();
        double widthUm = widthPixels * pixelSizeUm;
        double heightUm = heightPixels * pixelSizeUm;
        if (Double.isInfinite(widthUm) || Double.isInfinite(heightUm)) {
            throw new IllegalArgumentException("Physical scene dimensions must be finite.");
        }
        Set<Integer> particleIds = new HashSet<Integer>();
        for (ParticleTrack track : particleTracks) {
            if (track == null) {
                throw new IllegalArgumentException("Particle tracks cannot contain null.");
            }
            if (!particleIds.add(track.getParticle().getParticleId())) {
                throw new IllegalArgumentException("Particle IDs must be unique.");
            }
            if (track.getTrajectory().length() != expectedFrameCount) {
                throw new IllegalArgumentException(
                        "All particle tracks must have the same frame count."
                );
            }
            validateInsideFieldOfView(track, widthUm, heightUm);
        }

        this.widthPixels = widthPixels;
        this.heightPixels = heightPixels;
        this.pixelSizeUm = pixelSizeUm;
        this.frameCount = expectedFrameCount;
        this.particleTracks = Collections.unmodifiableList(
                new ArrayList<ParticleTrack>(particleTracks)
        );
    }

    public int getWidthPixels() {
        return widthPixels;
    }

    public int getHeightPixels() {
        return heightPixels;
    }

    public double getPixelSizeUm() {
        return pixelSizeUm;
    }

    public double getWidthUm() {
        return widthPixels * pixelSizeUm;
    }

    public double getHeightUm() {
        return heightPixels * pixelSizeUm;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public List<ParticleTrack> getParticleTracks() {
        return particleTracks;
    }

    private static void validateInsideFieldOfView(
            ParticleTrack track,
            double widthUm,
            double heightUm
    ) {
        for (int frame = 0; frame < track.getTrajectory().length(); frame++) {
            double xUm = track.getTrajectory().getXUm(frame);
            double yUm = track.getTrajectory().getYUm(frame);
            if (xUm < 0.0 || xUm >= widthUm || yUm < 0.0 || yUm >= heightUm) {
                throw new IllegalArgumentException(
                        "Particle trajectory must remain inside the field of view."
                );
            }
        }
    }
}
