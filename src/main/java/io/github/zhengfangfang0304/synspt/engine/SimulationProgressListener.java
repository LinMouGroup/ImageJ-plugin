package io.github.zhengfangfang0304.synspt.engine;

/**
 * Progress callback shared by the future Swing worker and simulation engine.
 */
public interface SimulationProgressListener {

    SimulationProgressListener NONE = new SimulationProgressListener() {
        @Override
        public void onProgress(int percent, String message) {
            // Deliberately empty.
        }
    };

    void onProgress(int percent, String message);
}
