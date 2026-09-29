package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig;
import io.github.zhengfangfang0304.synspt.config.MotionParameterDistributionConfig.NumericRange;
import io.github.zhengfangfang0304.synspt.config.ParticleAppearanceDistributionConfig;
import io.github.zhengfangfang0304.synspt.config.PsfSizeMixtureConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.motion.StateSegment;
import io.github.zhengfangfang0304.synspt.motion.MultiStateGenerator;
import io.github.zhengfangfang0304.synspt.motion.ChangePointGenerator;
import io.github.zhengfangfang0304.synspt.optical.ParticlePsfParameters;
import io.github.zhengfangfang0304.synspt.util.SeedDerivation;
import io.github.zhengfangfang0304.synspt.run.SimulationRunContext;
import io.github.zhengfangfang0304.synspt.noise.PhotonCalibrationResult;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;
import io.github.zhengfangfang0304.synspt.optical.PsfSizeClass;
import io.github.zhengfangfang0304.synspt.particle.Particle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/** Writes complete request, realized-plan, and calibration metadata. */
public final class MetadataJsonExporter {

    public void write(
            RenderedSimulationResult result,
            Path outputPath,
            ExportConfig exportConfig,
            SimulationRunContext run
    )
            throws IOException {
        if (result == null || outputPath == null || exportConfig == null) {
            throw new IllegalArgumentException(
                    "Result, metadata path, and export config cannot be null."
            );
        }
        BufferedWriter writer = Files.newBufferedWriter(
                outputPath,
                StandardCharsets.UTF_8
        );
        try {
            writer.write(toJson(result, exportConfig, run));
            writer.newLine();
        } finally {
            writer.close();
        }
    }

