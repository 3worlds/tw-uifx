package au.edu.anu.twuifx.widgets;

import java.util.Random;

public class timetest {

	private static int delay(long d) {
		long s = System.currentTimeMillis();
		long e = System.currentTimeMillis();
		Random r = new Random();
		double sum = 0;
		int i = 0;
		while (e - s < d) {
			sum += r.nextDouble();
			e = System.currentTimeMillis();
			i++;
		}
		return i;
	}

	public static void main(String[] args) {
		System.out.println(delay(3));
	}

}
