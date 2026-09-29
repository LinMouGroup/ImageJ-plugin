package io.github.zhengfangfang0304.synspt.engine;

import java.util.concurrent.CancellationException;

/** Signals cooperative cancellation between particle trajectories. */
public final class SimulationCancelledException extends CancellationException {

    private static final long serialVersionUID = 1L;

    public SimulationCancelledException() {
        super("Synthetic-data simulation was cancelled.");
    }
}