    String toJson(
            RenderedSimulationResult result,
            ExportConfig exportConfig,
            SimulationRunContext run
    ) {
        if (result == null || exportConfig == null || run == null) {
            throw new IllegalArgumentException(
                    "Result and export config cannot be null."
            );
        }
        SimulationConfig config = result.getPlan().getRequestConfig();
        run.requireConfig(config);
        MicroscopeConfig microscope = config.getMicroscopeConfig();
        PhotonCalibrationResult calibration =
                result.getPlan().getPhotonCalibration();
        ModelUsage usage = modelUsage(result);
        StringBuilder json = new StringBuilder(8192);
        json.append("{\n");
        field(json, 1, "schema_version", "10.1", true);
        field(json, 1, "generator", "synSPT Synthetic Data Generator", true);
        field(json, 1, "run_id", run.getRunId(), true);
        numberField(json, 1, "root_seed", config.getRandomSeed(), true);
        field(json, 1, "started_at", run.getStartedAt().toString(), true);
        json.append("  \"selected_motion_models\": [");
        int selectedIndex = 0;
        for (MotionType type : config.getSelectedMotionTypes()) {
            if (selectedIndex++ > 0) { json.append(", "); }
            json.append('"').append(type.name()).append('"');
        }
        json.append("],\n");

        json.append("  \"image\": {\n");
        numberField(json, 2, "width_pixels", config.getImageWidth(), true);
        numberField(json, 2, "height_pixels", config.getImageHeight(), true);
        numberField(json, 2, "frames", config.getFrames(), true);
        numberField(json, 2, "particle_number", config.getParticleNumber(), true);
        numberField(json, 2, "pixel_size_um", microscope.getPixelSizeUm(), true);
        numberField(json, 2, "frame_interval_seconds",
                microscope.getFrameIntervalSeconds(), true);
        numberField(json, 2, "frame_rate_fps",
                1.0 / microscope.getFrameIntervalSeconds(), true);
        numberField(json, 2, "bit_depth", microscope.getBitDepth(), false);
        json.append("  },\n");

        appendComposition(json, result, usage);
        appendMotionDistribution(json,
                config.getMotionParameterDistributionConfig());
        appendAppearanceDistribution(json, result);
        appendTrajectorySchema(json);
        appendMultiStateRules(json);
        appendReproducibility(json, config);
        appendParticles(json, result);

        json.append("  \"optical\": {\n");
        field(json, 2, "spot_shape", config.getSpotShape().name(), true);
        field(json, 2, "psf_model", "Gaussian", true);
        field(json, 2, "psf_integration",
                config.getSpotShape() == PsfShapeType.CIRCULAR_GAUSSIAN
                        ? "analytic_error_function"
                        : "rotated_supersampled",
                true);
        numberField(json, 2, "background_photons_per_pixel",
                microscope.getBackgroundPhotons(), false);
        json.append("  },\n");

        json.append("  \"noise\": {\n");
        field(json, 2, "shot_noise", "poisson", true);
        field(json, 2, "snr_statistics_interpretation",
                "Predicted peak-pixel SNR at the reference pixel-center position; "
                        + "realized_snr is the median over calibrated particles, "
                        + "not a measurement from noisy images.", true);
        field(json, 2, "calibration_method",
                calibration.getCalibrationMethod(), true);
        numberField(json, 2, "target_snr",
                config.getNoiseConfig().getTargetSnr(), true);
        field(json, 2, "snr_definition",
                config.getNoiseConfig().getSnrDefinition().getMetadataName(), true);
        numberField(json, 2, "reference_photon_budget",
                calibration.getReferencePhotonBudget(), true);
        numberField(json, 2, "photon_scale",
                calibration.getPhotonScale(), true);
        numberField(json, 2, "physical_read_noise_sigma_adu",
                calibration.getPhysicalReadNoiseSigmaAdu(), true);
        numberField(json, 2, "background_photons_per_pixel",
                calibration.getBackgroundPhotonsPerPixel(), true);
        numberField(json, 2, "predicted_snr",
                calibration.getPredictedSnr(), true);
        numberField(json, 2, "realized_snr",
                calibration.getRealizedSnr(), true);
        numberField(json, 2, "maximum_achievable_snr",
                calibration.getMaximumAchievableSnr(), true);
        numberField(json, 2, "gain_adu_per_photon",
                microscope.getGainAduPerPhoton(), true);
        numberField(json, 2, "offset_adu", microscope.getOffsetAdu(), true);
        appendSnrDistribution(json, calibration);
        json.append("  },\n");

        json.append("  \"coordinates\": {\n");
        field(json, 2, "unit", "um", true);
        numberField(json, 2, "frame_index_base", 0, true);
        field(json, 2, "field_boundary", "reflecting", false);
        json.append("  },\n");

        json.append("  \"files\": {\n");
        field(json, 2, "image", DatasetExporter.TIFF_FILE_NAME, true);
        field(json, 2, "trajectory", DatasetExporter.TRAJECTORY_FILE_NAME, true);
        field(json, 2, "metadata", DatasetExporter.METADATA_FILE_NAME, true);
        booleanField(
                json,
                2,
                "advanced_export",
                exportConfig.isExportAdvanced(),
                exportConfig.isExportAdvanced()
        );
        if (exportConfig.isExportAdvanced()) {
            field(json, 2, "advanced_directory",
                    DatasetExporter.ADVANCED_DIRECTORY_NAME, true);
            field(json, 2, "trajectory_states",
                    advancedPath(DatasetExporter.TRAJECTORY_STATES_FILE_NAME), true);
            field(json, 2, "particle_properties",
                    advancedPath(DatasetExporter.PARTICLE_PROPERTIES_FILE_NAME), true);
            field(json, 2, "trajectory_segments",
                    advancedPath(DatasetExporter.TRAJECTORY_SEGMENTS_FILE_NAME), false);
        }
        json.append("  }\n");
        json.append('}');
        return json.toString();
    }

