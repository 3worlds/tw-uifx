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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 * @author Ian Davies 24 Apr 2022
 */

public class DelayTask {
	private DelayTask() {
	};

	/**
	 * Prevents animation tasks from flickering. Tasks are placed in a Timeline to
	 * be run at some time in the future ('head') at a rate no greater than the
	 * value of 'gap'. 
	 * <p>
	 * If the generation of tasks is faster than the 'ga'Using this method can cause animations to lag way behind their
	 * computation. Typically, 'gap' need be no greater than 1 ms.
	 * </p>
	 * <p>
	 * This method should only be called from the application thread to prevent
	 * instances of Timeline from throwing concurrent modification exceptions.
	 * </p>
	 * 
	 * @param gap  time in ms to advance the time front. Must be > 0
	 * @param head the current value of the time front
	 * @param task submitted task to run at that time.
	 * @return updated value of time front.
	 */
	public static long submit(final long gap, long head, EventHandler<ActionEvent> task) {
		assert(gap>0);
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
