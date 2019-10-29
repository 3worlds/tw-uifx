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

import java.util.Timer;

import au.edu.anu.twcore.data.runtime.Metadata;
import au.edu.anu.twcore.data.runtime.TimeSeriesData;
import au.edu.anu.twcore.ui.runtime.AbstractDisplayWidget;
import au.edu.anu.twcore.ui.runtime.StatusWidget;
import au.edu.anu.twcore.ui.runtime.Widget;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.CircularDoubleErrorDataSet;
import fr.cnrs.iees.properties.SimplePropertyList;
import fr.cnrs.iees.rvgrid.statemachine.State;
import fr.cnrs.iees.rvgrid.statemachine.StateMachineEngine;

/**
 * @author Ian Davies
 *
 * @date 29 Oct 2019
 * 
 * Trial of chart-fx based on the "RollingBufferSample"
 */
public class SimpleTimeSeriesWidget2 extends AbstractDisplayWidget<TimeSeriesData, Metadata> implements Widget {

	private int BUFFER_CAPACITY = 750;// pref mm/mr or both
	// drop overlayed points
	private int MIN_PIXEL_DISTANCE = 0;
	private int  N_SAMPLES = 3000;// what is this?
	private int UPDATE_PERIOD = 40; // [ms] is this required?
	// these should be created in metadata msg or getUserInterfaceContainer ?
	// That is, they must be created after fx application thread is running.
	private CircularDoubleErrorDataSet d1 = new CircularDoubleErrorDataSet("name [units]",BUFFER_CAPACITY);
	private ErrorDataSetRenderer d1Renderer = new ErrorDataSetRenderer();
	private DefaultNumericAxis yAxis1 = new DefaultNumericAxis("label","units");
	private Timer timer;// dont need this
	
	private void initErrorDataSetRenderer(final ErrorDataSetRenderer r) {
		r.setErrorType(ErrorStyle.NONE);
		r.setDashSize(MIN_PIXEL_DISTANCE);
		r.setPointReduction(true);
		r.setDrawMarker(false);
		DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) r.getRendererDataReducer();
		reductionAlgorithm.setMinPointPixelDistance(MIN_PIXEL_DISTANCE);
	}
	protected SimpleTimeSeriesWidget2(StateMachineEngine<StatusWidget> statusSender, int dataType) {
		super(statusSender, dataType);
		// TODO Auto-generated constructor stub
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

	@Override
	public void onMetaDataMessage(Metadata meta) {
		// TODO Auto-generated method stub
		// this occurs EVERY reset so take care not to recreate axis etc
		
	}

	@Override
	public void onStatusMessage(State state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getUserInterfaceContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getMenuContainer() {
		// TODO Auto-generated method stub
		return null;
	}

}
