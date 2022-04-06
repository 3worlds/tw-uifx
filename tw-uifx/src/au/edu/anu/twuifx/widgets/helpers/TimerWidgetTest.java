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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TimerWidgetTest {
	private final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
	private final Timer timer = new Timer();
	private final TimerTask task;
	private final long delay;

	public TimerWidgetTest(long delay) {
		this.delay = delay;
		task = new TimerTask() {

			@Override
			public void run() {
				Integer i = queue.poll();
				if (i != null)
					System.out.println(i + "\t of size " + queue.size() + "\t at " + System.currentTimeMillis());
			}
		};

	}

	public void start() {
		System.out.println("Starting timer");
		timer.scheduleAtFixedRate(task, 0, delay);
	}

	public void stop() {
		System.out.println("Stopping timer");
		timer.cancel();
	}

	public void onDataMessage(Integer i) {
		try {
			queue.put(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	};

	public static void main(String[] args) {
		TimerWidgetTest w = new TimerWidgetTest(40);// 25Hz
		w.start();
		for (int i = 0; i <1000; i++)
			w.onDataMessage(i);
		try {
			Thread.sleep(10000);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		w.clear();
		w.stop();

	}

	private void clear() {
		queue.clear();
		
	}

}
