package io.github.zhengfangfang0304.particletracking.gui;

import io.github.zhengfangfang0304.particletracking.controller.ParticleTrackingController;
import io.github.zhengfangfang0304.particletracking.io.ImageSequenceImporter;
import io.github.zhengfangfang0304.particletracking.model.Detection;
import io.github.zhengfangfang0304.particletracking.model.Track;
import io.github.zhengfangfang0304.particletracking.preprocessing.GaussianDenoiser;
import io.github.zhengfangfang0304.particletracking.preprocessing.ImageDenoiser;
import io.github.zhengfangfang0304.particletracking.preprocessing.MedianDenoiser;
import io.github.zhengfangfang0304.particletracking.detection.CentroidDetector;
import io.github.zhengfangfang0304.particletracking.detection.DetectionParameters;
import io.github.zhengfangfang0304.particletracking.detection.LocalMaximumDetector;
import io.github.zhengfangfang0304.particletracking.tracking.ParticleTracker;
import io.github.zhengfangfang0304.particletracking.tracking.TrackingParameters;
import io.github.zhengfangfang0304.particletracking.detection.ParticleDetector;
import io.github.zhengfangfang0304.particletracking.tracking.GreedyNearestNeighborTracker;

import io.github.zhengfangfang0304.particletracking.tracking.trackmate.TrackMateNearestNeighborTracker;
import io.github.zhengfangfang0304.particletracking.analysis.DiffusionCoefficientCalculator;
import io.github.zhengfangfang0304.particletracking.analysis.MsdCalculator;
import io.github.zhengfangfang0304.particletracking.analysis.TrackStatisticsCalculator;
import io.github.zhengfangfang0304.particletracking.io.ResultCsvExporter;
import io.github.zhengfangfang0304.particletracking.io.TrackCsvImporter;
import io.github.zhengfangfang0304.particletracking.model.DiffusionCoefficientResult;
import io.github.zhengfangfang0304.particletracking.model.EnsembleMsdResult;
import io.github.zhengfangfang0304.particletracking.model.MsdResult;
import io.github.zhengfangfang0304.particletracking.model.TrackStatistics;


import io.github.zhengfangfang0304.particletracking.simulation.config.ImageSizePreset;
import io.github.zhengfangfang0304.particletracking.simulation.config.ImagingConfig;
import io.github.zhengfangfang0304.particletracking.simulation.config.MotionSelectionConfig;
import io.github.zhengfangfang0304.particletracking.simulation.config.SimulationConfig;
import io.github.zhengfangfang0304.particletracking.simulation.data.SyntheticDataset;
import io.github.zhengfangfang0304.particletracking.simulation.export.SyntheticDatasetExporter;
import io.github.zhengfangfang0304.particletracking.simulation.generators.SyntheticDatasetGenerator;
import io.github.zhengfangfang0304.particletracking.simulation.motion.MotionType;


import ij.WindowManager;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import ij.IJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Box;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Desktop;
import java.awt.Color;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
/**
 * 单颗粒追踪插件的主界面。
 *
 * 这个类负责：
 * 1. 创建窗口；
 * 2. 创建按钮、输入框和下拉框；
 * 3. 接收用户操作；
 * 4. 显示日志和结果。
 *
 * 这个类不负责实现检测和追踪算法。
 */
public final class ParticleTrackingFrame extends JFrame {
    private JComboBox<String> denoiseMethodBox;
    private JTextField denoiseParameterField;

    private JComboBox<String> detectionMethodBox;
    private JComboBox<String> exportTypeBox;
    private JComboBox<String> trackingMethodBox;
    private JCheckBox invertSequenceCheckBox;

    private JTextField detectionThresholdField;
    private JTextField localMaxRadiusField;
    private JTextField minDistanceField;
    private JTextField trackingMaxDistanceField;

    private JComboBox<ImageSizePreset> simulationImageSizeBox;
    private JTextField simulationFramesField;
    private JTextField simulationParticleCountField;
    private JTextField simulationPixelSizeField;
    private JTextField simulationFrameIntervalField;
    private JTextField simulationPsfSigmaField;
    private JTextField simulationNoiseSigmaField;
    private JCheckBox normalDiffusionCheckBox;
    private JCheckBox subdiffusionCheckBox;
    private JCheckBox superdiffusionCheckBox;
    private JCheckBox directedAnomalousDiffusionCheckBox;
    private JCheckBox immobileCheckBox;

    private final List<Detection> lastDetections =
            new ArrayList<>();

    private final List<Track> lastTracks =
            new ArrayList<>();

    private final ParticleTrackingController controller;

    private boolean currentImageFromSimulation =
            false;

    private SyntheticDataset currentSyntheticDataset;

    private SimulationConfig currentSimulationConfig;

    private File currentSyntheticOutputDirectory;

    private final Font chineseFont =
            new Font(
                    "Microsoft YaHei",
                    Font.PLAIN,
                    16
            );

    private final JTextArea logArea =
            new JTextArea();

    

    public ParticleTrackingFrame(
            ParticleTrackingController controller
    ) {
        super("SPTurbo – Single Particle Tracking");

        this.controller =
                Objects.requireNonNull(
                        controller,
                        "Controller不能为null。"
                );
        initializeWindow();
    }

    public static void openFromPlugin(
            ParticleTrackingController controller
    ) {
        SwingUtilities.invokeLater(() -> {
            ParticleTrackingFrame frame =
                    new ParticleTrackingFrame(controller);

            frame.setVisible(true);
        });
    }

