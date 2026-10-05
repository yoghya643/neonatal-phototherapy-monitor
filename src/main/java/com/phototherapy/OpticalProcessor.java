package com.phototherapy;

import com.phototherapy.model.SensorReading;

import java.util.ArrayList;
import java.util.List;

public class OpticalProcessor {

    private final int windowSize;

    public OpticalProcessor(int windowSize) {

        if (windowSize <= 0) {
            throw new IllegalArgumentException(
                    "Window size must be greater than zero."
            );
        }

        this.windowSize = windowSize;
    }

    public List<Double> calculateMovingAverage(
            List<SensorReading> readings) {

        List<Double> filteredValues = new ArrayList<>();

        if (readings == null || readings.isEmpty()) {
            return filteredValues;
        }

        for (int i = 0; i < readings.size(); i++) {

            int startIndex =
                    Math.max(0, i - windowSize + 1);

            double sum = 0;

            int count = 0;

            for (int j = startIndex; j <= i; j++) {

                sum += readings
                        .get(j)
                        .getOpticalValue();

                count++;
            }

            double average = sum / count;

            filteredValues.add(average);
        }

        return filteredValues;
    }
}