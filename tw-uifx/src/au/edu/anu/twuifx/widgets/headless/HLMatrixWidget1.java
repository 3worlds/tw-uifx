package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.*;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.*;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import au.edu.anu.rscs.aot.collections.tables.IntTable;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.Output2DData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.experiment.runtime.EddReadable;
import au.edu.anu.twcore.experiment.runtime.ExperimentDesignDetails;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twcore.project.ProjectPaths;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.twuifx.widgets.WidgetUtils;
import au.edu.anu.ymuit.ui.colour.Palette;
import au.edu.anu.ymuit.ui.colour.PaletteFactory;
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
//	private String outputDir;
//	private String precis;
	private final Palette palette = PaletteFactory.orangeMauveBlue();
	private int magnification;
	private EddReadable edd;

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
		edd = (ExperimentDesignDetails) properties.getPropertyValue(P_EXP_DETAILS.key());
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
		String widgetDirName = WidgetUtils.getUniqueExperimentSubdirectoryName(edd.getExpDir(), widgetId);

		File designFile = Project.makeFile(ProjectPaths.RUNTIME, edd.getExpDir(), widgetDirName, "Design.csv");
		WidgetUtils.SaveExperimentDesignDetails(edd, designFile);

		if (doAverage) {// maybe always both?
			senderSelectedData.forEach((sender, sampleData) -> {
				int rep = sender / edd.getTreatments().size();
				Number[][] matrix = averageMatrices(sampleData);
				writeTiff(widgetDirName, rep, sender, "avg", getTreatmentDescriptor(sender), matrix);

			});

		} else {
			senderSelectedData.forEach((sender, sampleData) -> {
				int rep = sender / edd.getTreatments().size();
				sampleData.forEach((timeStep, matrix) -> {
					writeTiff(widgetDirName, rep, sender, timeStep.toString(), getTreatmentDescriptor(sender), matrix);
				});
			});
		}
	}

	private String getTreatmentDescriptor(Integer sender) {
		// TODO Handle exp from file
		switch (edd.getType()) {
		case crossFactorial: {
			return WidgetUtils.getXFDescriptor(edd.getTreatments().get(sender % edd.getTreatments().size()), edd.getFactors());
		}
		case sensitivityAnalysis: {
			// ????
			return WidgetUtils.getXFDescriptor(edd.getTreatments().get(sender % edd.getTreatments().size()), edd.getFactors());

		}
		default:
			return "";
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

	private void writeTiff(String widgetDir, int rep, int sender, String prefix, String descriptor, Number[][] matrix) {
		File file = Project.makeFile(ProjectPaths.RUNTIME, edd.getExpDir(), widgetDir,
				"r" + rep + "_s" + sender + "_t" + prefix + descriptor + ".tif");
		WidgetUtils.writeResizedMatrixToTiffFile(matrix, file, palette, zRange, magnification,
				BufferedImage.TYPE_INT_RGB, Image.SCALE_SMOOTH);
	}

}