    private void initializeWindow() {

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        setSize(1300, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        SimulationConfig defaultSimulationConfig =
                SimulationConfig.defaultConfig();
        ImagingConfig defaultImagingConfig =
                defaultSimulationConfig.getImagingConfig();
        MotionSelectionConfig defaultMotionSelectionConfig =
                defaultSimulationConfig.getMotionSelectionConfig();

        JButton simulationGenerateButton = new JButton("Generate");

        JButton importSequenceButton = new JButton("导入图像序列");

        JButton imageButton = new JButton("检查当前图像");

        JButton importCSVButton = new JButton("导入CSV轨迹");

        JButton denoiseButton = new JButton("执行降噪");

        JButton detectButton = new JButton("识别颗粒");

        JButton trackButton = new JButton("执行追踪");

        JButton analyzeButton = new JButton("轨迹统计");

        JButton msdButton = new JButton("计算 MSD");

        JButton plotMSDButton = new JButton("绘制MSD");

        JButton diffusionButton = new JButton("计算扩散系数D");

        JButton exportButton = new JButton("导出结果");

        JButton closeButton = new JButton("关闭");

        JButton[] buttons =
                {
                        simulationGenerateButton,
                        importSequenceButton,
                        imageButton,
                        importCSVButton,
                        denoiseButton,
                        detectButton,
                        trackButton,
                        analyzeButton,
                        msdButton,
                        plotMSDButton,
                        diffusionButton,
                        exportButton,
                        closeButton
                };

        for (JButton button : buttons) {
            button.setFont(chineseFont);
        }

        invertSequenceCheckBox =
                new JCheckBox(
                        "导入时反相（适用于暗颗粒）",
                        true
                );

        invertSequenceCheckBox.setFont(chineseFont);

        denoiseMethodBox =
                new JComboBox<>(
                        new String[]{
                                "Gaussian Blur 高斯滤波",
                                "Median Filter 中值滤波"
                        }
                );

        denoiseMethodBox.setFont(chineseFont);

        denoiseParameterField =
                new JTextField("1.0", 6);

        denoiseParameterField.setFont(chineseFont);

        detectionMethodBox =
                new JComboBox<>(
                        new String[]{
                                "Local Maximum 局部极大值",
                                "Centroid 质心定位"
                        }
                );

        trackingMethodBox =
                new JComboBox<>(
                        new String[]{
                                "Simple Tracking 自编简单追踪",
                                "TrackMate Nearest Neighbor TrackMate 最近邻"
                        }
                );

        trackingMethodBox.setFont(chineseFont);

        detectionMethodBox.setFont(chineseFont);

        exportTypeBox =
                new JComboBox<>(
                        new String[]{
                                "Track Results 轨迹坐标",
                                "Track Summary 轨迹统计",
                                "MSD Results MSD结果",
                                "Ensemble MSD 总体平均MSD"
                        }
                );

        exportTypeBox.setFont(chineseFont);

        detectionThresholdField =
                new JTextField("80", 6);

        localMaxRadiusField =
                new JTextField("2", 6);

        minDistanceField =
                new JTextField("6", 6);

        trackingMaxDistanceField =
                new JTextField("10", 6);

        simulationImageSizeBox =
                new JComboBox<>(ImageSizePreset.values());
        simulationImageSizeBox.setSelectedItem(
                findImageSizePreset(
                        defaultSimulationConfig.width,
                        defaultSimulationConfig.height
                )
        );
        simulationImageSizeBox.setFont(chineseFont);

        simulationFramesField =
                new JTextField(
                        Integer.toString(
                                defaultSimulationConfig.frames
                        ),
                        6
                );

        simulationParticleCountField =
                new JTextField(
                        Integer.toString(
                                defaultSimulationConfig.particleNumber
                        ),
                        6
                );

        simulationPixelSizeField =
                new JTextField(
                        Double.toString(
                                defaultImagingConfig
                                .getPixelSizeUmPerPixel()
                        ),
                        6
                );

        simulationFrameIntervalField =
                new JTextField(
                        Double.toString(
                                defaultImagingConfig
                                .getFrameIntervalSeconds()
                        ),
                        6
                );

        simulationPsfSigmaField =
                new JTextField(
                        Double.toString(
                                defaultSimulationConfig.psfSigma
                        ),
                        6
                );

        simulationNoiseSigmaField =
                new JTextField(
                        Double.toString(
                                defaultSimulationConfig.noiseSigma
                        ),
                        6
                );

        normalDiffusionCheckBox = motionTypeCheckBox(
                MotionType.NORMAL_DIFFUSION,
                defaultMotionSelectionConfig
                        .getSelectedMotionTypes()
                        .contains(MotionType.NORMAL_DIFFUSION)
        );
        subdiffusionCheckBox = motionTypeCheckBox(
                MotionType.SUBDIFFUSION,
                defaultMotionSelectionConfig
                        .getSelectedMotionTypes()
                        .contains(MotionType.SUBDIFFUSION)
        );
        superdiffusionCheckBox = motionTypeCheckBox(
                MotionType.SUPERDIFFUSION,
                defaultMotionSelectionConfig
                        .getSelectedMotionTypes()
                        .contains(MotionType.SUPERDIFFUSION)
        );
        directedAnomalousDiffusionCheckBox = motionTypeCheckBox(
                MotionType.DIRECTED_ANOMALOUS_DIFFUSION,
                defaultMotionSelectionConfig
                        .getSelectedMotionTypes()
                        .contains(
                                MotionType
                                .DIRECTED_ANOMALOUS_DIFFUSION
                        )
        );
        immobileCheckBox = motionTypeCheckBox(
                MotionType.IMMOBILE,
                defaultMotionSelectionConfig
                        .getSelectedMotionTypes()
                        .contains(MotionType.IMMOBILE)
        );

        JTextField[] textFields =
                {
                        simulationFramesField,
                        simulationParticleCountField,
                        simulationPixelSizeField,
                        simulationFrameIntervalField,
                        simulationPsfSigmaField,
                        simulationNoiseSigmaField,
                        denoiseParameterField,
                        detectionThresholdField,
                        localMaxRadiusField,
                        minDistanceField,
                        trackingMaxDistanceField
                };

        for (JTextField textField : textFields) {
            textField.setFont(chineseFont);
        }

        logArea.setEditable(false);
        logArea.setFont(
                new Font(
                        "Microsoft YaHei",
                        Font.PLAIN,
                        14
                )
        );
       
        simulationGenerateButton.addActionListener(event -> generateSimulationFromMainPanel());

        importSequenceButton.addActionListener(event -> importImageSequence());

        imageButton.addActionListener(event -> readCurrentImage());

        importCSVButton.addActionListener(event -> importTrackingCSV());

        denoiseButton.addActionListener(event -> denoiseCurrentImage());

        detectButton.addActionListener(event -> detectParticles());

      trackButton.addActionListener(event -> runSelectedTrackingMethod());

        analyzeButton.addActionListener(event -> analyzeTracks());

        msdButton.addActionListener(event -> calculateMSD());

        plotMSDButton.addActionListener(event -> plotMSD());

        diffusionButton.addActionListener(event -> calculateDiffusionCoefficient());

        exportButton.addActionListener(event -> exportSelectedResults());

        closeButton.addActionListener(event -> dispose());

        styleNormalButton(importSequenceButton);
        styleNormalButton(importCSVButton);
        styleNormalButton(imageButton);

        stylePrimaryButton(denoiseButton);
        stylePrimaryButton(detectButton);
        stylePrimaryButton(trackButton);

        styleNormalButton(analyzeButton);
        styleNormalButton(msdButton);
        styleNormalButton(plotMSDButton);
        styleNormalButton(diffusionButton);
        stylePrimaryButton(exportButton);
        styleNormalButton(closeButton);
        stylePrimaryButton(simulationGenerateButton);


        makeFullWidth(importSequenceButton);
        makeFullWidth(importCSVButton);
        makeFullWidth(imageButton);
        makeFullWidth(invertSequenceCheckBox);

        makeFullWidth(denoiseButton);
        makeFullWidth(detectButton);
        makeFullWidth(trackButton);
        makeFullWidth(analyzeButton);
        makeFullWidth(msdButton);
        makeFullWidth(plotMSDButton);
        makeFullWidth(diffusionButton);
        makeFullWidth(exportButton);
        makeFullWidth(closeButton);

        JPanel mainPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                5,
                                12,
                                0
                        )
                );

