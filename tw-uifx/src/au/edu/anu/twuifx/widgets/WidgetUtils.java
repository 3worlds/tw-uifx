package au.edu.anu.twuifx.widgets;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
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
import fr.cnrs.iees.identity.impl.LocalScope;
import fr.ens.biologie.generic.utils.Interval;
import fr.ens.biologie.generic.utils.NiceScale;
import javafx.scene.paint.Color;

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
	 * @param bkgColor           Colour used when value is considered a missing
	 *                           value.
	 * @param isMissingValue     Enum to determine if a value is a missing value
	 *                           {@link IsMissingValue}.
	 */
	public static void writeResizedMatrixToTiffFile(Number[][] matrix, File file, Palette palette, Interval range,
			int magnification, final int bufferImageType, int imageScalingMethod, IsMissingValue isMissingValue,
			Color bkgColor) {
		double min = range.inf();
		double max = range.sup();
		int nCols = matrix.length;
		int nRows = matrix[0].length;

		BufferedImage bi = new BufferedImage(nCols, nRows, bufferImageType);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		for (int x = 0; x < nCols; x++)
			for (int y = 0; y < nRows; y++) {
				javafx.scene.paint.Color fx = bkgColor;
				if (!isMissingValue.apply(min, max, matrix[x][y].doubleValue()))
					fx = palette.getColour(matrix[x][y].doubleValue(), min, max);
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
	/**
	 * Generate an R script that will produce a box plot showing the overall trends
	 * in the response variable for each of the experiment factors.
	 * 
	 * @param inFile  The file path of the data file.
	 * @param factors The experiment factor details {@link ExpFactor}.
	 * @param rv      The response variable name.
	 * @return The script as a list of strings.
	 */
	public static List<String> generateBoxPlotScript(File inFile, Map<String, ExpFactor> factors, String rv) {

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

		// seems to produce unnecessary output to the console
		// result.add("dev.off()");

		return result;

	}

	public static List<String> generateRSSPlotScript(File inFile, String rv) {
		List<String> result = new ArrayList<>();
		double t = 0.01;
		result.add("setwd(\"" + inFile.getParent() + "\")");
		result.add("data = read.csv(\"" + inFile.getName() + "\",sep=\"\\t\",header = TRUE,dec=\".\")");
		result.add("data = na.omit(data)");
		result.add("select = data$Rel.sum.sq>=" + t);
		result.add("terms = data$Terms[select]");
		result.add("values = data$Rel.sum.sq[select]");
		result.add("w =2 + 0.5 * length(values)");
		result.add("h = 3");
		result.add("svg(paste(\"" + rv + "\",\"_RSSPlot\",\".svg\"),width = w,height = h)");
		result.add("r = barplot(values,names=terms,ylim = c(0.0,1.0),xlab=\"Terms\",ylab=paste(\"Explained(>=" + t
				+ ")\"),main = \"" + rv + "\",las=2)");
		result.add("segments(0.0,0.05,r[length(r)]+0.5,0.05,lty=2)");
		return result;
	}

	public static List<String> generateTrendsBarPlotScript(File inFile, Map<String, ExpFactor> factors, String rv,
			boolean isMinZero) {
		// Make sure we refer to the column headings only to get the order of factors.
		List<String> result = null;
		try {
			List<String> lines = Files.readAllLines(inFile.toPath(), StandardCharsets.UTF_8);
			String header = lines.get(0).replace("\t", ",");
			String lastLine = lines.get(lines.size() - 1).replace("\t", ",");
			String[] cols = header.split(",");
			String[] items = lastLine.split(",");
			// NB: Make sure we construct a list in the order they appear in the column
			// header!!!
			List<String> factorOrder = new ArrayList<>();
			String[] colItems = cols[0].split("_");
			for (String item : colItems) {
				String factorName = item.substring(0, item.indexOf("["));
				factorOrder.add(factorName);
			}
			// make a Map where the edge name is the key rather than the endNode id
			Map<String, ExpFactor> fvmap = new HashMap<>();
			int maxLevels = 0;
			for (ExpFactor expf : factors.values()) {
				fvmap.put(expf.getName(), expf);
				maxLevels = Math.max(maxLevels, expf.nLevels());
			}

			double min = Double.MAX_VALUE;
			double max = -min;
			for (String s : items) {
				double d = Double.parseDouble(s);
				min = Math.min(d, min);
				max = Math.max(d, max);
			}
			if (isMinZero)
				min = 0;
			NiceScale ns = new NiceScale(min, max);
			String exp = inFile.getParentFile().getParentFile().getName();

			List<String> levelColours = new ArrayList<>();
			for (int i = 0; i < maxLevels; i++) {
				int idx = (int) ((double) i / (double) maxLevels * 100.0);
				levelColours.add("gray" + idx);

			}
			result = new ArrayList<>();
			result.add("setwd(\"" + inFile.getParent() + "\")");
			result.add("values = c(" + lastLine + ")");
			result.add("xNames = letters[1:length(values)]");
			result.add("yRange = c(" + ns.getNiceMin() + ", " + ns.getNiceMax() + ")");
			result.add("");
			result.add("w = 2 + 0.4 * length(values)");
			result.add("h = 5");
			result.add("svg(paste(\"" + rv + "\",\"_MeansPlot\",\".svg\"),width = w,height = h)");
			result.add("");
			result.add(" x <-barplot(values,");
			result.add("\tmain = paste(\"" + exp + "\",\"(\",\"" + rv + "\",\")\"),");
			result.add("\tylab = \"" + rv + "\",");
			result.add("\txlab = \"Trials\",");
			result.add("\tylim = yRange,");
			result.add("\tnames.arg = xNames,xpd = FALSE)");
			result.add("");
			result.add("s = 0.04");
			result.add("range = yRange[2]-yRange[1]");
			result.add("dx = 0.3");
			result.add("weight = 6");
			String line = "ly = c(s * range";
			for (int f = 1; f < factorOrder.size(); f++) {
				line += ", " + (f + 1) + " * s * range";
			}
			result.add(line + ")+yRange[1]");
			result.add("");
			for (int i = 0; i < items.length; i++) {
				int barNo = i + 1;
				String[] fs = cols[i].split("_");
				result.add("");
				for (int l = 0; l < fs.length; l++) { // items x fs statements
					int ht = l + 1;
					String fl = fs[l];
					// which level?
					// dd[short]
					String factorName = fl.substring(0, fl.indexOf("["));
					String factorLevel = fl.substring(fl.indexOf("[") + 1, fl.indexOf("]"));
					int nLevels = fvmap.get(factorName).nLevels();
					int level = fvmap.get(factorName).getValueLevel(factorLevel);
					// segments(x[1]-dx,ly[4],x[1]+dx,ly[4],col="red4",lwd=weight)
					result.add("segments(x[" + barNo + "]-dx, ly[" + ht + "], x[" + barNo + "]+dx, ly[" + ht
							+ "], col=\"" + levelColours.get(level) + "\", lwd=weight)");
				}
			}
			result.add("");
			int i = 0;
			for (String s : factorOrder) {
//				text(-0.2,ly[1],"dd")
				result.add("text(-0.2, ly[" + (++i) + "], \"" + s + "\")");
			}

			result.add("");
			result.add("pos = \"topleft\"");
			result.add("if (values[1]>values[length(values)])");
			result.add("\tpos =\"topright\"");
			String s = "";

			for (int j = factorOrder.size() - 1; j >= 0; j--) {
				s += "," + "\"" + fvmap.get(factorOrder.get(j)).toShortString() + "\"";
			}
			s = s.replaceFirst(",", "");

			result.add("legend(pos,");
			result.add("\tc(" + s + "),");
			result.add("\tlty=c(0),");
			result.add("\ttitle= \"Treatments\",");
			result.add("\tbty=\"n\",");
			result.add("\tncol = 1)");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {
		List<String> lst = new ArrayList<>();
		lst.add("4");
		lst.add("3");
		lst.add("2");
		lst.add("1");
		for (int i = lst.size() - 1; i >= 0; i--)
			System.out.println(lst.get(i));
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
				result = false;
			}
		} catch (IOException e) {
			result = false;
		}
		return result;

	}

}
