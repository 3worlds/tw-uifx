package au.edu.anu.twuifx.widgets.headless;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import au.edu.anu.omhtk.rng.Pcg32;
import fr.ens.biologie.generic.utils.Duple;
import fr.ens.biologie.generic.utils.Tuple;

public class Individual {
	public static int nBids = 1;
	private int loc;
	
	public Individual(int loc) {
		this.loc = loc;
	}


	private int getLocation() {
		return loc;
	}

	private static int[] runOnce(Random rng, int[] pop, boolean[] habitat, int size, double dd) {
		double k1 = 3.0;
		double k2 = 1.5;

		List<Individual> idvs = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			for (int p = 0; p < pop[i]; p++) {
				idvs.add(new Individual(i));
			}
		}

		int[] result = new int[pop.length];
		for (int i = 0; i < size; i++)
			result[i] = 0;

		for (Individual i : idvs) {
			List<Duple<Integer, Double>> sites = new ArrayList<>();
			for (int b = 0; b < nBids; b++) {
//				int bl = rng.nextInt(size);
				double distance = -(dd)*Math.log(1-rng.nextDouble());
				if (rng.nextBoolean())
					distance = -distance;
				int bl = (int)Math.round(distance) % size;
				if (bl<0)
					bl+=size;
				
				int p = pop[bl];
				double supply = k2;
				if (habitat[bl])
					supply = k1;
				double demand = pop[bl];
				double avail = supply - demand;
				sites.add(new Duple<Integer, Double>(bl, avail));
			}
			sites.sort(new Comparator<Duple<Integer, Double>>() {
				@Override
				public int compare(Duple<Integer, Double> o1, Duple<Integer, Double> o2) {
					return o2.getSecond().compareTo(o1.getSecond());
				}
			});
			Duple<Integer, Double> loc = sites.get(0);
			double limit = k2;
			if (habitat[loc.getFirst()])
				limit = k1;
			if (result[loc.getFirst()]<limit)
					result[loc.getFirst()]++;
			pop[i.getLocation()]--;
			pop[loc.getFirst()]++;
		}
		int tg = 0;
		int tb = 0;
		for (int i = 0; i < size; i++) {
			if (habitat[i]) {
				tg += result[i];
			} else {
				tb += result[i];
			}
		}

		System.out.println(tb+"\t"+tg);
		return result;
	}

	public static void main(String[] args) {
		double k1 = 3.0;
		double k2 = 1.5;
		double dd = 10;
		int size = 10000;
		int nSteps = 1000;
		int habitatRate = 1;
		System.out.println("Bad\tGood");
		Random rng = new Pcg32();
		boolean[] habitat = new boolean[size];
		int[] pop = new int[size];
		for (int i = 0; i < size; i++)
			pop[i] = rng.nextInt(3);

		for (int r = 0; r < nSteps; r++) {
			if (r  % habitatRate ==0)
				for (int i = 0; i < size; i++)
					habitat[i] = rng.nextBoolean();
			pop = runOnce(rng, pop, habitat, size,dd);
		}

	}

}
