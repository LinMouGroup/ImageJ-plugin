package io.github.zhengfangfang0304.synspt.noise;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.optical.ExpectedPhotonFrame;

/** Converts expected photon counts into digitized camera samples. */
public interface NoiseModel {

    CameraFrame apply(
            ExpectedPhotonFrame expectedPhotonFrame,
            MicroscopeConfig microscopeConfig,
            long randomSeed
    );
}
