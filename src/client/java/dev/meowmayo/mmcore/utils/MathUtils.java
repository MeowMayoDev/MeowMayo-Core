package dev.meowmayo.mmcore.utils;

import java.util.ArrayList;

public class MathUtils {
    public static double get3dDistance(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        double dz = a[2] - b[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static double get2dDistanceSq(float x1, float y1, float x2, float y2) {
        return Math.pow(x1 - x2, 2) +
                Math.pow(y1 - y2, 2);
    }

    public static double get2dDistance(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) +
                Math.pow(y1 - y2, 2));
    }

    public static int roundUpToFive(double value) {
        return (int) (Math.ceil(value / 5.0) * 5);
    }

    public static double calculateStandardDeviationL(ArrayList<Long> data) {
        long sum = 0;
        double mean = 0.0;
        double standardDeviation = 0.0;
        int n = data.size();

        if (n == 0) {
            return Double.NaN;
        }

        for (long num : data) {
            sum += num;
        }
        mean = sum / (double) (n);

        for (double num : data) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        double variance = standardDeviation / n;

        return Math.sqrt(variance);
    }

    public static double calculateStandardDeviationI(ArrayList<Integer> data) {
        int sum = 0;
        double mean = 0.0;
        double standardDeviation = 0.0;
        int n = data.size();

        if (n == 0) {
            return Double.NaN;
        }

        for (int num : data) {
            sum += num;
        }
        mean = sum / (double) (n);

        for (double num : data) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        double variance = standardDeviation / n;

        return Math.sqrt(variance);
    }
}