        mainPanel.setBackground(
                new Color(
                        245,
                        247,
                        250
                )
        );

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        14,
                        14,
                        14,
                        14
                )
        );

        JPanel simulationCard =
                createSectionCard(
                        "①",
                        "Simulation"
                );

        JPanel denoiseCard =
                createSectionCard(
                        "②",
                        "Denoise"
                );

        JPanel detectionCard =
                createSectionCard(
                        "③",
                        "Detection"
                );

        JPanel trackingCard =
                createSectionCard(
                        "④",
                        "Tracking"
                );

        JPanel analysisCard =
                createSectionCard(
                        "⑤",
                        "Analysis / Export"
                );

        /*
        * 1. Simulation
        */
        JPanel simulationContent =
                createVerticalContentPanel();

        JLabel essentialSettingsLabel =
                new JLabel("Essential Settings");

        essentialSettingsLabel.setFont(
                new Font("Microsoft YaHei",Font.BOLD,15));

        essentialSettingsLabel.setForeground(
                new Color(35,48,70));

        simulationContent.add(essentialSettingsLabel);

        simulationContent.add(Box.createVerticalStrut(12));

        simulationContent.add(
                createLabelAndComponentRow(
                        "Image Size",
                        simulationImageSizeBox
                )
        );

        simulationContent.add(Box.createVerticalStrut(8));

        simulationContent.add(
                createLabelAndComponentRow(
                        "Frames",
                        simulationFramesField
                )
        );

        simulationContent.add(Box.createVerticalStrut(8));

        simulationContent.add(
                createLabelAndComponentRow(
                        "Particles",
                        simulationParticleCountField
                )
        );

        simulationContent.add(Box.createVerticalStrut(8));

        simulationContent.add(
                createLabelAndComponentRow(
                        "Pixel size μm/pixel",
                        simulationPixelSizeField
                )
        );

        simulationContent.add(Box.createVerticalStrut(8));

        simulationContent.add(
                createLabelAndComponentRow(
                        "Frame interval s/frame",
                        simulationFrameIntervalField
                )
        );

        simulationContent.add(Box.createVerticalStrut(8));

        simulationContent.add(
                createLabelAndComponentRow(
                        "PSF sigma (pixel)",
                        simulationPsfSigmaField
                )
        );

        simulationContent.add(Box.createVerticalStrut(8));

        JPanel motionTypePanel = new JPanel(
                new GridLayout(0, 1, 0, 2)
        );
        motionTypePanel.setBackground(Color.WHITE);
        motionTypePanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Motion Type (select one or more)"
                )
        );
        motionTypePanel.add(normalDiffusionCheckBox);
        motionTypePanel.add(subdiffusionCheckBox);
        motionTypePanel.add(superdiffusionCheckBox);
        motionTypePanel.add(directedAnomalousDiffusionCheckBox);
        motionTypePanel.add(immobileCheckBox);
        simulationContent.add(motionTypePanel);

        simulationContent.add(Box.createVerticalStrut(8));

        simulationContent.add(
                createLabelAndComponentRow(
                        "Noise sigma",
                        simulationNoiseSigmaField
                )
        );


        simulationContent.add(Box.createVerticalGlue());

        JPanel simulationButtonPanel =
                new JPanel(new BorderLayout(12,0));

        simulationButtonPanel.setBackground(Color.WHITE);

        simulationGenerateButton.setPreferredSize(
                new Dimension(
                        130,
                        42
                )
        );

        simulationGenerateButton.setMaximumSize(
                new Dimension(
                        130,
                        42
                )
        );

        simulationButtonPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        45
                )
        );

        simulationButtonPanel.add(
                simulationGenerateButton,
                BorderLayout.EAST
        );

        simulationContent.add(
                Box.createVerticalStrut(18)
        );

        simulationContent.add(
                simulationButtonPanel
        );

        simulationCard.add(simulationContent,BorderLayout.CENTER);

        /*
        * 2. Denoise
        */
        JPanel denoiseContent =
                createVerticalContentPanel();

        denoiseContent.add(
                createLabelAndComponentRow(
                        "Method",
                        denoiseMethodBox
                )
        );

        denoiseContent.add(Box.createVerticalStrut(10));

        denoiseContent.add(
                createLabelAndComponentRow(
                        "Parameter",
                        denoiseParameterField
                )
        );

        denoiseContent.add(Box.createVerticalStrut(16));
        denoiseContent.add(denoiseButton);

        denoiseCard.add(
                denoiseContent,
                BorderLayout.CENTER
        );

        /*
        * 3. Detection
        */
        JPanel detectionContent =
                createVerticalContentPanel();

        detectionContent.add(
                createLabelAndComponentRow(
                        "Method",
                        detectionMethodBox
                )
        );

        detectionContent.add(Box.createVerticalStrut(10));

        detectionContent.add(
                createLabelAndComponentRow(
                        "Threshold",
                        detectionThresholdField
                )
        );

        detectionContent.add(Box.createVerticalStrut(10));

        detectionContent.add(
                createLabelAndComponentRow(
                        "Local Max Radius",
                        localMaxRadiusField
                )
        );

        detectionContent.add(Box.createVerticalStrut(10));

        detectionContent.add(
                createLabelAndComponentRow(
                        "Min Distance",
                        minDistanceField
                )
        );

        detectionContent.add(Box.createVerticalStrut(16));
        detectionContent.add(detectButton);

        detectionCard.add(
                detectionContent,
                BorderLayout.CENTER
        );

        /*
        * 4. Tracking
        */
        JPanel trackingContent =
                createVerticalContentPanel();

        trackingContent.add(
                createLabelAndComponentRow(
                        "Method",
                        trackingMethodBox
                )
        );

        trackingContent.add(
                Box.createVerticalStrut(10)
        );

        trackingContent.add(
                createLabelAndComponentRow(
                        "Max Distance",
                        trackingMaxDistanceField
                )
        );

        trackingContent.add(
                Box.createVerticalStrut(16)
        );

        trackingContent.add(trackButton);

        trackingCard.add(
                trackingContent,
                BorderLayout.CENTER
        );



        /*
        * 5. Analysis / Export
        */
        JPanel analysisContent =
                createVerticalContentPanel();

        analysisContent.add(analyzeButton);
        analysisContent.add(Box.createVerticalStrut(10));
        analysisContent.add(msdButton);
        analysisContent.add(Box.createVerticalStrut(10));
        analysisContent.add(plotMSDButton);
        analysisContent.add(Box.createVerticalStrut(10));
        analysisContent.add(diffusionButton);

        analysisContent.add(Box.createVerticalStrut(16));

        analysisContent.add(
                createLabelAndComponentRow(
                        "Export Type",
                        exportTypeBox
                )
        );

        analysisContent.add(Box.createVerticalStrut(16));
        analysisContent.add(exportButton);
        analysisContent.add(Box.createVerticalStrut(10));
        analysisContent.add(closeButton);

        analysisCard.add(
                analysisContent,
                BorderLayout.CENTER
        );

        mainPanel.add(simulationCard);
        mainPanel.add(denoiseCard);
        mainPanel.add(detectionCard);
        mainPanel.add(trackingCard);
        mainPanel.add(analysisCard);

        JScrollPane mainScrollPane =
                new JScrollPane(mainPanel);

        mainScrollPane.setBorder(null);

        JScrollPane logScrollPane =
                new JScrollPane(logArea);

        logScrollPane.setPreferredSize(
                new Dimension(
                        1300,
                        120
                )
        );

        logScrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Log"
                )
        );

        getContentPane().setBackground(
                new Color(
                        245,
                        247,
                        250
                )
        );

        add(mainScrollPane, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);
    }

    private JPanel createSectionCard(String number, String titleText) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235), 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel title = new JLabel(number + "  " + titleText);
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 17));
        title.setForeground(new Color(20, 105, 210));

        card.add(title, BorderLayout.NORTH);

        return card;
    }

    private JPanel createVerticalContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JPanel createLabelAndComponentRow(String labelText, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel label = new JLabel(labelText);
        label.setFont(chineseFont);
        label.setForeground(new Color(35, 48, 70));

        component.setFont(chineseFont);

        row.add(label, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);

        return row;
    }

    private JCheckBox motionTypeCheckBox(
            MotionType motionType,
            boolean selected
    ) {
        JCheckBox checkBox = new JCheckBox(
                motionType.getDisplayName(),
                selected
        );
        checkBox.setBackground(Color.WHITE);
        checkBox.setFont(chineseFont);
        return checkBox;
    }

    private ImageSizePreset findImageSizePreset(
            int width,
            int height
    ) {
        for (ImageSizePreset preset : ImageSizePreset.values()) {
            if (preset.getWidth() == width
                    && preset.getHeight() == height) {
                return preset;
            }
        }
        throw new IllegalStateException(
                "Default image size must match an image size preset."
        );
    }

    private void addIfSelected(
            EnumSet<MotionType> selectedTypes,
            MotionType motionType,
            JCheckBox checkBox
    ) {
        if (checkBox.isSelected()) {
            selectedTypes.add(motionType);
        }
    }

    private void validateRange(
            String parameterName,
            double value,
            double minimum,
            double maximum
    ) {
        if (!Double.isFinite(value)
                || value < minimum
                || value > maximum) {
            throw new IllegalArgumentException(
                    parameterName
                            + " must be between "
                            + minimum
                            + " and "
                            + maximum
                            + "."
            );
        }
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(chineseFont);
        button.setFocusPainted(false);
        button.setBackground(new Color(25, 118, 210));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private void styleNormalButton(JButton button) {
        button.setFont(chineseFont);
        button.setFocusPainted(false);
        button.setBackground(new Color(245, 248, 252));
        button.setForeground(new Color(30, 45, 70));
    }

    private void makeFullWidth(JComponent component) {
        component.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        component.getPreferredSize().height
                )
        );

        component.setAlignmentX(
                JComponent.LEFT_ALIGNMENT
        );
    }
    
    private void readCurrentImage() {
        try {
            ImagePlus image =
                    IJ.getImage();

            logArea.append(
                    "当前图像："
                            + image.getTitle()
                            + "\n"
            );

            logArea.append(
                    "宽度："
                            + image.getWidth()
                            + "\n"
            );

            logArea.append(
                    "高度："
                            + image.getHeight()
                            + "\n"
            );

            logArea.append(
                    "切片/帧数："
                            + image.getStackSize()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "请先在 Fiji/ImageJ 中打开一张图像。\n\n"
            );
        }
    }
    private void clearSyntheticDatasetContext() {
        currentImageFromSimulation =
                false;

        currentSyntheticDataset =
                null;

        currentSimulationConfig =
                null;

        currentSyntheticOutputDirectory =
                null;
    }

    private SimulationConfig buildSimulationConfigFromMainPanel() {
        SimulationConfig config =
                SimulationConfig.defaultConfig();

        ImageSizePreset imageSizePreset =
                (ImageSizePreset) simulationImageSizeBox
                        .getSelectedItem();
        if (imageSizePreset == null) {
            throw new IllegalArgumentException(
                    "Image size preset must be selected."
            );
        }
        config.width = imageSizePreset.getWidth();
        config.height = imageSizePreset.getHeight();

        config.frames =
                Integer.parseInt(
                        simulationFramesField.getText()
                                .trim()
                );

        config.particleNumber =
                Integer.parseInt(
                        simulationParticleCountField.getText()
                                .trim()
                );

        double pixelSizeUmPerPixel = Double.parseDouble(
                simulationPixelSizeField.getText().trim()
        );
        double frameIntervalSeconds = Double.parseDouble(
                simulationFrameIntervalField.getText().trim()
        );
        config.setImagingConfig(
                new ImagingConfig(
                        pixelSizeUmPerPixel,
                        frameIntervalSeconds
                )
        );

        config.psfSigma =
                Double.parseDouble(
                        simulationPsfSigmaField.getText()
                                .trim()
                );

        config.noiseSigma =
                Double.parseDouble(
                        simulationNoiseSigmaField.getText()
                                .trim()
                );

        config.setMotionSelectionConfig(
                buildMotionSelectionConfigFromMainPanel()
        );

        validateRange(
                "Pixel size",
                pixelSizeUmPerPixel,
                0.001,
                100.0
        );
        validateRange(
                "Frame interval",
                frameIntervalSeconds,
                0.000001,
                3600.0
        );
        config.validate();
        return config;
    }

    private MotionSelectionConfig
            buildMotionSelectionConfigFromMainPanel() {
        EnumSet<MotionType> selectedTypes =
                EnumSet.noneOf(MotionType.class);
        addIfSelected(
                selectedTypes,
                MotionType.NORMAL_DIFFUSION,
                normalDiffusionCheckBox
        );
        addIfSelected(
                selectedTypes,
                MotionType.SUBDIFFUSION,
                subdiffusionCheckBox
        );
        addIfSelected(
                selectedTypes,
                MotionType.SUPERDIFFUSION,
                superdiffusionCheckBox
        );
        addIfSelected(
                selectedTypes,
                MotionType.DIRECTED_ANOMALOUS_DIFFUSION,
                directedAnomalousDiffusionCheckBox
        );
        addIfSelected(
                selectedTypes,
                MotionType.IMMOBILE,
                immobileCheckBox
        );
        return new MotionSelectionConfig(selectedTypes);
    }

    private void generateSimulationFromMainPanel() {
        try {
            SimulationConfig config =
                    buildSimulationConfigFromMainPanel();

            generateAndExportSimulation(config);
        } catch (NumberFormatException ex) {
            logArea.append(
                    "Simulation 参数输入错误，请检查所有数值字段。\n\n"
            );
        } catch (IllegalArgumentException ex) {
            logArea.append(
                    "Simulation 参数无效: "
                            + ex.getMessage()
                            + "\n\n"
            );
        }
    }

    private void generateAndExportSimulation(
            SimulationConfig config
    ) {
        if (config == null) {
            logArea.append("模拟数据参数为空，取消生成。\n\n");
            return;
        }

        File outputDirectory =
                chooseSimulationOutputDirectory();

        if (outputDirectory == null) {
            logArea.append("已取消选择模拟数据输出文件夹。\n\n");
            return;
        }

        try {
            SyntheticDatasetGenerator generator =
                    new SyntheticDatasetGenerator();
            long masterSeed = System.nanoTime();
            SyntheticDataset dataset = generator.generate(
                    config,
                    masterSeed
            );
            ImagePlus image = dataset.getImage();

            SyntheticDatasetExporter.export(
                    dataset,
                    outputDirectory
            );

            image.show();
            lastDetections.clear();
            lastTracks.clear();
            controller.clearSession();
            setSyntheticDatasetContext(
                    dataset,
                    config,
                    outputDirectory
            );

            logArea.append(
                    "模拟数据生成完成。\n"
                            + "图像尺寸: "
                            + config.width
                            + " × "
                            + config.height
                            + "\n"
                            + "帧数: "
                            + config.frames
                            + "\n"
                            + "粒子数: "
                            + config.particleNumber
                            + "\n"
                            + "运动类型: "
                            + config.getMotionSelectionConfig()
                            .getSelectedMotionTypes()
                            + "\n"
                            + "Master seed: "
                            + masterSeed
                            + "\n"
                            + "Ground truth 点数: "
                            + getGroundTruthPointCount(dataset)
                            + "\n"
                            + "已保存: "
                            + new File(
                                    outputDirectory,
                                    "simulation.tif"
                            ).getAbsolutePath()
                            + "\n"
                            + "已保存: "
                            + new File(
                                    outputDirectory,
                                    "ground_truth.csv"
                            ).getAbsolutePath()
                            + "\n"
                            + "已保存: "
                            + new File(
                                    outputDirectory,
                                    "particle_motion_profiles.csv"
                            ).getAbsolutePath()
                            + "\n\n"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Synthetic dataset generated successfully.\n\n"
                            + outputDirectory.getAbsolutePath(),
                    "Generation complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            logArea.append(
                    "模拟数据生成失败: "
                            + ex.getMessage()
                            + "\n\n"
            );
            ex.printStackTrace();
        }
    }

    private File chooseSimulationOutputDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(
                "选择 simulation 数据集的保存文件夹"
        );
        chooser.setFileSelectionMode(
                JFileChooser.DIRECTORIES_ONLY
        );
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showDialog(this, "Generate");

        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser.getSelectedFile();
    }

    private int getGroundTruthPointCount(
            SyntheticDataset dataset
    ) {
        return dataset.getParticles()
                .stream()
                .mapToInt(
                        particle ->
                                particle.getTrajectory().size()
                )
                .sum();
    }

    private void setSyntheticDatasetContext(
            SyntheticDataset dataset,
            SimulationConfig config,
            File outputDirectory
    ) {
        currentImageFromSimulation =
                true;

        currentSyntheticDataset =
                dataset;

        currentSimulationConfig =
                config;

        currentSyntheticOutputDirectory =
                outputDirectory;
    }

    private void appendSyntheticDatasetContextLog() {
        if (!currentImageFromSimulation) {
            logArea.append(
                    "当前图像来源：外部图像序列或普通图像。\n"
            );

            logArea.append(
                    "Ground truth 状态：无。\n\n"
            );

            return;
        }

        logArea.append(
                "当前图像来源：模拟数据生成器。\n"
        );

        if (currentSyntheticDataset != null) {
            logArea.append(
                    "Ground truth 状态：已载入内存。\n"
            );

            logArea.append(
                    "真实检测点数量："
                            + getGroundTruthPointCount(
                                    currentSyntheticDataset
                            )
                            + "\n"
            );
        } else {
            logArea.append(
                    "Ground truth 状态：无。\n"
            );
        }

        if (currentSyntheticOutputDirectory != null) {
            logArea.append(
                    "模拟数据保存路径："
                            + currentSyntheticOutputDirectory
                                    .getAbsolutePath()
                            + "\n"
            );
        } else {
            logArea.append(
                    "模拟数据保存路径：未保存到文件夹\n"
            );
        }

        logArea.append("\n");
    }

    private void loadGeneratedDatasetForAnalysis(
            ImagePlus image,
            SyntheticDataset dataset,
            SimulationConfig config,
            File outputDirectory
    ) {
        if (image == null) {
            logArea.append(
                    "生成图像为空，无法进入普通分析页面。\n\n"
            );

            return;
        }

        if (image.getWindow() == null) {
            image.show();
        }

        lastDetections.clear();
        lastTracks.clear();
        controller.clearSession();

        setSyntheticDatasetContext(
                dataset,
                config,
                outputDirectory
        );

        setVisible(true);
        toFront();

        logArea.append(
                "已进入普通分析页面，并载入刚生成的模拟数据。\n"
        );

        logArea.append(
                "图像名称："
                        + image.getTitle()
                        + "\n"
        );

        logArea.append(
                "图像尺寸："
                        + image.getWidth()
                        + " × "
                        + image.getHeight()
                        + "\n"
        );

        logArea.append(
                "帧数："
                        + image.getStackSize()
                        + "\n"
        );

        logArea.append(
                "粒子数："
                        + config.particleNumber
                        + "\n"
        );

        logArea.append(
                "真实 ground truth 点数："
                        + getGroundTruthPointCount(dataset)
                        + "\n"
        );

        if (outputDirectory != null) {
            logArea.append(
                    "模拟数据保存路径："
                            + outputDirectory.getAbsolutePath()
                            + "\n"
            );
        }

        appendSyntheticDatasetContextLog();

        logArea.append(
                "\n下一步建议：\n"
                        + "1. 点击“检查当前图像”确认当前图像。\n"
                        + "2. 点击“执行降噪”，可选。\n"
                        + "3. 点击“识别颗粒”。\n"
                        + "4. 在 Tracking 的 Method 中选择追踪方法，然后点击“执行追踪”。\n"
                        + "5. 点击“轨迹统计 / 计算 MSD / 计算扩散系数D”。\n\n"
        );
    }

    private void openOutputDirectory(
            File outputDirectory
    ) {
        if (outputDirectory == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "当前数据没有保存到文件夹，因此无法打开输出文件夹。",
                    "没有输出文件夹",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!outputDirectory.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "输出文件夹不存在：\n"
                            + outputDirectory.getAbsolutePath(),
                    "文件夹不存在",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(
                    this,
                    "当前系统不支持自动打开文件夹。\n\n"
                            + "请手动打开：\n"
                            + outputDirectory.getAbsolutePath(),
                    "无法自动打开",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {
            Desktop.getDesktop().open(
                    outputDirectory
            );

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "打开输出文件夹失败：\n"
                            + ex.getMessage()
                            + "\n\n请手动打开：\n"
                            + outputDirectory.getAbsolutePath(),
                    "打开失败",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showPostGenerationOptions(
            ImagePlus image,
            SyntheticDataset dataset,
            SimulationConfig config,
            File outputDirectory
    ) {
        String saveInfo;

        if (outputDirectory == null) {
            saveInfo =
                    "未保存到文件夹";
        } else {
            saveInfo =
                    outputDirectory.getAbsolutePath();
        }

        int choice =
                JOptionPane.showOptionDialog(
                        this,
                        "模拟数据已经生成完成。\n\n"
                                + "图像尺寸："
                                + config.width
                                + " × "
                                + config.height
                                + "\n"
                                + "帧数："
                                + config.frames
                                + "\n"
                                + "粒子数："
                                + config.particleNumber
                                + "\n"
                                + "真实点数："
                                + getGroundTruthPointCount(dataset)
                                + "\n"
                                + "保存路径："
                                + saveInfo
                                + "\n\n"
                                + "请选择下一步操作：",
                        "模拟数据生成完成",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        new String[]{
                                "进入普通分析页面",
                                "继续设计新的模拟数据",
                                "打开输出文件夹",
                                "关闭"
                        },
                        "进入普通分析页面"
                );

        if (choice == 0) {
            loadGeneratedDatasetForAnalysis(
                    image,
                    dataset,
                    config,
                    outputDirectory
            );

            return;
        }

        if (choice == 1) {
            return;
        }

        if (choice == 2) {
            openOutputDirectory(
                    outputDirectory
            );

            showPostGenerationOptions(
                    image,
                    dataset,
                    config,
                    outputDirectory
            );

            return;
        }

        dispose();
    }

    private void importImageSequence() {
        try {
            DirectoryChooser directoryChooser =
                    new DirectoryChooser(
                            "选择图像序列文件夹"
                    );

            String directory =
                    directoryChooser.getDirectory();

            if (directory == null) {
                logArea.append(
                        "已取消导入图像序列。\n\n"
                );
                return;
            }

            boolean invertImage =
                    invertSequenceCheckBox.isSelected();

            ImageSequenceImporter.ImportResult importResult =
                    ImageSequenceImporter.importFromDirectory(
                            directory,
                            invertImage
                    );

            ImagePlus preparedSequence =
                    importResult.image();

            preparedSequence.show();

            lastDetections.clear();
            lastTracks.clear();
            controller.clearSession();

            clearSyntheticDatasetContext();

            logArea.append("图像序列导入完成。\n");

            logArea.append(
                    "当前图像来源：外部图像序列。\n"
                            + "Ground truth 状态：无。\n\n"
            );

            logArea.append(
                    "文件夹："
                            + importResult.directory()
                            + "\n"
            );

            logArea.append(
                    "图像名称："
                            + preparedSequence.getTitle()
                            + "\n"
            );

            logArea.append(
                    "宽度："
                            + importResult.width()
                            + "\n"
            );

            logArea.append(
                    "高度："
                            + importResult.height()
                            + "\n"
            );

            logArea.append(
                    "帧数："
                            + importResult.frames()
                            + "\n"
            );

            logArea.append(
                    "导入时反相："
                            + (importResult.inverted() ? "是" : "否")
                            + "\n"
            );

            logArea.append(
                    "之前的识别和追踪结果已清空。\n"
            );

            logArea.append(
                    "下一步可点击：检查当前图像 → 执行降噪 → 识别颗粒。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "图像序列导入参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "图像序列导入失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void denoiseCurrentImage() {
        try {
            ImagePlus original =
                    IJ.getImage();

            if (original == null) {
                logArea.append(
                        "没有检测到当前图像，请先打开或生成一张图像。\n\n"
                );
                return;
            }

            String method =
                    (String) denoiseMethodBox.getSelectedItem();

            double parameter =
                    Double.parseDouble(
                            denoiseParameterField.getText()
                    );

            ImageDenoiser denoiser;

            if (method != null
                    && method.contains("Median")) {

                denoiser =
                        new MedianDenoiser();

            } else {

                denoiser =
                        new GaussianDenoiser();
            }

            ImagePlus denoised =
                    denoiser.denoise(
                            original,
                            parameter
                    );

            denoised.show();

            lastDetections.clear();
            lastTracks.clear();
            controller.clearSession();

            logArea.append("降噪完成。\n");

            logArea.append(
                    "原始图像："
                            + original.getTitle()
                            + "\n"
            );

            logArea.append(
                    "降噪方法："
                            + denoiser.getName()
                            + "\n"
            );

            logArea.append(
                    "参数："
                            + parameter
                            + "\n"
            );

            logArea.append(
                    "处理帧数："
                            + denoised.getStackSize()
                            + "\n"
            );

            logArea.append(
                    "已生成新图像："
                            + denoised.getTitle()
                            + "\n\n"
            );

        } catch (NumberFormatException ex) {

            logArea.append(
                    "降噪参数输入错误，请输入数字，例如 1.0 或 2.0。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "降噪参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "降噪失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void detectParticles() {
        try {
            ImagePlus image =
                    IJ.getImage();

            if (image == null) {
                logArea.append(
                        "没有检测到当前图像，请先打开、生成或导入一张图像。\n\n"
                );
                return;
            }

            double threshold =
                    Double.parseDouble(
                            detectionThresholdField.getText()
                    );

            int localMaximumRadius =
                    Integer.parseInt(
                            localMaxRadiusField.getText()
                    );

            double minimumDistance =
                    Double.parseDouble(
                            minDistanceField.getText()
                    );

            DetectionParameters parameters =
                    new DetectionParameters(
                            threshold,
                            localMaximumRadius,
                            minimumDistance
                    );

            String method =
                    (String) detectionMethodBox.getSelectedItem();

            ParticleDetector detector;

            if (method != null
                    && method.contains("Centroid")) {

                detector =
                        new CentroidDetector();

            } else {

                detector =
                        new LocalMaximumDetector();
            }

            List<Detection> detections =
                    controller.detect(
                            image,
                            detector,
                            parameters
                    );

            lastDetections.clear();
            lastDetections.addAll(
                    detections
            );

            lastTracks.clear();

            ResultTablePresenter.showDetections(
                    image,
                    lastDetections,
                    "Particle Detections"
            );

            logArea.append("颗粒识别完成。\n");

            logArea.append(
                    "识别方法："
                            + detector.getName()
                            + "\n"
            );

            logArea.append(
                    "识别阈值："
                            + threshold
                            + "\n"
            );

            logArea.append(
                    "局部极大半径："
                            + localMaximumRadius
                            + "\n"
            );

            logArea.append(
                    "最小距离："
                            + minimumDistance
                            + "\n"
            );

            logArea.append(
                    "识别到颗粒数量："
                            + lastDetections.size()
                            + "\n"
            );

            logArea.append(
                    "结果已显示在 Particle Detections 表格中。\n\n"
            );

        } catch (NumberFormatException ex) {

            logArea.append(
                    "识别参数输入错误，请检查阈值、半径和最小距离是否为数字。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "识别参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "颗粒识别失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void trackParticles() {
        try {
            if (lastDetections.isEmpty()) {
                logArea.append(
                        "还没有颗粒识别结果，请先点击“识别颗粒”。\n\n"
                );
                return;
            }

            ImagePlus image =
                    IJ.getImage();

            if (image == null) {
                logArea.append(
                        "没有检测到当前图像。\n\n"
                );
                return;
            }

            double maximumLinkingDistance =
                    Double.parseDouble(
                            trackingMaxDistanceField.getText()
                    );

            TrackingParameters parameters =
                    new TrackingParameters(
                            maximumLinkingDistance,
                            0
                    );
//最大连接距离 = 用户输入的像素距离
//最大跨帧间隔 = 0，也就是只连接相邻帧
            ParticleTracker tracker =
                    new GreedyNearestNeighborTracker();

            List<Track> tracks =
                    controller.track(
                            tracker,
                            parameters
                    );

            lastTracks.clear();
            lastTracks.addAll(
                    tracks
            );

            ResultTablePresenter.showTrackingResults(
                    image,
                    lastDetections,
                    lastTracks,
                    "Track Results"
            );

            logArea.append("简单追踪完成。\n");

            logArea.append(
                    "追踪器："
                            + tracker.getName()
                            + "\n"
            );

            logArea.append(
                    "追踪最大距离："
                            + maximumLinkingDistance
                            + " pixel\n"
            );

            logArea.append(
                    "最大间隔帧数：0\n"
            );

            logArea.append(
                    "生成轨迹数量："
                            + lastTracks.size()
                            + "\n"
            );

            logArea.append(
                    "结果已显示在 Track Results 表格中。\n\n"
            );

        } catch (NumberFormatException ex) {

            logArea.append(
                    "追踪参数输入错误，请输入数字，例如 10。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "追踪参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "追踪失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void runSelectedTrackingMethod() {
        String selectedMethod =
                (String) trackingMethodBox.getSelectedItem();

        if (selectedMethod == null) {
            logArea.append(
                    "请选择追踪方法。\n\n"
            );

            return;
        }

        if (selectedMethod.contains("TrackMate")) {
            trackParticlesWithTrackMate();

            return;
        }

        trackParticles();
    }

    private void trackParticlesWithTrackMate() {
        try {
            if (lastDetections.isEmpty()) {
                logArea.append(
                        "还没有颗粒识别结果，请先点击“识别颗粒”。\n\n"
                );
                return;
            }

            ImagePlus image =
                    IJ.getImage();

            if (image == null) {
                logArea.append(
                        "没有检测到当前图像。\n\n"
                );
                return;
            }

            double maximumLinkingDistance =
                    Double.parseDouble(
                            trackingMaxDistanceField.getText()
                    );

            TrackingParameters parameters =
                    new TrackingParameters(
                            maximumLinkingDistance,
                            0
                    );

            ParticleTracker tracker =
                    new TrackMateNearestNeighborTracker();

            List<Track> tracks =
                    controller.track(
                            tracker,
                            parameters
                    );

            lastTracks.clear();
            lastTracks.addAll(
                    tracks
            );

            ResultTablePresenter.showTrackingResults(
                    image,
                    lastDetections,
                    lastTracks,
                    "TrackMate NN Results"
            );

            logArea.append("TrackMate 最近邻追踪完成。\n");

            logArea.append(
                    "追踪器："
                            + tracker.getName()
                            + "\n"
            );

            logArea.append(
                    "追踪最大距离："
                            + maximumLinkingDistance
                            + " pixel\n"
            );

            logArea.append(
                    "生成轨迹数量："
                            + lastTracks.size()
                            + "\n"
            );

            logArea.append(
                    "结果已显示在 TrackMate NN Results 表格中。\n\n"
            );

        } catch (NumberFormatException ex) {

            logArea.append(
                    "TrackMate追踪参数输入错误，请输入数字，例如 10。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "TrackMate追踪参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "TrackMate追踪失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void importTrackingCSV() {
        JFileChooser fileChooser =
                new JFileChooser();

        fileChooser.setDialogTitle(
                "选择外部追踪 CSV 文件"
        );

        int userSelection =
                fileChooser.showOpenDialog(this);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            logArea.append("已取消导入。\n\n");
            return;
        }

        File csvFile =
                fileChooser.getSelectedFile();

        try {
            TrackCsvImporter.ImportResult importResult =
                    TrackCsvImporter.importFrom(
                            csvFile
                    );

            lastDetections.clear();
            lastDetections.addAll(
                    importResult.detections()
            );

            lastTracks.clear();
            lastTracks.addAll(
                    importResult.tracks()
            );

            ImagePlus currentImage =
                    WindowManager.getCurrentImage();

            controller.loadImportedResults(
                    currentImage,
                    importResult.detections(),
                    importResult.tracks()
            );

            ResultTablePresenter.showImportedTrackResults(
                    lastTracks
            );

            logArea.append("外部追踪 CSV 导入完成。\n");
            logArea.append(
                    "文件："
                            + csvFile.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "有效数据行数："
                            + importResult.importedRows()
                            + "\n"
            );
            logArea.append(
                    "particle 数量："
                            + lastTracks.size()
                            + "\n"
            );
            logArea.append(
                    "坐标点数量："
                            + lastDetections.size()
                            + "\n"
            );

            if (!importResult.intensityColumnFound()) {
                logArea.append(
                        "未找到 mass、signal 或 intensity 列，强度统一记为 0。\n"
                );
            }

            logArea.append(
                    "现在可以直接点击：轨迹统计、计算 MSD、绘制MSD、计算D或导出结果。\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "CSV格式错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "CSV 导入失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void analyzeTracks() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先在 Tracking 的 Method 中选择追踪方法，并点击“执行追踪”。\n\n"
                );
                return;
            }

            List<TrackStatistics> statisticsList =
                    TrackStatisticsCalculator.calculate(
                            lastTracks
                    );

            ResultsTable table =
                    new ResultsTable();

            for (TrackStatistics statistics : statisticsList) {

                table.incrementCounter();

                table.addValue(
                        "particle",
                        statistics.trackId()
                );

                table.addValue(
                        "Start_Frame",
                        statistics.startFrame()
                );

                table.addValue(
                        "End_Frame",
                        statistics.endFrame()
                );

                table.addValue(
                        "N_Points",
                        statistics.numberOfPoints()
                );

                table.addValue(
                        "Duration_Frames",
                        statistics.durationFrames()
                );

                table.addValue(
                        "Start_X",
                        statistics.startX()
                );

                table.addValue(
                        "Start_Y",
                        statistics.startY()
                );

                table.addValue(
                        "End_X",
                        statistics.endX()
                );

                table.addValue(
                        "End_Y",
                        statistics.endY()
                );

                table.addValue(
                        "Displacement",
                        statistics.displacement()
                );

                table.addValue(
                        "Path_Length",
                        statistics.pathLength()
                );

                table.addValue(
                        "Mean_Step",
                        statistics.meanStep()
                );

                table.addValue(
                        "Mean_Speed_px_per_frame",
                        statistics.meanSpeed()
                );

                table.addValue(
                        "Mean_Intensity",
                        statistics.meanIntensity()
                );
            }

            table.show(
                    "Track Summary"
            );

            logArea.append("轨迹统计完成。\n");
            logArea.append(
                    "输入轨迹数量："
                            + lastTracks.size()
                            + "\n"
            );
            logArea.append(
                    "输出统计行数："
                            + statisticsList.size()
                            + "\n"
            );
            logArea.append(
                    "结果已显示在 Track Summary 表格中。\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "轨迹统计失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void calculateMSD() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先在 Tracking 的 Method 中选择追踪方法，并点击“执行追踪”。\n\n"
                );
                return;
            }

            List<MsdResult> msdResults =
                    MsdCalculator.calculatePerTrack(
                            lastTracks
                    );

            List<EnsembleMsdResult> ensembleResults =
                    MsdCalculator.calculateEnsemble(
                            lastTracks
                    );

            ResultsTable msdTable =
                    new ResultsTable();

            for (MsdResult result : msdResults) {

                msdTable.incrementCounter();

                msdTable.addValue(
                        "particle",
                        result.trackId()
                );

                msdTable.addValue(
                        "Lag_Frames",
                        result.lagFrames()
                );

                msdTable.addValue(
                        "MSD_px2",
                        result.msd()
                );

                msdTable.addValue(
                        "N_Pairs",
                        result.pairCount()
                );
            }

            ResultsTable ensembleTable =
                    new ResultsTable();

            for (EnsembleMsdResult result : ensembleResults) {

                ensembleTable.incrementCounter();

                ensembleTable.addValue(
                        "Lag_Frames",
                        result.lagFrames()
                );

                ensembleTable.addValue(
                        "Ensemble_MSD_px2",
                        result.ensembleMsd()
                );

                ensembleTable.addValue(
                        "N_Pairs",
                        result.pairCount()
                );
            }

            msdTable.show("MSD Results");
            ensembleTable.show("MSD Ensemble");

            logArea.append("MSD 计算完成。\n");
            logArea.append(
                    "单轨迹MSD结果行数："
                            + msdResults.size()
                            + "\n"
            );
            logArea.append(
                    "Ensemble MSD结果行数："
                            + ensembleResults.size()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "MSD 计算失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void plotMSD() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先在 Tracking 的 Method 中选择追踪方法，并点击“执行追踪”。\n\n"
                );
                return;
            }

            List<EnsembleMsdResult> ensembleResults =
                    MsdCalculator.calculateEnsemble(
                            lastTracks
                    );

            if (ensembleResults.isEmpty()) {
                logArea.append(
                        "没有足够的轨迹点绘制MSD曲线。\n\n"
                );
                return;
            }

            double[] x =
                    new double[ensembleResults.size()];

            double[] y =
                    new double[ensembleResults.size()];

            for (int i = 0;
                i < ensembleResults.size();
                i++) {

                EnsembleMsdResult result =
                        ensembleResults.get(i);

                x[i] =
                        result.lagFrames();

                y[i] =
                        result.ensembleMsd();
            }

            Plot plot =
                    new Plot(
                            "Ensemble MSD Curve",
                            "Lag Frames",
                            "MSD px^2"
                    );

            plot.addPoints(
                    x,
                    y,
                    Plot.CONNECTED_CIRCLES
            );

            plot.show();

            logArea.append("MSD曲线绘制完成。\n");
            logArea.append(
                    "数据点数量："
                            + ensembleResults.size()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "MSD绘图失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void calculateDiffusionCoefficient() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先在 Tracking 的 Method 中选择追踪方法，并点击“执行追踪”。\n\n"
                );
                return;
            }

            DiffusionCoefficientResult result =
                    DiffusionCoefficientCalculator.calculateFromTracks(
                            lastTracks,
                            5
                    );

            ResultsTable diffusionTable =
                    new ResultsTable();

            diffusionTable.incrementCounter();

            diffusionTable.addValue(
                    "Fit_Points",
                    result.fitPoints()
            );

            diffusionTable.addValue(
                    "Slope_px2_per_frame",
                    result.slope()
            );

            diffusionTable.addValue(
                    "Intercept_px2",
                    result.intercept()
            );

            diffusionTable.addValue(
                    "Diffusion_Coefficient_D_px2_per_frame",
                    result.diffusionCoefficient()
            );

            diffusionTable.show(
                    "Diffusion Coefficient"
            );

            logArea.append("扩散系数计算完成。\n");
            logArea.append(
                    "拟合模型：MSD = slope × lag + intercept\n"
            );
            logArea.append(
                    "使用前 "
                            + result.fitPoints()
                            + " 个 Ensemble MSD 点拟合。\n"
            );
            logArea.append(
                    "Slope = "
                            + result.slope()
                            + " px²/frame\n"
            );
            logArea.append(
                    "Intercept = "
                            + result.intercept()
                            + " px²\n"
            );
            logArea.append(
                    "D = slope / 4 = "
                            + result.diffusionCoefficient()
                            + " px²/frame\n\n"
            );

        } catch (IllegalArgumentException ex) {

            logArea.append(
                    "扩散系数参数错误："
                            + ex.getMessage()
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "扩散系数计算失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void exportSelectedResults() {
        String exportType =
                (String) exportTypeBox.getSelectedItem();

        if (exportType == null) {
            logArea.append(
                    "请选择导出类型。\n\n"
            );
            return;
        }

        if (exportType.contains("Track Results")) {

            exportTrackResultsToCSV();

        } else if (exportType.contains("Track Summary")) {

            exportTrackSummaryToCSV();

        } else if (exportType.contains("MSD Results")) {

            exportMSDResultsToCSV();

        } else if (exportType.contains("Ensemble MSD")) {

            exportEnsembleMSDToCSV();

        } else {

            logArea.append(
                    "未知导出类型："
                            + exportType
                            + "\n\n"
            );
        }
    }

    private File chooseCSVFile(
            String defaultFileName
    ) {
        JFileChooser fileChooser =
                new JFileChooser();

        fileChooser.setDialogTitle(
                "选择CSV保存位置"
        );

        fileChooser.setSelectedFile(
                new File(defaultFileName)
        );

        int userSelection =
                fileChooser.showSaveDialog(this);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selectedFile =
                fileChooser.getSelectedFile();

        if (selectedFile == null) {
            return null;
        }

        String path =
                selectedFile.getAbsolutePath();

        if (!path.toLowerCase().endsWith(".csv")) {
            selectedFile =
                    new File(
                            path + ".csv"
                    );
        }

        return selectedFile;
    }

    private void exportTrackResultsToCSV() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先在 Tracking 的 Method 中选择追踪方法，并点击“执行追踪”。\n\n"
                );
                return;
            }

            File fileToSave =
                    chooseCSVFile(
                            "track_results.csv"
                    );

            if (fileToSave == null) {
                logArea.append(
                        "已取消导出。\n\n"
                );
                return;
            }

            int rowCount =
                    ResultCsvExporter.exportTrackResults(
                            lastTracks,
                            fileToSave
                    );

            logArea.append("轨迹坐标导出完成。\n");
            logArea.append(
                    "保存路径："
                            + fileToSave.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "导出数据行数："
                            + rowCount
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "轨迹坐标导出失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void exportTrackSummaryToCSV() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先在 Tracking 的 Method 中选择追踪方法，并点击“执行追踪”。\n\n"
                );
                return;
            }

            File fileToSave =
                    chooseCSVFile(
                            "track_summary.csv"
                    );

            if (fileToSave == null) {
                logArea.append(
                        "已取消导出。\n\n"
                );
                return;
            }

            List<TrackStatistics> statisticsList =
                    TrackStatisticsCalculator.calculate(
                            lastTracks
                    );

            int rowCount =
                    ResultCsvExporter.exportTrackStatistics(
                            statisticsList,
                            fileToSave
                    );

            logArea.append("轨迹统计导出完成。\n");
            logArea.append(
                    "保存路径："
                            + fileToSave.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "导出轨迹数量："
                            + rowCount
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "轨迹统计导出失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void exportMSDResultsToCSV() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先在 Tracking 的 Method 中选择追踪方法，并点击“执行追踪”。\n\n"
                );
                return;
            }

            File fileToSave =
                    chooseCSVFile(
                            "msd_results.csv"
                    );

            if (fileToSave == null) {
                logArea.append(
                        "已取消导出。\n\n"
                );
                return;
            }

            List<MsdResult> msdResults =
                    MsdCalculator.calculatePerTrack(
                            lastTracks
                    );

            int rowCount =
                    ResultCsvExporter.exportMsdResults(
                            msdResults,
                            fileToSave
                    );

            logArea.append("MSD 结果导出完成。\n");
            logArea.append(
                    "保存路径："
                            + fileToSave.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "导出 MSD 数据行数："
                            + rowCount
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "MSD 结果导出失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    private void exportEnsembleMSDToCSV() {
        try {
            if (lastTracks.isEmpty()) {
                logArea.append(
                        "还没有追踪结果，请先在 Tracking 的 Method 中选择追踪方法，并点击“执行追踪”。\n\n"
                );
                return;
            }

            File fileToSave =
                    chooseCSVFile(
                            "ensemble_msd.csv"
                    );

            if (fileToSave == null) {
                logArea.append(
                        "已取消导出。\n\n"
                );
                return;
            }

            List<EnsembleMsdResult> ensembleResults =
                    MsdCalculator.calculateEnsemble(
                            lastTracks
                    );

            int rowCount =
                    ResultCsvExporter.exportEnsembleMsdResults(
                            ensembleResults,
                            fileToSave
                    );

            logArea.append("Ensemble MSD 导出完成。\n");
            logArea.append(
                    "保存路径："
                            + fileToSave.getAbsolutePath()
                            + "\n"
            );
            logArea.append(
                    "导出数据行数："
                            + rowCount
                            + "\n\n"
            );

        } catch (Exception ex) {

            logArea.append(
                    "Ensemble MSD 导出失败："
                            + ex.getMessage()
                            + "\n\n"
            );

            ex.printStackTrace();
        }
    }

    /**
     * 向界面日志框追加文字。
     */
    public void appendLog(String message) {
        logArea.append(message);
    }


    /**
     * 返回当前控制器。
     */
    public ParticleTrackingController getController() {
        return controller;
    }
}
