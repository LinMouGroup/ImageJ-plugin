package io.github.zhengfangfang0304.synspt.motion.parameters;

import io.github.zhengfangfang0304.synspt.motion.MotionType;

import java.util.Map;

/**
 * Marker and metadata contract for immutable model-specific parameters.
 */
public interface MotionParameters {

    MotionType getMotionType();

    /**
     * Returns stable ordered parameter names and values, including units.
     */
    Map<String, String> toMetadata();
}
