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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import au.edu.anu.rscs.aot.collections.tables.IntTable;
import au.edu.anu.rscs.aot.graph.property.Property;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.Output2DData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.experiment.ExpFactor;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.twuifx.widgets.WidgetUtils;
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
	final private Map<Integer, Map<Long, Number[][]>> senderSelectedData;
	private Interval zRange;
	private boolean doAverage;
	private int nCols;
	private int nRows;
	private boolean isSized;
	private String outputDir;
	private String precis;
	private final Palette palette = PaletteFactory.orangeMauveBlue();
	private int magnification;
	private List<List<Property>> treatmentList;
	private Map<String, ExpFactor> factors;
	private Map<String, Object> baseline;
	private int nReps;

	// TODO work out how we are to use ymuit palettes in the archetype?
	// TODO need settings to set colour to out of scale pixels as per matrix widget
	// TODO WE MUST SAVE WITH A SENDER ID!!!
	/**
	 * Constructor for a 2-D reading widget (Must be public).
	 * 
	 * @param statusSender The state machine
	 */
	public HLMatrixWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM2);
		recordTimes = new HashSet<>();
		senderSelectedData = new ConcurrentHashMap<>();
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
		if (properties.hasProperty(P_DESIGN_TYPE.key())) {
			treatmentList = (List<List<Property>>) properties.getPropertyValue("TreatmentList");
			factors = (Map<String, ExpFactor>) properties.getPropertyValue("Factors");
			baseline = (Map<String, Object>) properties.getPropertyValue("Baseline");
		}
		nReps = 1;
		if (properties.hasProperty(P_EXP_NREPLICATES.key()))
			nReps = (Integer) properties.getPropertyValue(P_EXP_NREPLICATES.key());
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
			int sender = data.sender();
			Map<Long, Number[][]> sd = senderSelectedData.get(sender);
			if (sd == null) {
				sd = new HashMap<>();
				senderSelectedData.put(sender, sd);
			}
			sd.put(data.time(), data.map());
		}

	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			senderSelectedData.clear();
		} else if (isSimulatorState(state, finished)) {
			writeData();
		}

	}

	/**
	 * Write the data {@link Number}[][]} to a tiff file using the current interval
	 * range, palette and magnification.
	 */
	private void writeData() {
		String widgetDirName = WidgetUtils.getUniqueExperimentSubdirectoryName(outputDir, widgetId);

		if (doAverage) {// maybe always both?
			senderSelectedData.forEach((k, v) -> {
				Number[][] avg = averageMatrices(v);
				writeTiff(widgetDirName, k.toString(), "avg", avg);

			});

		} else {
			senderSelectedData.forEach((k, v) -> {
				v.forEach((kk, vv) -> {
					writeTiff(widgetDirName, k.toString(), kk.toString(), vv);
				});
			});
		}
	}

	/**
	 * Create a matrix averaged over all recorded matrices for this sender's set of
	 * matrices.
	 * 
	 * @param v The set of matrices.
	 * 
	 * @return Average of the set.
	 */
	private Number[][] averageMatrices(Map<Long, Number[][]> v) {
		Number[][] result = new Number[nCols][nRows];
		int n = v.size();
		for (Number[][] value : v.values()) {
			for (int x = 0; x < nCols; x++)
				for (int y = 0; y < nRows; y++)
					result[x][y] = result[x][y].doubleValue() + value[x][y].doubleValue();
		}
		for (int x = 0; x < nCols; x++)
			for (int y = 0; y < nRows; y++)
				result[x][y] = result[x][y].doubleValue() / (double) n;
		return result;
	}

	private void writeTiff(String widgetDir, String sender, String name, Number[][] matrix) {
		File file = Project.makeFile(ProjectPaths.RUNTIME, outputDir, widgetDir, sender + "_" + name + ".tif");
		WidgetUtils.writeResizedMatrixToTiffFile(matrix, file, palette, zRange, magnification,
				BufferedImage.TYPE_INT_RGB, Image.SCALE_SMOOTH);
	}

}