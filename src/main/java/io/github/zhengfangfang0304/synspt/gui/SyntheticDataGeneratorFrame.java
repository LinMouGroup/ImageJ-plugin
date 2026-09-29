package io.github.zhengfangfang0304.synspt.gui;

import io.github.zhengfangfang0304.synspt.config.SimulationConfig;
import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;
import io.github.zhengfangfang0304.synspt.engine.SimulationCancellationToken;
import io.github.zhengfangfang0304.synspt.engine.SimulationProgressListener;
import io.github.zhengfangfang0304.synspt.engine.SyntheticDatasetGenerator;
import io.github.zhengfangfang0304.synspt.export.DatasetExporter;
import io.github.zhengfangfang0304.synspt.export.ExportManifest;
import io.github.zhengfangfang0304.synspt.run.SimulationRunContext;
import io.github.zhengfangfang0304.synspt.run.SimulationRunDirectoryFactory;

import ij.IJ;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/** Main Fiji window for simulation generation and export. */
public final class SyntheticDataGeneratorFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int GENERATION_PROGRESS_RANGE = 85;
    private static final int EXPORT_PROGRESS_OFFSET = 85;
    private static final int EXPORT_PROGRESS_RANGE = 15;
    private static final int INITIAL_WINDOW_WIDTH = 700;
    private static final int INITIAL_WINDOW_HEIGHT = 680;
    private static final int MINIMUM_WINDOW_WIDTH = 640;
    private static final int MINIMUM_WINDOW_HEIGHT = 600;

    private final SimulationFormPanel formPanel;
    private final JButton readmeButton;
    private final JButton generateButton;
    private final JButton cancelButton;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final SyntheticDatasetGenerator datasetGenerator;
    private final DatasetExporter datasetExporter;
    private final SecureRandom randomSeedSource;

    private GenerationWorker activeWorker;
    private ReadmeDialog readmeDialog;

    public SyntheticDataGeneratorFrame() {
        this(
                new SyntheticDatasetGenerator(),
                new DatasetExporter(),
                new SecureRandom()
        );
    }

    SyntheticDataGeneratorFrame(
            SyntheticDatasetGenerator datasetGenerator,
            DatasetExporter datasetExporter,
            SecureRandom randomSeedSource
    ) {
        super("synSPT Synthetic Data Generator");
        if (datasetGenerator == null
                || datasetExporter == null
                || randomSeedSource == null) {
            throw new IllegalArgumentException("GUI dependencies cannot be null.");
        }
        this.datasetGenerator = datasetGenerator;
        this.datasetExporter = datasetExporter;
        this.randomSeedSource = randomSeedSource;
        formPanel = new SimulationFormPanel();
        readmeButton = new JButton("README");
        generateButton = new JButton("Generate...");
        cancelButton = new JButton("Cancel");
        progressBar = new JProgressBar(0, 100);
        statusLabel = new JLabel("Ready");

        buildWindow();
        bindActions();
    }

    private void buildWindow() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));
        add(formPanel, BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
        cancelButton.setEnabled(false);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        pack();
        setMinimumSize(new Dimension(
                MINIMUM_WINDOW_WIDTH,
                MINIMUM_WINDOW_HEIGHT
        ));
        setSize(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
        setResizable(true);
        setLocationRelativeTo(IJ.getInstance());
    }

    private JPanel createControlPanel() {
        JPanel outer = new JPanel(new BorderLayout(8, 6));
        outer.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        outer.add(statusLabel, BorderLayout.NORTH);
        outer.add(progressBar, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new BorderLayout());
        JPanel rightButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        rightButtons.add(cancelButton);
        rightButtons.add(generateButton);
        actionRow.add(readmeButton, BorderLayout.WEST);
        actionRow.add(rightButtons, BorderLayout.EAST);
        outer.add(actionRow, BorderLayout.SOUTH);
        return outer;
    }

    private void bindActions() {
        readmeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showReadme();
            }
        });
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                beginGeneration();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                cancelActiveGeneration();
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                closeWindow();
            }
        });
    }

    private void showReadme() {
        if (readmeDialog == null || !readmeDialog.isDisplayable()) {
            readmeDialog = new ReadmeDialog(this);
        }
        readmeDialog.showAtTop();
    }

    private void beginGeneration() {
        if (activeWorker != null) {
            return;
        }

        final SimulationConfig config;
        try {
            config = formPanel.createConfig(randomSeedSource.nextLong());
        } catch (IllegalArgumentException exception) {
            showError("Invalid simulation settings", exception.getMessage());
            return;
        }

        Path outputDirectory = chooseOutputDirectory();
        if (outputDirectory == null) {
            return;
        }
        activeWorker = new GenerationWorker(config, outputDirectory);
        setRunning(true);
        activeWorker.execute();
    }

    private Path chooseOutputDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select synSPT base output folder (a new run folder will be created)");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonText("Generate");
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
    }

    private void cancelActiveGeneration() {
        if (activeWorker != null) {
            statusLabel.setText("Cancelling...");
            cancelButton.setEnabled(false);
            activeWorker.cancel(true);
        }
    }

    private void closeWindow() {
        if (activeWorker == null) {
            dispose();
            return;
        }
        int decision = JOptionPane.showConfirmDialog(
                this,
                "A simulation is running. Cancel it and close the window?",
                "Cancel simulation?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (decision == JOptionPane.YES_OPTION) {
            activeWorker.cancel(true);
            dispose();
        }
    }

    private void setRunning(boolean running) {
        generateButton.setEnabled(!running);
        cancelButton.setEnabled(running);
        formPanel.setInputsEnabled(!running);
        if (running) {
            progressBar.setValue(0);
            statusLabel.setText("Starting simulation...");
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    private SimulationProgressListener scaledProgress(
            final GenerationWorker worker,
            final int offset,
            final int range
    ) {
        return new SimulationProgressListener() {
            @Override
            public void onProgress(int percent, String message) {
                int bounded = Math.max(0, Math.min(100, percent));
                worker.report(offset + range * bounded / 100, message);
            }
        };
    }

    private final class GenerationWorker extends
            SwingWorker<GenerationOutcome, ProgressUpdate> {

        private final SimulationConfig config;
        private final Path outputDirectory;

        private GenerationWorker(
                SimulationConfig config,
                Path outputDirectory
        ) {
            this.config = config;
            this.outputDirectory = outputDirectory;
        }

        @Override
        protected GenerationOutcome doInBackground() throws Exception {
            SimulationCancellationToken cancellationToken =
                    new SimulationCancellationToken() {
                        @Override
                        public boolean isCancellationRequested() {
                            return isCancelled()
                                    || Thread.currentThread().isInterrupted();
                        }
                    };
            SimulationRunContext run = new SimulationRunDirectoryFactory().create(outputDirectory, config);
            report(0, "Run " + run.getRunId());
            RenderedSimulationResult result = datasetGenerator.generate(
                    run.getConfig(),
                    scaledProgress(
                            this,
                            0,
                            GENERATION_PROGRESS_RANGE
                    ),
                    cancellationToken
            );
            ExportManifest manifest = datasetExporter.export(
                    result,
                    run,
                    scaledProgress(
                            this,
                            EXPORT_PROGRESS_OFFSET,
                            EXPORT_PROGRESS_RANGE
                    ),
                    cancellationToken
            );
            return new GenerationOutcome(result, manifest);
        }

        private void report(int percent, String message) {
            int bounded = Math.max(0, Math.min(100, percent));
            setProgress(bounded);
            publish(new ProgressUpdate(bounded, message));
        }

        @Override
        protected void process(List<ProgressUpdate> updates) {
            if (updates.isEmpty()) {
                return;
            }
            ProgressUpdate latest = updates.get(updates.size() - 1);
            progressBar.setValue(latest.percent);
            statusLabel.setText(latest.message);
        }

        @Override
        protected void done() {
            try {
                if (isCancelled()) {
                    statusLabel.setText("Cancelled");
                    return;
                }
                GenerationOutcome outcome = get();
                progressBar.setValue(100);
                statusLabel.setText("Complete");
                outcome.result.getImagePlus().show();
                if (isDisplayable()) {
                    JOptionPane.showMessageDialog(
                            SyntheticDataGeneratorFrame.this,
                            "Synthetic dataset generated successfully.\n\n"
                                    + outcome.manifest.getTiffPath() + "\n"
                                    + outcome.manifest.getTrajectoryCsvPath() + "\n"
                                    + outcome.manifest.getMetadataJsonPath(),
                            "synSPT generation complete",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (CancellationException exception) {
                statusLabel.setText("Cancelled");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                statusLabel.setText("Interrupted");
            } catch (ExecutionException exception) {
                Throwable cause = exception.getCause() == null
                        ? exception
                        : exception.getCause();
                IJ.log("synSPT generation failed: " + cause.toString());
                if (isDisplayable()) {
                    showError(
                            "synSPT generation failed",
                            cause.getMessage() == null
                                    ? cause.toString()
                                    : cause.getMessage()
                    );
                }
                statusLabel.setText("Failed");
            } finally {
                activeWorker = null;
                setRunning(false);
            }
        }
    }

    private static final class ProgressUpdate {

        private final int percent;
        private final String message;

        private ProgressUpdate(int percent, String message) {
            this.percent = percent;
            this.message = message;
        }
    }

    private static final class GenerationOutcome {

        private final RenderedSimulationResult result;
        private final ExportManifest manifest;

        private GenerationOutcome(
                RenderedSimulationResult result,
                ExportManifest manifest
        ) {
            this.result = result;
            this.manifest = manifest;
        }
    }
}
