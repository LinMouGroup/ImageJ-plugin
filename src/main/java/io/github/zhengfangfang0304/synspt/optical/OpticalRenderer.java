package io.github.zhengfangfang0304.synspt.optical;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.scene.Scene;

/** Renders one scene time point into expected photons per camera pixel. */
public interface OpticalRenderer {

    ExpectedPhotonFrame renderFrame(
            Scene scene,
            int frameIndex,
            MicroscopeConfig microscopeConfig
    );
}
