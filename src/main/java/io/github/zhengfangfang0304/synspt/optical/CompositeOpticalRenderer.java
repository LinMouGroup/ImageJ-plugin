package io.github.zhengfangfang0304.synspt.optical;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.particle.ParticleTrack;
import io.github.zhengfangfang0304.synspt.scene.Scene;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/** Dispatches every particle to the renderer matching its lifetime PSF shape. */
public final class CompositeOpticalRenderer implements OpticalRenderer {

    private final Map<PsfShapeType, PsfKernelRenderer> renderers;

    public CompositeOpticalRenderer() {
        this(new PsfKernelRenderer[] {
                new CircularGaussianPsfRenderer(),
                new EllipticalGaussianPsfRenderer()
        });
    }

    public CompositeOpticalRenderer(PsfKernelRenderer[] kernelRenderers) {
        if (kernelRenderers == null) {
            throw new IllegalArgumentException("PSF renderers cannot be null.");
        }
        EnumMap<PsfShapeType, PsfKernelRenderer> mapped =
                new EnumMap<PsfShapeType, PsfKernelRenderer>(PsfShapeType.class);
        for (PsfKernelRenderer renderer : kernelRenderers) {
            if (renderer == null || mapped.put(renderer.getShapeType(), renderer) != null) {
                throw new IllegalArgumentException(
                        "PSF renderers must be non-null and unique by shape."
                );
            }
        }
        if (mapped.size() != PsfShapeType.values().length) {
            throw new IllegalArgumentException(
                    "Every PSF shape must have exactly one renderer."
            );
        }
        this.renderers = mapped;
    }

    @Override
    public ExpectedPhotonFrame renderFrame(
            Scene scene,
            int frameIndex,
            MicroscopeConfig microscopeConfig
    ) {
        validateArguments(scene, frameIndex, microscopeConfig);
        int width = scene.getWidthPixels();
        int height = scene.getHeightPixels();
        long pixelCount = (long) width * height;
        if (pixelCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Rendered frame contains too many pixels.");
        }
        double[] photons = new double[(int) pixelCount];
        Arrays.fill(photons, microscopeConfig.getBackgroundPhotons());
        for (ParticleTrack track : scene.getParticleTracks()) {
            ParticleImagingParameters imaging =
                    track.getParticle().getImagingParameters();
            PsfKernelRenderer renderer = renderers.get(
                    imaging.getPsfParameters().getShapeType()
            );
            renderer.render(
                    photons,
                    width,
                    height,
                    track.getTrajectory().getXUm(frameIndex) / scene.getPixelSizeUm(),
                    track.getTrajectory().getYUm(frameIndex) / scene.getPixelSizeUm(),
                    imaging
            );
        }
        return ExpectedPhotonFrame.takeOwnership(width, height, photons);
    }

    private void validateArguments(
            Scene scene,
            int frameIndex,
            MicroscopeConfig microscopeConfig
    ) {
        if (scene == null || microscopeConfig == null) {
            throw new IllegalArgumentException(
                    "Scene and microscope config cannot be null."
            );
        }
        if (frameIndex < 0 || frameIndex >= scene.getFrameCount()) {
            throw new IllegalArgumentException("Frame index is outside the scene.");
        }
        if (Double.compare(scene.getPixelSizeUm(),
                microscopeConfig.getPixelSizeUm()) != 0) {
            throw new IllegalArgumentException(
                    "Scene and microscope pixel sizes must match."
            );
        }
    }
}
