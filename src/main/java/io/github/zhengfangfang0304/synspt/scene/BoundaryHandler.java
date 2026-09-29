package io.github.zhengfangfang0304.synspt.scene;

/**
 * Applies a scene-level field-of-view boundary independently of local model
 * boundaries such as confined diffusion.
 */
public interface BoundaryHandler {

    double apply(double coordinate, double upperExclusive);
}
