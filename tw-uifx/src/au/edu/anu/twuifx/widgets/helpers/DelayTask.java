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

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.*;
import javafx.util.Duration;

/**
 * Static class to prevent animation tasks flickering. This can occur when many
 * tasks are rapidly submitted to Platform.runLater(). 'gap'.
 * 
 * @author Ian Davies 24 Apr 2022
 */

public class DelayTask {
	// prevent instantiation
	private DelayTask() {
	};

	/**
	 * To prevent flicker, tasks are placed in a Timeline to be run at
	 * some time in the future ('head') at a rate no greater than the value of
	 * 'gap'.
	 * <p>
	 * If the generation of tasks is faster than 'gap', animations will lag behind
	 * their computation, sometimes very noticeably. Typically, 'gap' need be no greater than 1 ms.
	 * </p>
	 * <p>
	 * NOTE: To prevent concurrent modification exceptions, this method must only be
	 * called from the application thread.
	 * </p>
	 * 
	 * @param gap  time in ms to advance the time front. Must be > 0
	 * @param head the current value of the time front
	 * @param task submitted task to run at that time.
	 * @return updated value of time front.
	 */
	public static long submit(long gap, long head, EventHandler<ActionEvent> task) {
		assert (Platform.isFxApplicationThread());
		gap = Math.max(0, 1L);
		long now = System.currentTimeMillis();
		head = Math.max(head, now);
		head += gap;
		long delay = head - now;
		Timeline timeline = new Timeline();
		KeyFrame keyFrame = new KeyFrame(Duration.millis(delay), task);
		timeline.getKeyFrames().add(keyFrame);
		timeline.play();
		return head;
	}

}
