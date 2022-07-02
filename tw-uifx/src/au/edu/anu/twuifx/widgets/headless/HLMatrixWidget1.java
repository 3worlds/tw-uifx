package au.edu.anu.twuifx.widgets.headless;

import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.finished;
import static au.edu.anu.twcore.ecosystem.runtime.simulator.SimulatorStates.waiting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import au.edu.anu.rscs.aot.collections.tables.IntTable;
import au.edu.anu.rscs.aot.collections.tables.LongTable;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.Output2DData;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.ecosystem.runtime.tracking.DataMessageTypes;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import au.edu.anu.twuifx.widgets.helpers.SimCloneWidgetTrackingPolicy;
import au.edu.anu.twuifx.widgets.helpers.WidgetTrackingPolicy;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.ens.biologie.generic.utils.Interval;

/**
 * <p>
 * A headless widget to save images of a matrix. It must have properties to the:
 * 
 * <li>time step(s) to save;
 * <li>min and max range of data;
 * <li>palette; and,
 * <li>images are saved as tiff files.
 * </p>
 * <p>
 * Requirements:
 * 
 * <li>Selected matrices are saved with their time step in a Map<> time step.
 * <li>The maps can be averaged to one map or saved individually.
 * <li>The file name is
 * {@literal <}time-step{@literal >}_{@literal <}matrixName{@literal >}.tif;
 * <li>All are saved in a directory with the widget's id.
 * 
 * @author Ian Davies - 1 July 2022
 */
public class HLMatrixWidget1 extends AbstractDisplayWidget<Output2DData, Metadata> implements Widget {
	private String widgetId;
	final private Set<Long> recordTimes;
	final private Map<Long, Number[][]> selectedData;
	private Interval defaultRange;
	private boolean doAverage;

	protected HLMatrixWidget1(StateMachineEngine<StatusWidget> statusSender) {
		super(statusSender, DataMessageTypes.DIM2);
		recordTimes = new HashSet<>();
		selectedData = new HashMap<>();
	}

	@Override
	public void onDataMessage(Output2DData data) {
		if (recordTimes.contains(data.time())) {
			selectedData.put(data.time(), data.map());
		}
		;
	}

	@Override
	public void onMetaDataMessage(Metadata meta) {
		System.out.println(meta);
	}

	@Override
	public void onStatusMessage(State state) {
		if (isSimulatorState(state, waiting)) {
			// clear everything
		} else if (isSimulatorState(state, finished)) {
			writeData();
		}

	}

	private void writeData() {
		if (doAverage) {

		} else {

		}

	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		widgetId = id;
		IntTable t = (IntTable) properties.getPropertyValue("recordTimes");
		for (int i = 0; i < t.size(); i++) {
			int in = t.getByInt(i);
			long l = in;
			recordTimes.add(l);
		}
		doAverage = (Boolean) properties.getPropertyValue("averageMaps");
	}

}
