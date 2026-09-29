package io.github.zhengfangfang0304.synspt.optical;

/** Supported point-spread-function shapes and their GUI labels. */
public enum PsfShapeType {

    CIRCULAR_GAUSSIAN("Circular Gaussian"),
    ELLIPTICAL_GAUSSIAN("Elliptical Gaussian");

    private final String displayName;

    PsfShapeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
