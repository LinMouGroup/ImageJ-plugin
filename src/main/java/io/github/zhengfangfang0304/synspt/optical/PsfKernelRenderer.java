package io.github.zhengfangfang0304.synspt.optical;

/** Adds one particle's PSF contribution to a mutable expected-photon frame. */
public interface PsfKernelRenderer {

    PsfShapeType getShapeType();

    void render(
            double[] expectedPhotons,
            int width,
            int height,
            double centerX,
            double centerY,
            ParticleImagingParameters imagingParameters
    );
}