    private void appendTrajectorySchema(StringBuilder json) {
        json.append("  \"trajectory_schema\": {\n");
        field(json, 2, "columns", TrajectoryCsvExporter.HEADER, true);
        field(json, 2, "time_s", "frame * frame_interval_seconds", true);
        field(json, 2, "position", "x_um and y_um are final rendered ground-truth coordinates in um", true);
        field(json, 2, "segment_id", "Zero-based contiguous segment index, independent for each particle", true);
        field(json, 2, "motion_type", "Basic model of the saved segment; never MULTI_STATE", true);
        field(json, 2, "is_change_point", "true only at the first frame of a noninitial segment; frame 0 is false", true);
        field(json, 2, "motion_parameters", "Human-readable realized segment parameters with units; CSV quoting applies", true);
        field(json, 2, "segment_interval", "Inclusive start_frame and end_frame", true);
        field(json, 2, "transition_step", "The step into the first frame of a new segment uses the new model", false);
        json.append("  },\n");
    }

    private void appendMultiStateRules(StringBuilder json) {
        json.append("  \"multi_state\": {\n");
        numberField(json, 2, "minimum_state_count", MultiStateGenerator.MINIMUM_STATE_COUNT, true);
        numberField(json, 2, "maximum_state_count", MultiStateGenerator.MAXIMUM_STATE_COUNT, true);
        field(json, 2, "state_count_sampling",
                "Uniform integer from 2 through min(5, frames, max(2, floor(frames / 10)))", true);
        numberField(json, 2, "normal_minimum_segment_frames",
                ChangePointGenerator.DEFAULT_MINIMUM_SEGMENT_LENGTH_FRAMES, true);
        field(json, 2, "duration_rule",
                "Minimum min(10, floor(frames / state_count)); remaining frames allocated "
                        + "using normalized independent -log(1-U) weights and largest remainders "
                        + "(ties by segment index); durations sum to frames", true);
        field(json, 2, "change_point_rule", "Cumulative segment durations; no equal-split rule", true);
        field(json, 2, "model_sampling", "All eight basic models; first model uniform", true);
        numberField(json, 2, "consecutive_repeat_weight", 0.25, true);
        numberField(json, 2, "other_model_weight", 1.0, true);
        field(json, 2, "parameter_rule", "Parameters sampled independently per segment, including repeated models", true);
        field(json, 2, "memory_policy", "Each segment restarts its model clock and stochastic memory; positions remain continuous", false);
        json.append("  },\n");
    }

    private void appendReproducibility(StringBuilder json, SimulationConfig config) {
        long seed = config.getRandomSeed();
        json.append("  \"reproducibility\": {\n");
        field(json, 2, "root_seed_reference", "root_seed (top-level signed 64-bit integer)", true);
        field(json, 2, "rng", "java.util.Random", true);
        field(json, 2, "seed_derivation", "io.github.zhengfangfang0304.synspt.util.SeedDerivation", true);
        field(json, 2, "seed_rule",
                "Domain-separated 64-bit mixing of rootSeed, domain and index; "
                        + "particle IDs are one-based; frame and segment indices are zero-based. "
                        + "Multi-state parameter and trajectory streams use nested particle/segment derivation. "
                        + "Actual derived seeds are recorded as decimal strings.", true);
        field(json, 2, "motion_composition_seed", Long.toString(SeedDerivation.motionCompositionSeed(seed)), true);
        field(json, 2, "motion_assignment_seed", Long.toString(SeedDerivation.motionAssignmentSeed(seed)), true);
        field(json, 2, "psf_size_composition_seed", Long.toString(SeedDerivation.psfSizeCompositionSeed(seed)), true);
        field(json, 2, "psf_size_assignment_seed", Long.toString(SeedDerivation.psfSizeAssignmentSeed(seed)), true);
        field(json, 2, "camera_stream_rule", "One stream per frame shared by Poisson sampling and Gaussian read noise in pixel order", true);
        json.append("    \"camera_noise_seeds_by_frame\": [");
        for (int frame = 0; frame < config.getFrames(); frame++) {
            if (frame > 0) { json.append(", "); }
            json.append('"').append(SeedDerivation.cameraNoiseSeed(seed, frame)).append('"');
        }
        json.append("]\n  },\n");
    }

