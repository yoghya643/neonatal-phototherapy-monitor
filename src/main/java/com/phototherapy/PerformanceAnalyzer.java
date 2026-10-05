package com.phototherapy;

import com.phototherapy.model.SensorReading;

import java.util.List;

public class PerformanceAnalyzer {

    // ---------------------------------------------------------
    // 1. Calculate Mean Optical Output
    // ---------------------------------------------------------
    public double calculateMean(List<SensorReading> readings) {

        if (readings.isEmpty()) {
            return 0;
        }

        double sum = 0;

        for (SensorReading reading : readings) {
            sum += reading.getOpticalValue();
        }

        return sum / readings.size();
    }


    // ---------------------------------------------------------
    // 2. Calculate Standard Deviation
    // ---------------------------------------------------------
    public double calculateStandardDeviation(
            List<SensorReading> readings) {

        if (readings.isEmpty()) {
            return 0;
        }

        double mean = calculateMean(readings);

        double sumSquaredDifference = 0;

        for (SensorReading reading : readings) {

            double difference =
                    reading.getOpticalValue() - mean;

            sumSquaredDifference +=
                    difference * difference;
        }

        return Math.sqrt(
                sumSquaredDifference / readings.size()
        );
    }


    // ---------------------------------------------------------
    // 3. Find Minimum Optical Output
    // ---------------------------------------------------------
    public double findMinimum(
            List<SensorReading> readings) {

        if (readings.isEmpty()) {
            return 0;
        }

        double minimum =
                readings.get(0).getOpticalValue();

        for (SensorReading reading : readings) {

            if (reading.getOpticalValue() < minimum) {
                minimum = reading.getOpticalValue();
            }
        }

        return minimum;
    }


    // ---------------------------------------------------------
    // 4. Find Maximum Optical Output
    // ---------------------------------------------------------
    public double findMaximum(
            List<SensorReading> readings) {

        if (readings.isEmpty()) {
            return 0;
        }

        double maximum =
                readings.get(0).getOpticalValue();

        for (SensorReading reading : readings) {

            if (reading.getOpticalValue() > maximum) {
                maximum = reading.getOpticalValue();
            }
        }

        return maximum;
    }


    // ---------------------------------------------------------
    // 5. Calculate Mean Absolute Error
    // ---------------------------------------------------------
    public double calculateMeanAbsoluteError(
            List<SensorReading> readings,
            double targetValue) {

        if (readings.isEmpty()) {
            return 0;
        }

        double totalError = 0;

        for (SensorReading reading : readings) {

            double error =
                    Math.abs(
                            targetValue
                                    - reading.getOpticalValue()
                    );

            totalError += error;
        }

        return totalError / readings.size();
    }


    // ---------------------------------------------------------
    // 6. Calculate Steady-State Error
    // ---------------------------------------------------------
    public double calculateSteadyStateError(
            List<SensorReading> readings,
            double targetValue) {

        if (readings.isEmpty()) {
            return 0;
        }

        // Use the last 3 readings for the
        // steady-state calculation.
        int numberOfFinalReadings =
                Math.min(3, readings.size());

        double sum = 0;

        for (int i = readings.size() - numberOfFinalReadings;
             i < readings.size();
             i++) {

            sum += readings.get(i).getOpticalValue();
        }

        double finalAverage =
                sum / numberOfFinalReadings;

        return Math.abs(
                targetValue - finalAverage
        );
    }


    // ---------------------------------------------------------
    // 7. Calculate Overshoot Percentage
    // ---------------------------------------------------------
    public double calculateOvershoot(
            List<SensorReading> readings,
            double targetValue) {

        if (readings.isEmpty() || targetValue == 0) {
            return 0;
        }

        double maximum =
                findMaximum(readings);

        if (maximum <= targetValue) {
            return 0;
        }

        return ((maximum - targetValue)
                / targetValue) * 100;
    }


    // ---------------------------------------------------------
    // 8. Calculate Settling Time
    // ---------------------------------------------------------
    public double calculateSettlingTime(
            List<SensorReading> readings,
            double targetValue,
            double tolerancePercentage) {

        if (readings.isEmpty()) {
            return 0;
        }

        // Example:
        // target = 100
        // tolerance = 2%
        //
        // Acceptable range:
        // 98 to 102

        double tolerance =
                targetValue
                        * tolerancePercentage
                        / 100.0;

        double lowerLimit =
                targetValue - tolerance;

        double upperLimit =
                targetValue + tolerance;

        for (int i = 0; i < readings.size(); i++) {

            boolean allRemainingReadingsWithinRange = true;

            for (int j = i; j < readings.size(); j++) {

                double value =
                        readings.get(j).getOpticalValue();

                if (value < lowerLimit
                        || value > upperLimit) {

                    allRemainingReadingsWithinRange =
                            false;

                    break;
                }
            }

            if (allRemainingReadingsWithinRange) {

                return readings.get(i).getTimestamp();
            }
        }

        // If the system never settles
        return -1;
    }
}