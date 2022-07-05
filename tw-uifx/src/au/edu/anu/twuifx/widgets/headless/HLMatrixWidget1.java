package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import au.edu.anu.rscs.aot.collections.tables.IntTable;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.Output2DData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.ymuit.ui.colour.Palette;
import au.edu.anu.ymuit.ui.colour.PaletteFactory;
import fr.cnrs.iees.identity.impl.LocalScope;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Interval;

/**
 *
 * A headless widget to save a matrix as a tiff file. It must have properties
 * describing:
 * <ol>
 * <li>min and max range of data;</li>
 * <li>image magnification factor;</li>
 * <li>background colour for out-of-range values (one colour each for lower and
 * upper bound; and</li>
 * <li>palette.</li>
 * </ol>
 * 
 * Requirements:
 * <ol>
 * <li>Selected matrices are recorded along with the time step in a
 * {@literal Map<>}</li>
 * <li>The maps can be averaged to one map or saved individually.</li>
 * <li>The file name is
 * {@literal <experimentDirectory>/<widgetId><n>/<time-step >}.tif; where
 * {@literal <n>} is the unique instance of the experiment. e.g.
 * exp0/FirePatterns3/12.tif</li>
 * <li>All are saved in a directory with the widget's id.</li>
 * </ol>
 * 
 * @author Ian Davies - 1 July 2022
 */
public class HLMatrixWidget1 extends AbstractDisplayWidget<Output2DData, Metadata> implements Widget {
	private String widgetId;
	final private Set<Long> recordTimes;
	final private Map<Long, Number[][]> selectedData;
	private Interval zRange;
	private boolean doAverage;
	private int nCols;
	private int nRows;
	private String outputDir;
	private String precis;
	private final Palette palette = PaletteFactory.orangeMauveBlue();
	private boolean isSized;
	private int magnification;

	// TODO work out how we are to use ymuit palettes in the archetype?
	// TODO need settings to set colour to out of scale pixels as per matrix widget
	/**
	 * Constructor for a 2-D reading widget (Must be public).
	 * 
	 * @param statusSender The state machine
	 */
	public HLMatrixWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM2);
		recordTimes = new HashSet<>();
		selectedData = new HashMap<>();
		isSized = false;
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		widgetId = id;
		IntTable t = (IntTable) properties.getPropertyValue(P_WIDGET_SAMPLETIMES.key());
		for (int i = 0; i < t.size(); i++) {
			int in = t.getByInt(i);
			long l = in;
			recordTimes.add(l);
		}
		doAverage = (Boolean) properties.getPropertyValue(P_WIDGET_ASAVERAGE.key());
		outputDir = (String) properties.getPropertyValue(P_EXP_DIR.key());
		precis = (String) properties.getPropertyValue(P_EXP_PRECIS.key());
		zRange = (Interval) properties.getPropertyValue(P_WIDGET_DEFAULT_Z_RANGE.key());
		magnification = Math.max(1, (Integer) properties.getPropertyValue(P_WIDGET_IMAGEMAGNIFICATION.key()));
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
	}

	@Override
	public void onDataMessage(Output2DData data) {
		if (recordTimes.contains(data.time())) {
			if (!isSized) {
				nCols = data.map().length;
				nRows = data.map()[0].length;
				isSized = true;
			}
			selectedData.put(data.time(), data.map());
		}

	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			selectedData.clear();
		} else if (isSimulatorState(state, finished)) {
			writeData();
		}

	}

	/**
	 * Write the data {@code Number[][]} to a tiff file using the current interval
	 * range, palette and magnification.
	 */
	private void writeData() {
		LocalScope scope = new LocalScope("Files");
		File dir = Project.makeFile(ProjectPaths.RUNTIME, outputDir);
		dir.mkdirs();
		for (String fileName : dir.list()) {
			int dotIndex = fileName.lastIndexOf('.');
			fileName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
			scope.newId(true, fileName);
		}
		String widgetDirName = scope.newId(false, widgetId + "0").id();

		if (doAverage) {// maybe always both?
			Number[][] avg = averageMatrices();
			writeTiff(widgetDirName, "avg", avg);

		} else {
			selectedData.forEach((k, v) -> {
				writeTiff(widgetDirName, k.toString(), v);

			});

		}
	}

	/**
	 * Create a matrix averaged over all recorded matrices.
	 * 
	 * @return The average.
	 */
	private Number[][] averageMatrices() {
		Number[][] result = new Number[nCols][nRows];
		int n = selectedData.size();
		for (Number[][] value : selectedData.values()) {
			for (int x = 0; x < nCols; x++)
				for (int y = 0; y < nRows; y++)
					result[x][y] = result[x][y].doubleValue() + value[x][y].doubleValue();
		}
		for (int x = 0; x < nCols; x++)
			for (int y = 0; y < nRows; y++)
				result[x][y] = result[x][y].doubleValue() / (double) n;
		return result;
	}

	/**
	 * Resize the image to the widget's magnification factor (integer)
	 * 
	 * @param img The original image.
	 * @return The rescaled image.
	 * @see <a href="https://www.tabnine.com/code/java/methods/java.awt.image.BufferedImage/getScaledInstance"> scaling a tiff file.</a>
	 *
	 */
	private BufferedImage resize(BufferedImage img) {
		int newW = nCols * magnification;
		int newH = nRows * magnification;
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage result = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = result.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		return result;
	}

	/**
	 * Write the data to a tiff file.
	 * 
	 * @param widgetDir A unique name for this writing instance.
	 * @param name      The file name. It is the time step the data was recorded or
	 *                  "avg" if an average of all data.
	 * @param matrix    The data array.
	 */
	private void writeTiff(String widgetDir, String name, Number[][] matrix) {
		double min = zRange.inf();
		double max = zRange.sup();
		BufferedImage bi = new BufferedImage(nCols, nRows, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		for (int x = 0; x < nCols; x++)
			for (int y = 0; y < nRows; y++) {
				javafx.scene.paint.Color fx = palette.getColour(matrix[x][y].doubleValue(), min, max);
				java.awt.Color awtColor = new java.awt.Color((float) fx.getRed(), (float) fx.getGreen(),
						(float) fx.getBlue(), (float) fx.getOpacity());
				g.setColor(awtColor);
				g.drawLine(x, y, x, y);
			}
		File file = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDir, name + ".tif");
		file.getParentFile().mkdirs();
		try {
			ImageIO.write(resize(bi), "tif", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}