    private void appendParticles(StringBuilder json, RenderedSimulationResult result) {
        long root = result.getPlan().getRequestConfig().getRandomSeed();
        PhotonCalibrationResult calibration = result.getPlan().getPhotonCalibration();
        json.append("  \"particles\": [\n");
        for (int index = 0; index < result.getPlan().getParticles().size(); index++) {
            Particle particle = result.getPlan().getParticles().get(index);
            int id = particle.getParticleId();
            ParticlePsfParameters psf = particle.getImagingParameters().getPsfParameters();
            json.append("    {\n");
            numberField(json, 3, "particle_id", id, true);
            field(json, 3, "track_motion_type", particle.getMotionType().name(), true);
            field(json, 3, "psf_shape", psf.getShapeType().name(), true);
            field(json, 3, "psf_size_class", psf.getSizeClass().name(), true);
            numberField(json, 3, "psf_support_radius_pixels", psf.getRadiusPixels(), true);
            numberField(json, 3, "sigma_x_pixels", psf.getSigmaXPixels(), true);
            numberField(json, 3, "sigma_y_pixels", psf.getSigmaYPixels(), true);
            numberField(json, 3, "rotation_radians", psf.getRotationRadians(), true);
            numberField(json, 3, "sigma_x", psf.getSigmaXPixels(), true);
            numberField(json, 3, "sigma_y", psf.getSigmaYPixels(), true);
            field(json, 3, "sigma_unit", "pixel", true);
            numberField(json, 3, "rotation_angle", psf.getRotationRadians(), true);
            field(json, 3, "rotation_angle_unit", "rad", true);
            numberField(json, 3, "aspect_ratio", psf.getSigmaYPixels() / psf.getSigmaXPixels(), true);
            field(json, 3, "aspect_ratio_definition", "sigma_y / sigma_x in the PSF local axes", true);
            numberField(json, 3, "relative_brightness", calibration.getRelativeBrightnesses().get(index), true);
            numberField(json, 3, "brightness_weight", result.getPlan().getAppearancePlan()
                    .getSamples().get(index).getBrightnessWeight(), true);
            numberField(json, 3, "emitter_photons_per_frame", particle.getImagingParameters().getEmitterPhotonsPerFrame(), true);
            numberField(json, 3, "reference_predicted_snr", calibration.getParticleSnrs().get(index), true);
            field(json, 3, "initial_position_seed", Long.toString(SeedDerivation.initialPositionSeed(root, id)), true);
            field(json, 3, "psf_geometry_seed", Long.toString(SeedDerivation.psfGeometrySeed(root, id)), true);
            field(json, 3, "brightness_seed", Long.toString(SeedDerivation.brightnessSeed(root, id)), true);
            if (particle.getMotionType().isComposite()) {
                field(json, 3, "state_count_seed", Long.toString(SeedDerivation.multiStateCountSeed(root, id)), true);
                field(json, 3, "state_type_seed", Long.toString(SeedDerivation.multiStateTypeSeed(root, id)), true);
                field(json, 3, "change_point_seed", Long.toString(SeedDerivation.multiStateChangePointSeed(root, id)), true);
            }
            json.append("      \"segments\": [\n");
            for (StateSegment segment : particle.getStateSegments()) {
                int state = segment.getStateIndex();
                json.append("        {\n");
                numberField(json, 5, "segment_id", state, true);
                numberField(json, 5, "start_frame", segment.getStartFrame(), true);
                numberField(json, 5, "end_frame", segment.getEndFrame(), true);
                field(json, 5, "motion_type", segment.getMotionType().name(), true);
                field(json, 5, "motion_parameters", MotionParametersFormatter.format(
                        segment.getMotionType(), segment.getMotionParameters()), true);
                field(json, 5, "parameter_seed", Long.toString(particle.getMotionType().isComposite()
                        ? SeedDerivation.multiStateParameterSeed(root, id, state)
                        : SeedDerivation.motionParameterSeed(root, id)), true);
                field(json, 5, "motion_seed", Long.toString(particle.getMotionType().isComposite()
                        ? SeedDerivation.multiStateTrajectorySeed(root, id, state)
                        : SeedDerivation.trajectorySeed(root, id)), false);
                json.append("        }").append(state + 1 < particle.getStateSegments().size() ? ",\n" : "\n");
            }
            json.append("      ]\n    }").append(index + 1 < result.getPlan().getParticles().size() ? ",\n" : "\n");
        }
        json.append("  ],\n");
    }

