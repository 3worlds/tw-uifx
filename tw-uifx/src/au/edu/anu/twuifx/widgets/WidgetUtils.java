package au.edu.anu.twuifx.widgets;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.ymuit.ui.colour.Palette;
import fr.cnrs.iees.identity.impl.LocalScope;
import fr.ens.biologie.generic.utils.Interval;

/**
 * Static methods commonly used in widgets.
 * 
 * @author Ian Davies - 6 July 2022
 */
public class WidgetUtils {
	private WidgetUtils() {
	};

	/**
	 * Write a 2-D {@code Number[][]} array to a tif file, optionally using a
	 * magnification factor.
	 * 
	 * @param matrix             The {@code Number[][]} matrix to save
	 * @param file               Full file name
	 * @param palette            {@link Palette} to use.
	 * @param range              The {@link Interval} range for mapping the data to
	 *                           the palette entries.
	 * @param magnification      Size increase in image output.
	 * @param bufferImageType    The {@link BufferedImage} type constant.
	 * @param imageScalingMethod The {@link Image} scaling method constant.
	 */
	public static void writeResizedMatrixToTiffFile(Number[][] matrix, File file, Palette palette, Interval range,
			int magnification, final int bufferImageType, int imageScalingMethod) {
		double min = range.inf();
		double max = range.sup();
		int nCols = matrix.length;
		int nRows = matrix[0].length;

		BufferedImage bi = new BufferedImage(nCols, nRows, bufferImageType);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		for (int x = 0; x < nCols; x++)
			for (int y = 0; y < nRows; y++) {
				javafx.scene.paint.Color fx = palette.getColour(matrix[x][y].doubleValue(), min, max);
				java.awt.Color awtColor = new java.awt.Color((float) fx.getRed(), (float) fx.getGreen(),
						(float) fx.getBlue(), (float) fx.getOpacity());
				g.setColor(awtColor);
				g.drawLine(x, y, x, y);
			}
		g.dispose();
		try {
			file.getParentFile().mkdirs();
			ImageIO.write(resize(bi, magnification, imageScalingMethod, bufferImageType), "tif", file);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Resize a {@BufferedImage} by an integer factor.
	 * 
	 * @param img                The original image.
	 * @param factor             Rescaling factor
	 * @param imageScalingMethod One of {@code Image} constants.
	 * @param BufferType         One of {@code BufferedImage} constants.
	 * @return The rescaled image.
	 * @see <a href=
	 *      "https://www.tabnine.com/code/java/methods/java.awt.image.BufferedImage/getScaledInstance">
	 *      scaling a tiff file.</a>
	 */
	public static BufferedImage resize(BufferedImage img, double factor, final int imageScalingMethod,
			final int BufferType) {
		assert (img.getWidth() * factor >= 1);
		assert (img.getHeight() * factor >= 1);
		int newW = (int) Math.round(img.getWidth() * factor);
		int newH = (int) Math.round(img.getHeight() * factor);
		Image tmp = img.getScaledInstance(newW, newH, imageScalingMethod);
		BufferedImage result = new BufferedImage(newW, newH, BufferType);
		Graphics2D g = result.createGraphics();
		g.drawImage(tmp, 0, 0, null);
		g.dispose();
		return result;
	}

	/**
	 * Save a file containing experiment details.
	 * 
	 * @param precis        A brief experiment description.
	 * @param baseline      Table of label|value pairs of the baseline settings.
	 * @param treatmentList List of properties with setting levels for each.
	 * @param file          File name for saving.
	 */
	public static void SaveExperimentDesignDetails(String precis, Map<String, Object> baseline,
			List<List<Property>> treatmentList, File file) {
		List<String> fileLines = new ArrayList<>();
		fileLines.clear();
		fileLines.add("Label\tValue");
		fileLines.add("Precis\t" + precis);
		for (Map.Entry<String, Object> pair : baseline.entrySet()) {
			fileLines.add(pair.getKey() + "\t" + pair.getValue());
		}
		if (treatmentList != null && !treatmentList.isEmpty()) {
			fileLines.add("\nSimulator\tSetting(s)");
			for (int i = 0; i < treatmentList.size(); i++) {
				List<Property> list = treatmentList.get(i);
				fileLines.add(i + "\t" + list.toString());
			}
		}
		try {
			file.getParentFile().mkdirs();
			Files.write(file.toPath(), fileLines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getUniqueExperimentSubdirectoryName(String expDir, String widgetId) {
		LocalScope scope = new LocalScope("Files");
		File dir = Project.makeFile(ProjectPaths.RUNTIME, expDir);
		dir.mkdirs();
		for (String fileName : dir.list()) {
			int dotIndex = fileName.lastIndexOf('.');
			fileName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
			scope.newId(true, fileName);
		}
		return scope.newId(false, widgetId + "0").id();

	}

}
