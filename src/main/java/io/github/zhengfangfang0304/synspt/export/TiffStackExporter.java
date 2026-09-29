package io.github.zhengfangfang0304.synspt.export;

import io.github.zhengfangfang0304.synspt.engine.RenderedSimulationResult;

import ij.io.FileSaver;

import java.io.IOException;
import java.nio.file.Path;

/** Writes the calibrated ImagePlus as TIFF or TIFF stack. */
public final class TiffStackExporter {

    public void write(RenderedSimulationResult result, Path outputPath)
            throws IOException {
        if (result == null || outputPath == null) {
            throw new IllegalArgumentException("Result and TIFF path cannot be null.");
        }
        FileSaver fileSaver = new FileSaver(result.getImagePlus());
        boolean saved = result.getImagePlus().getStackSize() > 1
                ? fileSaver.saveAsTiffStack(outputPath.toString())
                : fileSaver.saveAsTiff(outputPath.toString());
        if (!saved) {
            throw new IOException("ImageJ could not save the TIFF dataset.");
        }
    }
}