    private void appendSnrDistribution(
            StringBuilder json,
            PhotonCalibrationResult calibration
    ) {
        json.append("    \"realized_snr_distribution\": {\n");
        numberField(json, 3, "minimum",
                calibration.getMinimumParticleSnr(), true);
        numberField(json, 3, "mean",
                calibration.getMeanParticleSnr(), true);
        numberField(json, 3, "median",
                calibration.getMedianParticleSnr(), true);
        numberField(json, 3, "maximum",
                calibration.getMaximumParticleSnr(), false);
        json.append("    }\n");
    }

    private String advancedPath(String fileName) {
        return DatasetExporter.ADVANCED_DIRECTORY_NAME + "/" + fileName;
    }

    private void appendComposition(
            StringBuilder json,
            RenderedSimulationResult result,
            ModelUsage usage
    ) {
        json.append("  \"motion_composition\": {\n");
        field(json, 2, "generator", "dirichlet", true);
        field(json, 2, "prior", "MotionWeightRegistry", true);
        numberField(json, 2, "concentration",
                result.getPlan().getRequestConfig()
                        .getRandomCompositionConfig().getConcentration(), true);
        json.append("    \"realized_models\": [\n");
        int index = 0;
        Map<MotionType, Double> composition =
                result.getPlan().getMotionComposition().getComposition();
        for (Map.Entry<MotionType, Double> entry : composition.entrySet()) {
            json.append("      {\"type\": \"")
                    .append(escape(entry.getKey().name()))
                    .append("\", \"display_name\": \"")
                    .append(escape(entry.getKey().getDisplayName()))
                    .append("\", \"sampling_weight\": ")
                    .append(Double.toString(entry.getValue()))
                    .append(", \"particle_count\": ")
                    .append(usage.particleCounts.get(entry.getKey()))
                    .append(", \"frame_count\": ")
                    .append(usage.frameCounts.get(entry.getKey()))
                    .append('}');
            index++;
            json.append(index < composition.size() ? ",\n" : "\n");
        }
        json.append("    ]\n");
        json.append("  },\n");
    }

