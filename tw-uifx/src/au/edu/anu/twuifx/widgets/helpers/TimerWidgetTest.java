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
