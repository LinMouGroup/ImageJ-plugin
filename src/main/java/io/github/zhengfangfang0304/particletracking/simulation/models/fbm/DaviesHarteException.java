package io.github.zhengfangfang0304.particletracking.simulation.models.fbm;

/**
 * Signals that a Davies-Harte circulant embedding is not non-negative.
 */
public class DaviesHarteException extends IllegalStateException {

    public DaviesHarteException(String message) {
        super(message);
    }

    public DaviesHarteException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
