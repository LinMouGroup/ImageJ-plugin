package io.github.zhengfangfang0304.synspt.optical;

/** Immutable PSF geometry assigned to one particle for its full lifetime. */
public final class ParticlePsfParameters {

    private static final double EQUALITY_TOLERANCE = 1.0e-12;

    private final PsfSizeClass sizeClass;
    private final PsfShapeType shapeType;
    private final double radiusPixels;
    private final double sigmaXPixels;
    private final double sigmaYPixels;
    private final double rotationRadians;

    public ParticlePsfParameters(
            PsfSizeClass sizeClass,
            PsfShapeType shapeType,
            double radiusPixels,
            double sigmaXPixels,
            double sigmaYPixels,
            double rotationRadians
    ) {
        if (sizeClass == null || shapeType == null) {
            throw new IllegalArgumentException(
                    "PSF size class and shape cannot be null."
            );
        }
        requirePositive(radiusPixels, "PSF radius");
        requirePositive(sigmaXPixels, "PSF sigma X");
        requirePositive(sigmaYPixels, "PSF sigma Y");
        if (Double.isNaN(rotationRadians)
                || Double.isInfinite(rotationRadians)
                || rotationRadians < 0.0
                || rotationRadians >= Math.PI) {
            throw new IllegalArgumentException(
                    "PSF rotation must be in [0, pi)."
            );
        }
        if (shapeType == PsfShapeType.CIRCULAR_GAUSSIAN
                && (Math.abs(sigmaXPixels - sigmaYPixels) > EQUALITY_TOLERANCE
                || rotationRadians != 0.0)) {
            throw new IllegalArgumentException(
                    "Circular PSFs require equal sigmas and zero rotation."
            );
        }
        if (radiusPixels < Math.max(sigmaXPixels, sigmaYPixels)) {
            throw new IllegalArgumentException(
                    "PSF radius must be at least the largest sigma."
            );
        }
        this.sizeClass = sizeClass;
        this.shapeType = shapeType;
        this.radiusPixels = radiusPixels;
        this.sigmaXPixels = sigmaXPixels;
        this.sigmaYPixels = sigmaYPixels;
        this.rotationRadians = rotationRadians;
    }

    public PsfSizeClass getSizeClass() {
        return sizeClass;
    }

    public PsfShapeType getShapeType() {
        return shapeType;
    }

    public double getRadiusPixels() {
        return radiusPixels;
    }

    public double getSigmaXPixels() {
        return sigmaXPixels;
    }

    public double getSigmaYPixels() {
        return sigmaYPixels;
    }

    public double getRotationRadians() {
        return rotationRadians;
    }

    private static void requirePositive(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive.");
        }
    }
}
