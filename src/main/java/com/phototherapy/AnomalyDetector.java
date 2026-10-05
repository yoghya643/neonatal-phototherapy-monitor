package com.phototherapy;

import com.phototherapy.model.SensorReading;

import java.util.ArrayList;
import java.util.List;

public class AnomalyDetector {

    private final int windowSize;
    private final double zScoreThreshold;

    public AnomalyDetector(int windowSize, double zScoreThreshold) {

        if (windowSize <= 0) {
            throw new IllegalArgumentException(
                    "Window size must be greater than zero."
            );
        }

        if (zScoreThreshold <= 0) {
            throw new IllegalArgumentException(
                    "Z-score threshold must be greater than zero."
            );
        }

        this.windowSize = windowSize;
        this.zScoreThreshold = zScoreThreshold;
    }

    public List<Integer> detectOpticalAnomalies(
            List<SensorReading> readings) {

        List<Integer> anomalyIndexes =
                new ArrayList<>();

        if (readings == null ||
                readings.size() < windowSize + 1) {

            return anomalyIndexes;
        }

        for (int i = windowSize;
             i < readings.size();
             i++) {

            double sum = 0;

            // Calculate previous-window mean
            for (int j = i - windowSize;
                 j < i;
                 j++) {

                sum += readings
                        .get(j)
                        .getOpticalValue();
            }

            double mean = sum / windowSize;

            // Calculate previous-window standard deviation
            double squaredDifferenceSum = 0;

            for (int j = i - windowSize;
                 j < i;
                 j++) {

                double difference =
                        readings.get(j).getOpticalValue()
                                - mean;

                squaredDifferenceSum +=
                        difference * difference;
            }

            double standardDeviation =
                    Math.sqrt(
                            squaredDifferenceSum
                                    / windowSize
                    );

            double currentValue =
                    readings.get(i).getOpticalValue();

            // If previous values are identical,
            // any different value is treated as an anomaly.
            if (standardDeviation == 0) {

                if (currentValue != mean) {
                    anomalyIndexes.add(i);
                }

                continue;
            }

            double zScore =
                    (currentValue - mean)
                            / standardDeviation;

            if (Math.abs(zScore) > zScoreThreshold) {
                anomalyIndexes.add(i);
            }
        }

        return anomalyIndexes;
    }
}