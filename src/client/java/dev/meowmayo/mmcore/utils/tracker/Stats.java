package dev.meowmayo.mmcore.utils.tracker;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public abstract class Stats implements Serializable {
    public int totalRuns = 0;
    public int totalComps = 0;
    public double totalTime = 0.0;
    public double totalCompTime = 0.0;

    public double[] bestSplits;

    public double fastest = -1.0;
    public double fastestLag = -1.0;
    public double[] fastestSplits;
    public double[] fastestLagSplits;

    public double slowest = -1.0;
    public double slowestLag = -1.0;
    public double[] slowestSplits;
    public double[] slowestLagSplits;

    public double[] compSplitTimes;
    public double totalLag = 0.0;
    public double[] compLagTimes;

    public void reset() {
        totalRuns = 0;
        totalComps = 0;
        totalTime = 0.0;
        totalCompTime = 0.0;

        fastest = -1.0;
        fastestLag = -1.0;
        Arrays.fill(fastestSplits, 0);
        Arrays.fill(fastestLagSplits, 0);

        slowest = -1.0;
        slowestLag = -1.0;
        Arrays.fill(slowestSplits, 0);
        Arrays.fill(slowestLagSplits, 0);

        Arrays.fill(compSplitTimes, 0);
        totalLag = 0;
        Arrays.fill(compLagTimes, 0);
    }

    public abstract void addSuccess(
            long startTime,
            List<Long> rt,
            List<Long> lt
    );

    public void addFailure(
            long startTime
    ) {
        this.totalRuns++;
        this.totalTime += (System.currentTimeMillis() - startTime) / 1000.0;
    }

    public double getRemaining(int phase) {
        double sum = 0.0;
        for (int i = phase; i < bestSplits.length; i++) {
            if (bestSplits[i] == Double.MAX_VALUE) {
                return 0.0;
            }
            sum += bestSplits[i];
        }
        return sum;
    }
}