    private void appendMotionDistribution(
            StringBuilder json,
            MotionParameterDistributionConfig config
    ) {
        json.append("  \"motion_parameter_distribution\": {\n");
        rangeField(json, 2, "brownian_D_um2_per_s", "log_uniform",
                config.getBrownianDiffusion(), true);
        rangeField(json, 2, "directed_D_um2_per_s", "log_uniform",
                config.getDirectedDiffusion(), true);
        rangeField(json, 2, "directed_speed_um_per_s", "log_uniform",
                config.getDirectedSpeed(), true);
        rangeField(json, 2, "confined_D_um2_per_s", "log_uniform",
                config.getConfinedDiffusion(), true);
        rangeField(json, 2, "confined_radius_um", "uniform",
                config.getConfinedRadius(), true);
        numberField(json, 2, "confined_substeps_per_frame",
                config.getConfinedSubsteps(), true);
        rangeField(json, 2, "fbm_H", "uniform", config.getFbmHurst(), true);
        rangeField(json, 2, "fbm_K_H_um2_per_s_alpha", "log_uniform",
                config.getFbmGeneralizedDiffusion(), true);
        rangeField(json, 2, "ctrw_alpha", "uniform",
                config.getCtrwAlpha(), true);
        rangeField(json, 2, "ctrw_D_um2_per_s", "log_uniform",
                config.getCtrwDiffusion(), true);
        rangeField(json, 2, "ctrw_tau0_frame_interval_factor", "log_uniform",
                config.getCtrwWaitingTimeFactor(), true);
        rangeField(json, 2, "levy_walk_sigma", "uniform",
                config.getLevyWalkSigma(), true);
        rangeField(json, 2, "levy_walk_speed_um_per_s", "log_uniform",
                config.getLevyWalkSpeed(), true);
        rangeField(json, 2, "levy_walk_tau0_frame_interval_factor", "log_uniform",
                config.getLevyWalkFlightTimeFactor(), true);
        rangeField(json, 2, "sbm_alpha", "uniform",
                config.getSbmAlpha(), true);
        rangeField(json, 2, "sbm_K_alpha_um2_per_s_alpha", "log_uniform",
                config.getSbmGeneralizedDiffusion(), true);
        rangeField(json, 2, "attm_sigma", "uniform",
                config.getAttmSigma(), true);
        rangeField(json, 2, "attm_gamma_minus_sigma", "uniform",
                config.getAttmGammaOffset(), true);
        rangeField(json, 2, "attm_D_max_um2_per_s", "log_uniform",
                config.getAttmMaximumDiffusion(), true);
        rangeField(json, 2, "attm_tau0_frame_interval_factor", "log_uniform",
                config.getAttmDwellTimeFactor(), false);
        json.append("  },\n");
    }

