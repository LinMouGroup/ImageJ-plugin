package io.github.zhengfangfang0304.synspt.noise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Physical photon-budget calibration for one simulation run. */
public final class PhotonCalibrationResult {

    public static final String CALIBRATION_METHOD = "photon_budget";

    private final double targetSnr;
    private final SnrDefinition snrDefinition;
    private final double photonScale;
    private final double referencePhotonBudget;
    private final double predictedSnr;
    private final double maximumAchievableSnr;
    private final double backgroundPhotonsPerPixel;
    private final double physicalReadNoiseSigmaAdu;
    private final List<Double> relativeBrightnesses;
    private final List<Double> particlePhotonBudgets;
    private final List<Double> particleSnrs;

    public PhotonCalibrationResult(
            double targetSnr,
            SnrDefinition snrDefinition,
            double photonScale,
            double referencePhotonBudget,
            double predictedSnr,
            double maxSnr,
            double background,
            double readNoiseAdu,
            List<Double> relativeBrightnesses,
            List<Double> particlePhotonBudgets,
            List<Double> particleSnrs
    ) {
        requirePositive(targetSnr, "Target SNR");
        requirePositive(photonScale, "Photon scale");
        requirePositive(referencePhotonBudget, "Reference photon budget");
        requirePositive(predictedSnr, "Predicted SNR");
        requirePositive(maxSnr, "Maximum achievable SNR");
        requireNonNegative(background, "Background photons");
        requireNonNegative(readNoiseAdu, "Physical read noise");
        if (snrDefinition == null) {
            throw new IllegalArgumentException("SNR definition cannot be null.");
        }
        if (relativeBrightnesses == null
                || particlePhotonBudgets == null
                || particleSnrs == null
                || relativeBrightnesses.isEmpty()
                || relativeBrightnesses.size() != particlePhotonBudgets.size()
                || relativeBrightnesses.size() != particleSnrs.size()) {
            throw new IllegalArgumentException(
                    "Calibration lists must be non-empty and have equal sizes."
            );
        }
        this.targetSnr = targetSnr;
        this.snrDefinition = snrDefinition;
        this.photonScale = photonScale;
        this.referencePhotonBudget = referencePhotonBudget;
        this.predictedSnr = predictedSnr;
        this.maximumAchievableSnr = maxSnr;
        this.backgroundPhotonsPerPixel = background;
        this.physicalReadNoiseSigmaAdu = readNoiseAdu;
        this.relativeBrightnesses = copyPositive(relativeBrightnesses, "Relative brightness");
        this.particlePhotonBudgets = copyPositive(particlePhotonBudgets, "Photon budget");
        this.particleSnrs = copyPositive(particleSnrs, "Particle SNR");
    }

    public double getTargetSnr() {
        return targetSnr;
    }

    public SnrDefinition getSnrDefinition() {
        return snrDefinition;
    }

    public String getCalibrationMethod() {
        return CALIBRATION_METHOD;
    }

    public double getPhotonScale() {
        return photonScale;
    }

    public double getReferencePhotonBudget() {
        return referencePhotonBudget;
    }

    public double getPredictedSnr() {
        return predictedSnr;
    }

    public double getRealizedSnr() {
        return median(particleSnrs);
    }

    public double getMaximumAchievableSnr() {
        return maximumAchievableSnr;
    }

    public double getBackgroundPhotonsPerPixel() {
        return backgroundPhotonsPerPixel;
    }

    public double getPhysicalReadNoiseSigmaAdu() {
        return physicalReadNoiseSigmaAdu;
    }

    public List<Double> getRelativeBrightnesses() {
        return relativeBrightnesses;
    }

    public List<Double> getParticlePhotonBudgets() {
        return particlePhotonBudgets;
    }

    public double getParticlePhotonBudget(int index) {
        return particlePhotonBudgets.get(index).doubleValue();
    }

    public List<Double> getParticleSnrs() {
        return particleSnrs;
    }

    public double getMinimumParticleSnr() {
        return Collections.min(particleSnrs).doubleValue();
    }

    public double getMeanParticleSnr() {
        return mean(particleSnrs);
    }

    public double getMedianParticleSnr() {
        return median(particleSnrs);
    }

    public double getMaximumParticleSnr() {
        return Collections.max(particleSnrs).doubleValue();
    }

    private static List<Double> copyPositive(List<Double> values, String name) {
        List<Double> copy = new ArrayList<Double>(values.size());
        for (Double boxed : values) {
            if (boxed == null) {
                throw new IllegalArgumentException(name + " values cannot be null.");
            }
            double value = boxed.doubleValue();
            requirePositive(value, name);
            copy.add(value);
        }
        return Collections.unmodifiableList(copy);
    }

    private static double mean(List<Double> values) {
        double sum = 0.0;
        for (int index = 0; index < values.size(); index++) {
            sum += values.get(index).doubleValue();
        }
        return sum / values.size();
    }

    private static double median(List<Double> values) {
        List<Double> sorted = new ArrayList<Double>(values);
        Collections.sort(sorted);
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(middle).doubleValue();
        }
        return 0.5 * (sorted.get(middle - 1).doubleValue()
                + sorted.get(middle).doubleValue());
    }

    private static void requirePositive(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(
                    name + " must be finite and positive."
            );
        }
    }

    private static void requireNonNegative(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
            throw new IllegalArgumentException(
                    name + " must be finite and non-negative."
            );
        }
    }

}
