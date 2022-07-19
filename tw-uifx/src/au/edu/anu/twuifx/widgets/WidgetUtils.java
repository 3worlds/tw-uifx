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
import au.edu.anu.twcore.experiment.ExpFactor;
import au.edu.anu.twcore.experiment.runtime.EddReadable;
import au.edu.anu.twcore.experiment.runtime.ExperimentDesignDetails;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.ymuit.ui.colour.Palette;
import au.edu.anu.ymuit.ui.colour.PaletteTypes;
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
	 * Save a file containing all details of the experiment .
	 * 
	 * @param edd  Experiment design details ({@link ExperimentDesignDetails}).
	 * @param file File name for saving.
	 */
	public static void SaveExperimentDesignDetails(EddReadable edd, File file) {
		String s = edd.toDetailString();
		List<String> fileLines = new ArrayList<>();
		fileLines.add(s);
		try {
			file.getParentFile().mkdirs();
			Files.write(file.toPath(), fileLines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Create unique sub-directory name below the given {@code root} directory by
	 * adding an integer to the {@code baseName}.
	 * 
	 * @param root     The root directory.
	 * @param baseName The name to be modified.
	 * @return Unique sub-directory name (wigetID {@literal <n>}).
	 */
	public static String getUniqueExperimentSubdirectoryName(String root, String baseName) {
		LocalScope scope = new LocalScope("Files");
		File dir = Project.makeFile(ProjectPaths.RUNTIME, root);
		dir.mkdirs();
		for (String fileName : dir.list()) {
			int dotIndex = fileName.lastIndexOf('.');
			fileName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
			scope.newId(true, fileName);
		}
		return scope.newId(false, baseName + "0").id();

	}

	/**
	 * Create a unique artifact name for a single simulator in a cross-factorial
	 * design.
	 * 
	 * @param properties Property list of a simulator.
	 * @param factors    Experiment factors.
	 * @return String describing the factor levels used, e.g.
	 *         distance(long)_habitatPattern(coarse)
	 */
	public static String getXFDescriptor(List<Property> properties, Map<String, ExpFactor> factors) {
		String result = "";
		for (Property p : properties) {
			// get the factor for this property key
			ExpFactor factor = factors.get(p.getKey());
			// get the level value name of this property
			String fn = factor.getValueName(p);
			result += "_" + factor.getName() + "(" + fn + ")";
		}
		return result;

	}

	/**
	 * Generate an R script for ANOVA analysis.
	 * 
	 * @param inFile               The input file name (tab separated with header
	 *                             and '.' decimal separator).
	 * @param factors              Factor details ({@link ExpFactor}).
	 * @param responseVariableName The name of the response variable. The input file
	 *                             must name the response variable as "RV".
	 * @param resultsName          The file name for the results table.
	 * @return Lines of text for the script.
	 */
	public static List<String> generateANOVAScript(File inFile, Map<String, ExpFactor> factors,
			String responseVariableName, String resultsName) {
		List<String> result = new ArrayList<>();
		result.add("setwd(\"" + inFile.getParent() + "\")");
		result.add("data = read.table(\"" + inFile.getName() + "\",sep=\"\t\",header = TRUE,dec=\".\")");
		for (Map.Entry<String, ExpFactor> entry : factors.entrySet())
			result.add(entry.getValue().getName() + " = data$" + entry.getValue().getName());
		result.add(responseVariableName + " = data$RV");
		String args = responseVariableName + "~";
		for (Map.Entry<String, ExpFactor> entry : factors.entrySet())
			args += "*" + entry.getValue().getName();
		args = args.replaceFirst("\\*", "");
		result.add("mdl = lm(" + args + ")");
		result.add("ava = anova (mdl)");
		result.add("write.table(ava,\"" + resultsName + "\", sep = \"\t\")");
		return result;
	}

	/*-
	setwd(paste("/home/ian/Documents/Tiwi/papers/Informed dispersal/ANOVA-HD/",exp,"/",subDir,sep=""))
	data = read.table(fileName,sep="\t",header = TRUE,dec=".")
	dd = data$dd
	hp = data$hp
	dm = data$dm
	hd = data$hd
	response = data$RV
	
	title = paste(exp," (",subject,")")
	if (toFile)
	svg(paste("Trends",title,".svg"),width = 5.5, height = 5.5)
	oldpar <- par(mfrow = c(2,2))
	plot(response~dd, main = title,ylab=subject,xlab = "Dispersal distance")
	plot(response~hp, main = title,ylab=subject,xlab = "Habitat pattern")
	plot(response~dm, main = title,ylab=subject,xlab = "Dispersal method")
	plot(response~hd, main = title,ylab=subject,xlab = "Habitat dynamics")
	if (toFile)
	dev.off()
	
	 */
	public static List<String> generateBoxPlotScript(File inFile, Map<String, ExpFactor> factors, String rv,
			String resultsName) {

		String exp = inFile.getParentFile().getParentFile().getName();
		String _rv = "tmp" + rv;
		List<String> f = new ArrayList<>();
		List<String> _f = new ArrayList<>();
		for (Map.Entry<String, ExpFactor> entry : factors.entrySet()) {
			String n = entry.getValue().getName();
			f.add(n);
			_f.add("tmp" + n);
		}
		int rows = (int) Math.max(1, Math.sqrt(factors.size()));
		int cols = rows;
		if (rows * cols < factors.size())
			cols++;
		if (rows * cols < factors.size())
			rows++;

		double width = 2.75 * cols;
		double height = 2.75 * rows;

		List<String> result = new ArrayList<>();
		result.add("setwd(\"" + inFile.getParent() + "\")");
		result.add("data = read.table(\"" + inFile.getName() + "\",sep=\"\\t\",header = TRUE,dec=\".\")");
		for (int i = 0; i < f.size(); i++) {
			String fn = f.get(i);
			String _fn = _f.get(i);
			result.add(_fn + " = data$" + fn);
		}
		result.add(_rv + " = data$RV");
		result.add("title = paste(\"" + exp + "\",\" (\",\"" + rv + "\",\")\")");
		result.add("svg(paste(\"Trends\",title,\".svg\"),width = " + width + ", height = " + height + ")");
		result.add("par(mfrow = c(" + rows + "," + cols + "))");
		for (int i = 0; i < f.size(); i++) {
			String fn = f.get(i);
			String _fn = _f.get(i);
			result.add("plot (" + _rv + "~" + _fn + ", main = title, ylab = \"" + rv + "\",xlab = \"" + fn + "\")");
		}

		//result.add("dev.off()");

		return result;

	}

	public static boolean saveAndExecuteScript(File scriptFile, List<String> lines) {
		try {
			Files.write(scriptFile.toPath(), lines, StandardCharsets.UTF_8);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<String> commands = new ArrayList<>();
		commands.add("Rscript");
		commands.add(scriptFile.getAbsolutePath());
		ProcessBuilder b = new ProcessBuilder(commands);
		b.directory(new File(scriptFile.getParent()));
		b.inheritIO();
		boolean result = true;
		try {
			try {
				b.start().waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				result = false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			result = false;
		}
		return result;

	}

}
