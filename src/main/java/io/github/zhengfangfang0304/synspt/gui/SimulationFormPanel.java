package io.github.zhengfangfang0304.synspt.gui;

import io.github.zhengfangfang0304.synspt.config.MicroscopeConfig;
import io.github.zhengfangfang0304.synspt.config.NoiseConfig;
import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.motion.MotionType;
import io.github.zhengfangfang0304.synspt.optical.PsfShapeType;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Simulation-only input form; scientific model parameters remain hidden. */
public final class SimulationFormPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JComboBox<ResolutionPreset> resolutionComboBox;
    private final JTextField framesField;
    private final JTextField particleNumberField;
    private final JTextField pixelSizeField;
    private final JTextField frameRateField;
    private final JTextField snrField;
    private final JComboBox<PsfShapeType> spotShapeComboBox;
    private final Map<MotionType, JCheckBox> modelCheckBoxes;

    public SimulationFormPanel() {
        super(new BorderLayout(0, 16));
        resolutionComboBox = new JComboBox<ResolutionPreset>(
                ResolutionPreset.values()
        );
        resolutionComboBox.setSelectedItem(ResolutionPreset.DEFAULT);
        resolutionComboBox.setPrototypeDisplayValue(
                ResolutionPreset.RESOLUTION_1024
        );
        framesField = numericField(SimulationConfig.DEFAULT_FRAMES);
        particleNumberField = numericField(SimulationConfig.DEFAULT_PARTICLE_NUMBER);
        pixelSizeField = decimalField(MicroscopeConfig.DEFAULT_PIXEL_SIZE_UM);
        frameRateField = defaultFrameRateField();
        snrField = decimalField(NoiseConfig.DEFAULT_TARGET_SNR);
        spotShapeComboBox = new JComboBox<PsfShapeType>(PsfShapeType.values());
        spotShapeComboBox.setSelectedItem(SimulationConfig.DEFAULT_SPOT_SHAPE);
        spotShapeComboBox.setPrototypeDisplayValue(
                PsfShapeType.ELLIPTICAL_GAUSSIAN
        );
        modelCheckBoxes = new EnumMap<MotionType, JCheckBox>(MotionType.class);

        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(createSimulationPanel(), BorderLayout.NORTH);
        add(createMotionModelPanel(), BorderLayout.CENTER);
    }

    public SimulationConfig createConfig(long randomSeed) {
        EnumSet<MotionType> selectedMotionTypes =
                EnumSet.noneOf(MotionType.class);
        for (Map.Entry<MotionType, JCheckBox> entry : modelCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedMotionTypes.add(entry.getKey());
            }
        }
        ResolutionPreset resolution = (ResolutionPreset)
                resolutionComboBox.getSelectedItem();
        if (resolution == null) {
            throw new IllegalArgumentException("Resolution must be selected.");
        }
        PsfShapeType spotShape = (PsfShapeType)
                spotShapeComboBox.getSelectedItem();
        if (spotShape == null) {
            throw new IllegalArgumentException("Spot Shape must be selected.");
        }
        MicroscopeConfig defaultMicroscopeConfig =
                MicroscopeConfig.defaultConfig();
        double frameRateFps = parsePositiveDouble(
                frameRateField,
                "Frame Rate"
        );
        double frameIntervalSeconds = frameIntervalFromRate(frameRateFps);
        MicroscopeConfig microscopeConfig = new MicroscopeConfig(
                parsePositiveDouble(pixelSizeField, "Pixel Size"),
                frameIntervalSeconds,
                defaultMicroscopeConfig.getBackgroundPhotons(),
                defaultMicroscopeConfig.getReadNoiseSigmaAdu(),
                defaultMicroscopeConfig.getGainAduPerPhoton(),
                defaultMicroscopeConfig.getOffsetAdu(),
                defaultMicroscopeConfig.getBitDepth()
        );
        return SimulationConfig.builder()
                .imageWidth(resolution.getWidth())
                .imageHeight(resolution.getHeight())
                .frames(parsePositiveInteger(framesField, "Frames"))
                .particleNumber(parsePositiveInteger(
                        particleNumberField,
                        "Particle Number"
                ))
                .microscopeConfig(microscopeConfig)
                .noiseConfig(new NoiseConfig(
                        parsePositiveDouble(snrField, "SNR"),
                        NoiseConfig.defaultConfig().getSnrDefinition()
                ))
                .spotShape(spotShape)
                .selectedMotionTypes(selectedMotionTypes)
                .randomSeed(randomSeed)
                .build();
    }

    public void setSelectedMotionTypes(Set<MotionType> selectedMotionTypes) {
        if (selectedMotionTypes == null) {
            throw new IllegalArgumentException("Selected motion types cannot be null.");
        }
        for (Map.Entry<MotionType, JCheckBox> entry : modelCheckBoxes.entrySet()) {
            entry.getValue().setSelected(selectedMotionTypes.contains(entry.getKey()));
        }
    }

    public void setInputsEnabled(boolean enabled) {
        super.setEnabled(enabled);
        resolutionComboBox.setEnabled(enabled);
        framesField.setEnabled(enabled);
        particleNumberField.setEnabled(enabled);
        pixelSizeField.setEnabled(enabled);
        frameRateField.setEnabled(enabled);
        snrField.setEnabled(enabled);
        spotShapeComboBox.setEnabled(enabled);
        for (JCheckBox checkBox : modelCheckBoxes.values()) {
            checkBox.setEnabled(enabled);
        }
    }

    void setResolutionPreset(ResolutionPreset resolutionPreset) {
        resolutionComboBox.setSelectedItem(resolutionPreset);
    }

    void setFramesText(String value) {
        framesField.setText(value);
    }

    void setPixelSizeText(String value) {
        pixelSizeField.setText(value);
    }

    void setFrameRateText(String value) {
        frameRateField.setText(value);
    }

    void setSnrText(String value) {
        snrField.setText(value);
    }

    void setSpotShape(PsfShapeType shapeType) {
        spotShapeComboBox.setSelectedItem(shapeType);
    }

    private JPanel createSimulationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Simulation"));
        addField(panel, 0, "Resolution", resolutionComboBox);
        addField(panel, 1, "Frames", framesField);
        addField(panel, 2, "Particle Number", particleNumberField);
        addField(panel, 3, "Pixel Size (\u00b5m/pixel)", pixelSizeField);
        addField(panel, 4, "Frame Rate (fps)", frameRateField);
        addField(panel, 5, "SNR", snrField);
        addField(panel, 6, "Spot Shape", spotShapeComboBox);
        return panel;
    }

    private JPanel createMotionModelPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Motion Models"));
        JPanel choices = new JPanel(new GridLayout(0, 2, 20, 12));
        for (MotionType motionType : MotionType.values()) {
            JCheckBox checkBox = new JCheckBox(motionType.getDisplayName());
            checkBox.setSelected(motionType == MotionType.BROWNIAN);
            modelCheckBoxes.put(motionType, checkBox);
            choices.add(checkBox);
        }
        panel.add(choices, BorderLayout.NORTH);
        return panel;
    }

    private void addField(
            JPanel panel,
            int row,
            String labelText,
            JComponent field
    ) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        labelConstraints.insets = new Insets(8, 8, 8, 16);
        JLabel label = new JLabel(labelText + ":");
        label.setLabelFor(field);
        panel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(8, 0, 8, 8);
        panel.add(field, fieldConstraints);
    }

    private JTextField numericField(int defaultValue) {
        return new JTextField(Integer.toString(defaultValue), 10);
    }

    private JTextField decimalField(double defaultValue) {
        return new JTextField(Double.toString(defaultValue), 10);
    }

    private JTextField defaultFrameRateField() {
        double defaultFrameRateFps = 1.0
                / MicroscopeConfig.DEFAULT_FRAME_INTERVAL_SECONDS;
        String text = defaultFrameRateFps == Math.rint(defaultFrameRateFps)
                ? Long.toString((long) defaultFrameRateFps)
                : Double.toString(defaultFrameRateFps);
        return new JTextField(text, 10);
    }

    private double frameIntervalFromRate(double frameRateFps) {
        double frameIntervalSeconds = 1.0 / frameRateFps;
        if (Double.isNaN(frameIntervalSeconds)
                || Double.isInfinite(frameIntervalSeconds)
                || frameIntervalSeconds <= 0.0) {
            throw new IllegalArgumentException(
                    "Frame Rate must produce a finite positive frame interval."
            );
        }
        return frameIntervalSeconds;
    }

    private int parsePositiveInteger(JTextField field, String name) {
        String text = field.getText().trim();
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                throw new NumberFormatException("Not positive");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    name + " must be a positive whole number."
            );
        }
    }

    private double parsePositiveDouble(JTextField field, String name) {
        String text = field.getText().trim();
        try {
            double value = Double.parseDouble(text);
            if (Double.isNaN(value)
                    || Double.isInfinite(value)
                    || value <= 0.0) {
                throw new NumberFormatException("Not finite and positive");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    name + " must be a finite positive number."
            );
        }
    }
}
