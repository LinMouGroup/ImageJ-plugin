package io.github.zhengfangfang0304.synspt.engine;

/** Read-only cancellation signal suitable for a future SwingWorker. */
public interface SimulationCancellationToken {

    SimulationCancellationToken NONE = new SimulationCancellationToken() {
        @Override
        public boolean isCancellationRequested() {
            return false;
        }
    };

    boolean isCancellationRequested();
}
