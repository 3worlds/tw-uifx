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
package au.edu.anu.twuifx.widgets;

import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_NTU;
import static fr.cnrs.iees.twcore.constants.ConfigurationPropertyNames.P_TIMEMODEL_TU;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import au.edu.anu.twcore.data.runtime.DataLabel;
import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeData;
import au.edu.anu.twcore.data.runtime.TimeSeriesData;
import au.edu.anu.twcore.data.runtime.TimeSeriesMetadata;
import au.edu.anu.twcore.ecosystem.runtime.timer.TimeUtil;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.AbstractDataSet;
import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;

/**
 * @author Ian Davies
 *
 * @date 29 Oct 2019
 * 
 *       Trial of chart-fx based on the "RollingBufferSample"
 */
public class SimpleTimeSeriesWidget2 extends AbstractDisplayWidget<TimeSeriesData, Metadata> implements Widget {

	private int BUFFER_CAPACITY = 750;// pref mm/mr or both
	// drop overlayed points
	private int MIN_PIXEL_DISTANCE = 0;
	private int N_SAMPLES = 3000;// what is this?
	private int UPDATE_PERIOD = 40; // [ms] is this required?
	// these should be created in metadata msg or getUserInterfaceContainer ?
	// That is, they must be created after fx application thread is running.
	private Timer timer;// dont need this
	private BorderPane content;
	private XYChart chart;
	private List<CircularDoubleErrorDataSet> seriesList;
	private TimeSeriesMetadata tsmeta;
	private WidgetTimeFormatter timeFormatter;
	private WidgetTrackingPolicy<TimeData> policy;

	protected SimpleTimeSeriesWidget2(StateMachineEngine<StatusWidget> statusSender, int dataType) {
		super(statusSender, dataType);
		seriesList = new ArrayList<>();
		timeFormatter = new WidgetTimeFormatter();
		policy = new SimpleWidgetTrackingPolicy();
	}

	@Override
	public void setProperties(String id, SimplePropertyList properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getPreferences() {
		// TODO Auto-generated method stub
		// on application thread AFTER getUserInterfaceContainer

	}

	@Override
	public void onDataMessage(TimeSeriesData data) {
		// TODO Auto-generated method stub

	}

	private boolean initialMessage = false;

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// clear data from last run - here we could use the lovely 'history' method
		// supplied with chartfx - TODO
		for (CircularDoubleErrorDataSet series : seriesList)
			series.reset();

		// this occurs EVERY reset so take care not to recreate axis etc
		if (!initialMessage) {
			createChart(meta);
			content.setCenter(chart);
			initialMessage = true;
		}
	}

	private void createChart(Metadata meta) {
		// Platform.runLater(() -> {
		// get all names in the list as before
		tsmeta = (TimeSeriesMetadata) meta.properties().getPropertyValue(TimeSeriesMetadata.TSMETA);
		List<ErrorDataSetRenderer> renderers = new ArrayList<>();
		for (DataLabel dl : tsmeta.doubleNames()) {
			String key = dl.toString();
			CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSet("name [units]", BUFFER_CAPACITY);
			seriesList.add(ds);
			ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
			initErrorDataSetRenderer(renderer);
			renderer.getDatasets().add(ds);
			renderers.add(renderer);
		}
		for (DataLabel dl : tsmeta.intNames()) {
			String key = dl.toString();
			CircularDoubleErrorDataSet ds = new CircularDoubleErrorDataSet("name [units]", BUFFER_CAPACITY);
			seriesList.add(ds);
			ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
			initErrorDataSetRenderer(renderer);
			renderer.getDatasets().add(ds);
			renderers.add(renderer);

		}
		timeFormatter.onMetaDataMessage(meta);
		TimeUnits tu = (TimeUnits) meta.properties().getPropertyValue(P_TIMEMODEL_TU.key());
		int nTu = (Integer) meta.properties().getPropertyValue(P_TIMEMODEL_NTU.key());
		
		DefaultNumericAxis yAxis1 = new DefaultNumericAxis("ylabel", "yunits");
		DefaultNumericAxis xAxis1 = new DefaultNumericAxis("time", TimeUtil.timeUnitName(tu, nTu));
		xAxis1.setAutoRangeRounding(false);
		xAxis1.setTickLabelRotation(45);
		xAxis1.setMinorTickCount(30);
		xAxis1.invertAxis(false);
		xAxis1.setTimeAxis(true);
		yAxis1.setForceZeroInRange(true);
		yAxis1.setAutoRangeRounding(true);
		//yAxis1.getAxisLabel().setText(arg0);we can create the chart and axis earilier
		//yAxis1.setUnit(value);
		
		// can't create a chart without axes
		chart = new XYChart(xAxis1, yAxis1);
		chart.legendVisibleProperty().set(true);
		chart.setAnimated(false);
		chart.setTitle("title");
		
		for (ErrorDataSetRenderer r : renderers)
			chart.getRenderers().add(r);
	}

	private void initErrorDataSetRenderer(final ErrorDataSetRenderer r) {
		r.setErrorType(ErrorStyle.NONE);
		r.setDashSize(MIN_PIXEL_DISTANCE);
		r.setPointReduction(true);
		r.setDrawMarker(false);
		DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) r.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(MIN_PIXEL_DISTANCE);
	}

	@Override
	public void onStatusMessage(State state) {
		// TODO Auto-generated method stub
		// on Finished push the last data from buffer to chart

	}

	@Override
	public Object getUserInterfaceContainer() {
		content = new BorderPane();
		return content;
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
	}

}
