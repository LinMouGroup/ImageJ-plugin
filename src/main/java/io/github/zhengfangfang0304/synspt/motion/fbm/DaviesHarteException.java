package io.github.zhengfangfang0304.synspt.motion.fbm;

/** Signals that a Davies-Harte circulant embedding is numerically invalid. */
public class DaviesHarteException extends IllegalStateException {

    public DaviesHarteException(String message) {
        super(message);
    }

    public DaviesHarteException(String message, Throwable cause) {
        super(message, cause);
    }
}
