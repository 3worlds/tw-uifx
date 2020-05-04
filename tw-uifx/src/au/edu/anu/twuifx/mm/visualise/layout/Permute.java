package au.edu.anu.twuifx.mm.visualise.layout;

//Java program to print all 
//permutations using Johnson 
//and Trotter algorithm. 
import java.util.*;

import au.edu.anu.omhtk.rng.Pcg32;

public class Permute {
	public static Integer[] shuffle(int n) {
		Random rnd = new Pcg32();
		List<Integer> bag = new ArrayList<>();
		List<Integer> out = new  ArrayList<>();
		for (int i = 0;i<n;i++)
			bag.add(i);
		while (!bag.isEmpty()) {
			int idx = rnd.nextInt(bag.size());
			int x = bag.get(idx);
			if (!out.contains(x)) {
				out.add(x);
				bag.remove(idx);
			}
		}
		return out.toArray(new Integer[0]);
	}
	

	//Johnson and Trotter algorithm
	public static int[][] getPemutationIndices(int n) {
		int[][] result = new int[fact(n)][n];
		int[] a = new int[n];
		boolean[] dir = new boolean[n];
		for (int i = 0; i < n; i++) {
			a[i] = i + 1;
			result[0][i] = i;// zero counting
		}

		for (int i = 0; i < n; i++)
			dir[i] = RIGHT_TO_LEFT;

		for (int i = 1; i < fact(n); i++) {
			doOnePerm(a, dir, n);
			for (int j = 0; j < n; j++) {
				result[i][j] = a[j]-1;// zero counting
			}
		}
		return result;
	}

	private final static boolean LEFT_TO_RIGHT = true;
	private final static boolean RIGHT_TO_LEFT = false;

	private static int searchArr(int a[], int n, int mobile) {
		for (int i = 0; i < n; i++)

			if (a[i] == mobile)
				return i + 1;

		return 0;
	}

	/**
	 * To carry out step 1 of the algorithm i.e. to find the largest mobile integer.
	 */
	private static int getMobile(int a[], boolean dir[], int n) {
		int mobile_prev = 0, mobile = 0;

		for (int i = 0; i < n; i++) {
			// direction 0 represents
			// RIGHT TO LEFT.
			if (dir[a[i] - 1] == RIGHT_TO_LEFT && i != 0) {
				if (a[i] > a[i - 1] && a[i] > mobile_prev) {
					mobile = a[i];
					mobile_prev = mobile;
				}
			}

			// direction 1 represents
			// LEFT TO RIGHT.
			if (dir[a[i] - 1] == LEFT_TO_RIGHT && i != n - 1) {
				if (a[i] > a[i + 1] && a[i] > mobile_prev) {
					mobile = a[i];
					mobile_prev = mobile;
				}
			}
		}

		if (mobile == 0 && mobile_prev == 0)
			return 0;
		else
			return mobile;
	}

	
	private static int doOnePerm(int a[], boolean dir[], int n) {
		int mobile = getMobile(a, dir, n);
		int pos = searchArr(a, n, mobile);

		// swapping the elements
		// according to the
		// direction i.e. dir[].
		if (dir[a[pos - 1] - 1] == RIGHT_TO_LEFT) {
			int temp = a[pos - 1];
			a[pos - 1] = a[pos - 2];
			a[pos - 2] = temp;
		} else if (dir[a[pos - 1] - 1] == LEFT_TO_RIGHT) {
			int temp = a[pos];
			a[pos] = a[pos - 1];
			a[pos - 1] = temp;
		}

		// changing the directions
		// for elements greater
		// than largest mobile integer.
		for (int i = 0; i < n; i++) {
			if (a[i] > mobile) {
				if (dir[a[i] - 1] == LEFT_TO_RIGHT)
					dir[a[i] - 1] = RIGHT_TO_LEFT;

				else if (dir[a[i] - 1] == RIGHT_TO_LEFT)
					dir[a[i] - 1] = LEFT_TO_RIGHT;
			}
		}

		return 0;
	}
	private static int fact(int n) {
		int res = 1;

		for (int i = 1; i <= n; i++)
			res = res * i;
		return res;
	}

	public static void main(String argc[]) {
		int n = 6;
		int[][] p = getPemutationIndices(n);
		System.out.println(Arrays.deepToString(p));
	}

}

//This code is contributed by Sagar Shukla 