    private void appendAppearanceDistribution(
            StringBuilder json,
            RenderedSimulationResult result
    ) {
        ParticleAppearanceDistributionConfig config = result.getPlan()
                .getRequestConfig().getAppearanceDistributionConfig();
        PsfSizeMixtureConfig mixture = config.getPsfSizeMixtureConfig();
        Map<PsfSizeClass, Integer> counts = psfSizeCounts(result);
        json.append("  \"appearance_distribution\": {\n");
        json.append("    \"psf_size_mixture\": {\n");
        field(json, 3, "generator", "bounded_dirichlet", true);
        json.append("      \"classes\": [\n");
        int classIndex = 0;
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            PsfSizeMixtureConfig.NumericRange sigma =
                    mixture.getSigmaRangePixels(sizeClass);
            PsfSizeMixtureConfig.NumericRange bounds =
                    mixture.getProportionBounds(sizeClass);
            json.append("        {\"size_class\": \"")
                    .append(sizeClass.name())
                    .append("\", \"sigma_distribution\": \"uniform\"")
                    .append(", \"sigma_min_pixels\": ")
                    .append(Double.toString(sigma.getMinimum()))
                    .append(", \"sigma_max_pixels\": ")
                    .append(Double.toString(sigma.getMaximum()))
                    .append(", \"dirichlet_alpha\": ")
                    .append(Double.toString(
                            mixture.getDirichletAlpha(sizeClass)
                    ))
                    .append(", \"proportion_minimum\": ")
                    .append(Double.toString(bounds.getMinimum()))
                    .append(", \"proportion_maximum\": ")
                    .append(Double.toString(bounds.getMaximum()))
                    .append(", \"sampled_ratio\": ")
                    .append(Double.toString(result.getPlan()
                            .getPsfSizeComposition().getRatio(sizeClass)))
                    .append(", \"particle_count\": ")
                    .append(counts.get(sizeClass).intValue())
                    .append('}');
            classIndex++;
            json.append(classIndex < PsfSizeClass.values().length
                    ? ",\n" : "\n");
        }
        json.append("      ]\n");
        json.append("    },\n");
        rangeField(json, 2, "elliptical_aspect_ratio", "uniform",
                range(config.getEllipticalAspectRatioMin(),
                        config.getEllipticalAspectRatioMax()), true);
        numberField(json, 2, "psf_cutoff_sigma",
                config.getPsfCutoffSigma(), true);
        rangeField(json, 2, "uncalibrated_brightness_weight", "log_uniform",
                range(config.getBrightnessWeightMin(),
                        config.getBrightnessWeightMax()), true);
        json.append("    \"size_brightness_correlation\": {\n");
        field(json, 3, "method", "gaussian_copula", true);
        numberField(json, 3, "rho",
                config.getSizeBrightnessCorrelation(), false);
        json.append("    }\n");
        json.append("  },\n");
    }

    private Map<PsfSizeClass, Integer> psfSizeCounts(
            RenderedSimulationResult result
    ) {
        Map<PsfSizeClass, Integer> counts =
                new EnumMap<PsfSizeClass, Integer>(PsfSizeClass.class);
        for (PsfSizeClass sizeClass : PsfSizeClass.values()) {
            counts.put(sizeClass, 0);
        }
        for (Particle particle : result.getPlan().getParticles()) {
            PsfSizeClass sizeClass = particle.getImagingParameters()
                    .getPsfParameters().getSizeClass();
            counts.put(sizeClass, counts.get(sizeClass).intValue() + 1);
        }
        return counts;
    }

    private NumericRange range(double minimum, double maximum) {
        return new NumericRange(minimum, maximum);
    }

    private ModelUsage modelUsage(RenderedSimulationResult result) {
        ModelUsage usage = new ModelUsage();
        for (MotionType type : result.getPlan().getMotionComposition()
                .getComposition().keySet()) {
            usage.particleCounts.put(type, 0);
            usage.frameCounts.put(type, 0);
        }
        for (Particle particle : result.getPlan().getParticles()) {
            MotionType type = particle.getMotionType();
            usage.particleCounts.put(type, usage.particleCounts.get(type) + 1);
            usage.frameCounts.put(
                    type,
                    usage.frameCounts.get(type) + result.getScene().getFrameCount()
            );
        }
        return usage;
    }

    private void rangeField(StringBuilder json, int indentation, String name,
            String distribution, NumericRange range, boolean comma) {
        indent(json, indentation);
        json.append('"').append(escape(name)).append("\": {")
                .append("\"distribution\": \"")
                .append(escape(distribution)).append("\", ")
                .append("\"minimum\": ")
                .append(Double.toString(range.getMinimum())).append(", ")
                .append("\"maximum\": ")
                .append(Double.toString(range.getMaximum())).append('}');
        json.append(comma ? ",\n" : "\n");
    }

    private void field(StringBuilder json, int indentation, String name,
            String value, boolean comma) {
        indent(json, indentation);
        json.append('"').append(escape(name)).append("\": \"")
                .append(escape(value)).append('"');
        json.append(comma ? ",\n" : "\n");
    }

    private void numberField(StringBuilder json, int indentation, String name,
            long value, boolean comma) {
        indent(json, indentation);
        json.append('"').append(escape(name)).append("\": ").append(value);
        json.append(comma ? ",\n" : "\n");
    }

    private void numberField(StringBuilder json, int indentation, String name,
            double value, boolean comma) {
        indent(json, indentation);
        json.append('"').append(escape(name)).append("\": ")
                .append(Double.toString(value));
        json.append(comma ? ",\n" : "\n");
    }

    private void booleanField(
            StringBuilder json,
            int indentation,
            String name,
            boolean value,
            boolean comma
    ) {
        indent(json, indentation);
        json.append('"').append(escape(name)).append("\": ")
                .append(value);
        json.append(comma ? ",\n" : "\n");
    }

    private void indent(StringBuilder json, int level) {
        for (int index = 0; index < level * 2; index++) {
            json.append(' ');
        }
    }

    private String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
            }
        }
        return escaped.toString();
    }

    private static final class ModelUsage {

        private final Map<MotionType, Integer> particleCounts =
                new EnumMap<MotionType, Integer>(MotionType.class);
        private final Map<MotionType, Integer> frameCounts =
                new EnumMap<MotionType, Integer>(MotionType.class);
    }
}
