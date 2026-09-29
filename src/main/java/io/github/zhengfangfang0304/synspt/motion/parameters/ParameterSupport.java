package io.github.zhengfangfang0304.synspt.motion.parameters;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Validation and stable metadata helpers shared by parameter value objects.
 */
final class ParameterSupport {

    private ParameterSupport() {
    }

    static void requirePositive(double value, String name) {
        if (!isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive.");
        }
    }

    static void requireNonNegative(double value, String name) {
        if (!isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative.");
        }
    }

    static void requireOpenUnitInterval(double value, String name) {
        if (!isFinite(value) || value <= 0.0 || value >= 1.0) {
            throw new IllegalArgumentException(name + " must be between zero and one.");
        }
    }

    static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    static String number(double value) {
        return Double.toString(value);
    }

    static Map<String, String> metadata(String... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Metadata requires key/value pairs.");
        }
        Map<String, String> values = new LinkedHashMap<String, String>();
        for (int index = 0; index < keysAndValues.length; index += 2) {
            values.put(keysAndValues[index], keysAndValues[index + 1]);
        }
        return Collections.unmodifiableMap(values);
    }
}
