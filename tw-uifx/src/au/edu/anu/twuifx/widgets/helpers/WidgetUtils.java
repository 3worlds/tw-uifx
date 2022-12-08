/**************************************************************************
 *  TW-UIFX - ThreeWorlds User-Interface fx                               *
 *                                                                        *
 *  Copyright 2018: Jacques Gignoux & Ian D. Davies                       *
 *       jacques.gignoux@upmc.fr                                          *
 *       ian.davies@anu.edu.au                                            *
 *                                                                        *
 *  TW-UIFX contains the Javafx interface for ModelMaker and ModelRunner. *
 *  This is to separate concerns of UI implementation and the code for    *
 *  these java programs.                                                  *
 *                                                                        *
 **************************************************************************
 *  This file is part of TW-UIFX (ThreeWorlds User-Interface fx).         *
 *                                                                        *
 *  TW-UIFX is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  TW-UIFX is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with TW-UIFX.                                                   *
 *  If not, see <https://www.gnu.org/licenses/gpl.html>.                  *
 *                                                                        *
 **************************************************************************/
package au.edu.anu.twuifx.widgets.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import fr.cnrs.iees.omugi.graph.property.Property;
import au.edu.anu.omhtk.util.StringUtils;
import au.edu.anu.twcore.experiment.ExpFactor;
import au.edu.anu.twcore.experiment.runtime.*;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.widgets.IsMissingValue;
import au.edu.anu.ymuit.ui.colour.Palette;
import fr.cnrs.iees.omugi.identity.impl.LocalScope;
import fr.cnrs.iees.twcore.constants.ExperimentDesignType;
import fr.cnrs.iees.omhtk.utils.*;
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
	 * Write a 2-D {@code Number[][]} array to a tif file
	 * <p>
	 * <img src="{@docRoot}/../doc/images/TiffExample.png" width="250" alt=
	 * "TiffExample"/>
	 * </p>
	 * 
	 * @param matrix             The {@code Number[][]} matrix to save
	 * @param file               Full file name
	 * @param palette            {@link Palette} to use.
	 * @param range              The {@link Interval} range for mapping the data to
	 *                           the palette entries.
	 * @param magnification      Size increase in image output (default 1).
	 * @param bufferImageType    The {@link BufferedImage} type constant.
	 * @param imageScalingMethod The {@link Image} scaling method constant.
	 * @param bkgColor           Colour used when if value is a missing value.
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
		File dir = Project.makeFile(Project.RUNTIME, root);
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
	 * Generate an script for ANOVA analysis.
	 * 
	 * @param inFile               The input file name (tab separated with header
	 *                             and '.' decimal separator).
	 * @param factors              Factor details ({@link ExpFactor}).
	 * @param orderedFactors       Factors ordered by appearance in column headers.
	 * @param responseVariableName The name of the response variable. The input file
	 *                             must name the response variable as "RV".
	 * @param resultsName          The file name for the results table.
	 * @return Lines of text for the script.
	 */
	public static List<String> generateANOVAScript(File inFile, Map<String, ExpFactor> factors,
			List<ExpFactor> orderedFactors, String responseVariableName, String resultsName) {
		List<String> result = new ArrayList<>();
		result.add("setwd(\"" + inFile.getParent() + "\")");
		result.add("data = read.table(\"" + inFile.getName() + "\",sep=\"\t\",header = TRUE,dec=\".\")");
		for (ExpFactor f : orderedFactors)
			result.add(f.getName() + " = data$" + f.getName());

//		for (Map.Entry<String, ExpFactor> entry : factors.entrySet())
//			result.add(entry.getValue().getName() + " = data$" + entry.getValue().getName());
		result.add(responseVariableName + " = data$RV");
		String args = responseVariableName + "~";
		for (ExpFactor f : orderedFactors)
			args += "*" + f.getName();
//		for (Map.Entry<String, ExpFactor> entry : factors.entrySet())
//			args += "*" + entry.getValue().getName();
		args = args.replaceFirst("\\*", "");
		result.add("mdl = lm(" + args + ")");
		result.add("ava = anova (mdl)");
		result.add("write.table(ava,\"" + resultsName + "\", sep = \"\t\")");
		return result;
	}

	/**
	 * Generate an R script that will produce a box plot showing the overall trends
	 * in the response variable for each of the experiment factors in a
	 * cross-factorial experiment.
	 * <p>
	 * <img src="{@docRoot}/../doc/images/population_boxplots.svg" width="250" alt=
	 * "population_boxblots"/>
	 * </p>
	 * 
	 * @param inFile         The file path of the data file.
	 * @param factors        The experiment factor details {@link ExpFactor}.
	 * @param orderedFactors The factors ordered according to series header order
	 * @param rv             The response variable name.
	 * @return The script as a list of strings.
	 */
	public static List<String> generateBoxPlotScript(File inFile, Map<String, ExpFactor> factors,
			List<ExpFactor> orderedFactors, String rv) {

		String exp = inFile.getParentFile().getParentFile().getName();
		String _rv = "tmp" + rv;
		List<String> f = new ArrayList<>();
		List<String> _f = new ArrayList<>();
		for (ExpFactor ef : orderedFactors) {
			String n = ef.getName();
			f.add(n);
			_f.add("tmp" + n);
		}
		Duple<Integer, Integer> d = getRowColSize(orderedFactors.size());
		int rows = d.getFirst();
		int cols = d.getSecond();

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
		result.add("title = \"" + StringUtils.cap(rv) + " [" + exp + "]\"");
		result.add("svg(\"" + rv + "_boxplots.svg\",width = " + width + ", height = " + height + ")");
		result.add("par(mfrow = c(" + rows + "," + cols + "))");
		result.add("par(oma=c(0,0,2,0))");
		for (int i = 0; i < f.size(); i++) {
			String fn = f.get(i);
			String _fn = _f.get(i);
			result.add("plot (" + _rv + "~" + _fn + ", ylab = \"" + rv + "\",xlab = \"" + fn + "\")");
		}
		result.add("mtext(text=title,side=3,line=0,outer=TRUE)");

		result.add("invisible(dev.off())");

		return result;

	}

	/**
	 * Generate an R script to create a barplot of the relative variance of the
	 * response variable explained by treatments.
	 * <p>
	 * <img src="{@docRoot}/../doc/images/population_RVEplot.svg" width="250" alt=
	 * "population_RVEplot"/>
	 * </p>
	 * 
	 * @param inFile The data file of relative variance explained
	 * @param rv     Name of the response variable.
	 * @return Script as a list of strings.
	 * 
	 */
	public static List<String> generateRVEPlotScript(File inFile, String rv) {
		String exp = inFile.getParentFile().getParentFile().getName();
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
		result.add("svg(\"" + rv + "_RVEplot.svg\",width = w,height = h)");
		result.add("par(mar=c(7,4,4,2)+.1)");

		result.add("r = barplot(values,names=terms,ylim = c(0.0,1.0),xlab=\"Terms\",ylab=\"Explained(>=" + t
				+ ")\",main = \"" + StringUtils.cap(rv) + " [" + exp + "]\",las=2)");
		result.add("segments(0.0,0.05,r[length(r)]+0.5,0.05,lty=2)");
		result.add("invisible(dev.off())");

		return result;
	}

	/**
	 * Generate an R script to create a barplot of mean value of replicates for each
	 * factor combination in a cross-factorial experiment.
	 * <p>
	 * <img src="{@docRoot}/../doc/images/population_barplot.svg" width="250" alt=
	 * "population_barplot"/>
	 * </p>
	 * 
	 * @param inFile    time series data of average for all replicates.
	 * @param factors   Experiment factors and their treatment levels.
	 * @param rv        the response variable
	 * @param isMinZero forces y-axis to zero if true.
	 * @return RScript as a list of strings.
	 */
	public static List<String> generateBarPlotScript(File inFile, Map<String, ExpFactor> factors, String rv,
			boolean isMinZero) {
		// Make sure we refer to the column headings only to obtain the order of
		// factors.
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
			List<String> yAxisLabels = getYAxisLabels(fvmap, factorOrder);
			int longest = 0;
			for (String term:yAxisLabels)
				longest = Math.max(longest, term.length());

			NiceScale ns = new NiceScale(min, max);
			String exp = inFile.getParentFile().getParentFile().getName();

//			List<String> levelColours = new ArrayList<>();
//			for (int i = 0; i < maxLevels; i++) {
//				int idx = (int) ((double) i / (double) maxLevels * 100.0);
//				levelColours.add("gray" + idx);
//
//			}
			result = new ArrayList<>();
			result.add("setwd(\"" + inFile.getParent() + "\")");
			result.add("values = c(" + lastLine + ")");
			result.add("yNames = c(");
			for (int i = 0; i<yAxisLabels.size();i++) {
				String term = yAxisLabels.get(i);
				if (i<yAxisLabels.size()-1)
				result.add("\""+term+"\",");
				else
					result.add("\""+term+"\")");			
			}
			result.add("yNames = rev(yNames)");
			result.add("values = rev(values)");
			result.add("xRange = c(" + ns.getNiceMin() + ", " + ns.getNiceMax() + ")");
			result.add("");
			result.add("w = 5");
			result.add("h = 5");
			
			result.add("svg(\"" + rv + "_barplot.svg\",width = w,height = h)");
			result.add("");
			result.add("bottom = 5.1");
			
			double f =  (Math.round(factorOrder.size()*2.5)+0.1);
			result.add("left = "+f+"");
			result.add("top = 4.1");
			result.add("right = 2.1");
			result.add("par(mar = c(bottom,left,top,right))");
			
			result.add(" x <-barplot(values,");
			result.add("horiz = TRUE,");
			result.add("\tmain = \"" + StringUtils.cap(rv) + " [" + exp + "]\",");
			result.add("\txlab = \""+rv+"\",");
			result.add("\txlim = xRange,");
			result.add("\tlas = 1,");
			result.add("\tnames.arg = yNames,xpd = FALSE)");
			result.add("");
			String yaxisHeader = getBarPlotYAxisHeader(factorOrder,longest);
			result.add("mtext(\""+yaxisHeader+"\",side =2, adj = 1.15, las=1,at=x[length(x)]+1)");
			result.add("invisible(dev.off())");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private static String getBarPlotYAxisHeader(List<String> factorOrder,int longest) {
		String tmp = "";
		for (String f:factorOrder)
			tmp+=f;
		int spacing = (longest - tmp.length())/(factorOrder.size()-1);
		StringBuilder sb = new StringBuilder();
		for (String f:factorOrder) {
			sb.append(f);
			for (int i=0;i<spacing;i++)
				sb.append(" ");
		}
		return sb.toString().trim();
	}

	private static List<String> getYAxisLabels(Map<String, ExpFactor> fvmap, List<String> factorOrder) {
		List<Integer> levels = new ArrayList<>();
		for (String key : factorOrder)
			levels.add(fvmap.get(key).nLevels());
		List<List<Integer>> indicies = FullFactorial.getFactorialIndicies(levels);
		Collections.reverse(indicies);
		// [[0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1],
		// [0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1],
		// [0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1],
		// [0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1]]
		List<List<String>> names = new ArrayList<>();
		for (int i = 0; i < indicies.size(); i++) {
			List<String> nameList = new ArrayList<>();
			names.add(nameList);
			List<Integer> index1 = indicies.get(i);
			for (int j = 0; j < index1.size(); j++) {
				Integer idx = index1.get(j);
				ExpFactor ef = fvmap.get(factorOrder.get(i));
				String name = ef.getValueName(idx);
				if (j == 0) {
					nameList.add(name);
				} else {
					if (idx.equals(index1.get(j - 1)))
						nameList.add("");
					else
						nameList.add(name);
				}
			}
		}
		List<String> result = new ArrayList<>();
		int n = indicies.get(0).size();
		List<List<String>> terms = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			List<String> term = new ArrayList<>();
			terms.add(term);
			for (int j = 0; j < names.size(); j++) {
				String name = names.get(j).get(i);
				if (!name.isBlank())
					term.add(name);

			}
		}
		for (List<String> term : terms) {
			StringBuilder t = new StringBuilder();
			for (String part:term) {
				t.append(part).append(" ");
			}
			result.add(t.toString().trim());
		}
		
		return result;
	}

	
	/**
	 * Creates file contents for an R script to plot data series for each treatment
	 * combination. averaged over replicates. Standard deviation for each mean
	 * series is shown with grey polygons.
	 * <p>
	 * <img src="{@docRoot}/../doc/images/Ht_Series.svg" width="400" alt=
	 * "Ht_Series"/>
	 * </p>
	 * 
	 * @param edt         The experiment design information.
	 * @param inFile      File of time series of the response variable for each all
	 *                    simulations.
	 * @param rv          The response variable
	 * @param nReplicates The number of experiment replicates.
	 * @param isMinZero   Flag to force zero on the y axis.
	 * @return R script file contents.
	 */
	public static List<String> generateSeriesScript(ExperimentDesignType edt, File inFile, String rv, int nReplicates,
			boolean isMinZero) {
		List<String> result = new ArrayList<>();

		try {
			List<String> lines = Files.readAllLines(inFile.toPath(), StandardCharsets.UTF_8);
			String[] header = lines.get(0).split("\t");
			String expName = inFile.getParentFile().getParentFile().getName();
			// 0:dd[short]_dm[uninf]_hd[dyna]_hs[small]
			int nPlots = header.length / nReplicates;
			Duple<Integer, Integer> d = getRowColSize(nPlots);
			String[] titles = new String[nPlots];
			for (int i = 0; i < nPlots; i++) {
				String[] items = header[i].split(":")[1].split("_");
				String title = items[0];
				for (int j = 1; j < items.length; j++)
					if (j % 3 == 0)
						title += "\\n" + items[j];
					else
						title += "; " + items[j];

				titles[i] = title;
			}
			int rows = d.getFirst();
			int cols = d.getSecond();

			double min = Double.MAX_VALUE;
			double max = -min;
			for (int i = 1; i < lines.size(); i++) {
				String[] items = lines.get(i).split("\t");
				for (String item : items) {
					double value = Double.parseDouble(item);
					min = Math.min(min, value);
					max = Math.max(max, value);
				}
			}
			if (isMinZero)
				min = 0;
			NiceScale ns = new NiceScale(min, max);

			result.add("setwd(\"" + inFile.getParent() + "\")");
			result.add("data = read.table(\"" + inFile.getName() + "\",sep=\"\\t\", header = TRUE, dec = \".\")");
			result.add("ylim = c(" + ns.getNiceMin() + "," + ns.getNiceMax() + ")");
			String rs = "rep = c(0";
			for (int i = 1; i < nReplicates; i++)
				rs += "," + ((i * nPlots));
			rs += ")";
			result.add(rs);

			result.add("svg(\"" + rv + "_Series.svg\",width = 8, height = 8)");
			result.add("par(mfrow=c(" + rows + "," + cols + "))");
			result.add("par(mar = c(3, 3, 3, 1) + 0.1)");
			result.add("par(oma = c(1,1,2,0)+0.1)");

			for (int p = 0; p < nPlots; p++) {
				result.add("");
				result.add("rep = rep+1");
				result.add("s = data[rep]");
				result.add("avg = rowMeans(s)");
				result.add("range = apply(s,1,sd)");
				result.add("plot(avg,");
				result.add("\ttype = \"n\",");
				result.add("\tylim = ylim,");
				result.add("\tylab = '',");
				result.add("\txlab = '')");

				if (edt.equals(ExperimentDesignType.singleRun))
					result.add("mtext('',3,0)");
				else
					result.add("mtext(\"" + titles[p] + "\",3,0,cex=0.8)");
				result.add("lower = avg-range");
				result.add("upper = avg+range");
				result.add("x = seq(1,length(avg),1)");
				result.add("polygon(c(x,rev(x)),c(lower,rev(upper)),col=\"grey\",border=NA)");
				result.add("lines(avg,lty=1)");
			}
			result.add("mtext(\"" + rv + "[" + expName + "]\", side = 3, line = 0, outer = TRUE)");
			result.add("mtext(\"" + rv + "\", side = 2, line = 0, outer = TRUE)");
			result.add("mtext(\"time\",side = 1, line = 0, outer = TRUE)");

			result.add("invisible(dev.off())");

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Saves an R script to file then executes the script with Rscript. This method
	 * waits for the execution to finish before returning.
	 * 
	 * @param scriptFile The file and path name of the script.
	 * @param lines      An array of lines of the script.
	 * 
	 * @return true if successful, false otherwise
	 */
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

		try {
			b.start().waitFor();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}

	/**
	 * Determine a convenient number of rows and columns for R plot i.e.
	 * "{@code par(fmrow =
	 * c(rows,cols))}". Columns are increased before rows.
	 * 
	 * @param length number of items to arrange.
	 * @return number of rows ({@code getFirst()}) and cols ({@code getSecond()}).
	 */
	private static Duple<Integer, Integer> getRowColSize(int length) {
		int rows = (int) Math.max(1, Math.sqrt(length));
		int cols = rows;
		if (rows * cols < length)
			cols++;
		if (rows * cols < length)
			rows++;
		return new Duple<Integer, Integer>(rows, cols);
	}

//	public static void main(String[] args) {
//	File file = new File(
//			"/home/ian/3w/project_GDDMExp_2022-07-23-04-13-06-683/local/runTime/OG/popWriter0/population.csv");
//	List<String> lines = generateSeriesScript(ExperimentDesignType.crossFactorial ,file, "population", 5, false);
//	for (String line : lines)
//		System.out.println(line);
//
//}

}
