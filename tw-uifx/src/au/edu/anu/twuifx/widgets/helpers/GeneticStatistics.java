package au.edu.anu.twuifx.widgets.helpers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GeneticStatistics {
	final List<List<Long>> pool;
	final List<Map<Long, Integer>> alleleSums;

	public GeneticStatistics(int nLoci) {
		this.pool = new ArrayList<>();
		alleleSums = new ArrayList<>();
		for (int locus = 0; locus < nLoci; locus++) {
			pool.add(new ArrayList<Long>());
			alleleSums.add(new HashMap<Long, Integer>());
		}
	}

	public void recordAllele(int locus, long allele) {
		List<Long> alleleValues = pool.get(locus);
		alleleValues.add(allele);
		Map<Long, Integer> sums = alleleSums.get(locus);
		Integer sum = 0;
		if (sums.containsKey(allele))
			sum = sums.get(allele);
		sum++;
		sums.put(allele, sum);
	}

	public double[] getHeterozygosity(int locus) {
		double[] result = new double[2];
		List<Long> alleleValues = pool.get(locus);
		double n = alleleValues.size();
		Map<Long, Integer> sums = alleleSums.get(locus);
		double sumSqrs = 0;
		for (Map.Entry<Long, Integer> entry : sums.entrySet()) {
			Integer sum = entry.getValue();
			double f = (double) sum / n;
			sumSqrs += f * f;
		}
		result[0]= (1.0 - sumSqrs);
		result[1] = sums.size();
		return result;

	}
	public double[] getMeanHeterozygosity() {
		double[] result = new double[2];
		double sumH = 0;
		double nUniqueAlleles = 0;
		//For each locus, sum h and the population of allele types.
		for (int position = 0; position < pool.size(); position++) {
			double [] h = getHeterozygosity(position);
			nUniqueAlleles += h[1];
			sumH += h[0];
		}
		// return average H
		result[0] = sumH / pool.size();
		result[1] = nUniqueAlleles;
		return result;
	}

}
