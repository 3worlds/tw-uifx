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
package au.edu.anu.twuifx.graphState;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.rscs.aot.errorMessaging.ErrorMessageManager;
import au.edu.anu.twcore.errorMessaging.ModelBuildErrorMsg;
import au.edu.anu.twcore.errorMessaging.ModelBuildErrors;
import au.edu.anu.twcore.graphState.IGraphState;
import au.edu.anu.twcore.graphState.IGraphStateListener;
import au.edu.anu.twcore.project.Project;
import au.edu.anu.twuifx.mm.view.DefaultWindowSettings;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Author Ian davies
 *
 * Date May 6, 2019
 */
public class GraphStatefx implements IGraphState {
	private BooleanProperty propertyHasChanged = new SimpleBooleanProperty(false);
	private StringProperty propertyTitle = new SimpleStringProperty("");
	private StringProperty propertyJavaPath = new SimpleStringProperty("");
	private List<IGraphStateListener> listeners = new ArrayList<>();

	public GraphStatefx(StringProperty propertyTitle, StringProperty propertyJavaPath) {
		if (propertyTitle != null)
			this.propertyTitle = propertyTitle;
		if (propertyJavaPath != null)
			this.propertyJavaPath = propertyJavaPath;
		propertyJavaPath.addListener(new ChangeListener<String>() {

			@Override
			public void changed(@SuppressWarnings("rawtypes") ObservableValue observable, String oldValue,
					String newValue) {
				setTitle();
			}
		});

	}

	private void setTitle() {
		// TODO: needs checking!
		Platform.runLater(() -> {
			String title = null;
			if (Project.isOpen()) {
				title = Project.getDisplayName();
				if (changed())
					title = "*" + title;
				if (!propertyTitle.getValue().isEmpty()) {
					if (!propertyJavaPath.getValue().isEmpty()) {
						title = title + "<o-o-o>" + propertyJavaPath.get();
					}
					propertyTitle.setValue(title);
				}
			} else
				propertyTitle.setValue(DefaultWindowSettings.defaultName());
			onChange();
		});

	}

	@Override
	public boolean changed() {
		return propertyHasChanged.getValue();
	}

	@Override
	public void setChanged() {
		propertyHasChanged.setValue(true);
		if (!ErrorMessageManager.haveErrors())
			ErrorMessageManager.dispatch(new ModelBuildErrorMsg(ModelBuildErrors.DEPLOY_PROJECT_UNSAVED));
		else
			onChange();
		setTitle();
	}

	@Override
	public void clear() {
		propertyHasChanged.setValue(false);
		setTitle();
	}

	@Override
	public void addListener(IGraphStateListener l) {
		listeners.add(l);

	}

	@Override
	public void onChange() {
		for (IGraphStateListener l : listeners)
			l.onStateChange(propertyHasChanged.getValue());

	}

}
