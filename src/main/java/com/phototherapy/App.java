
package com.phototherapy;

import com.phototherapy.model.SensorReading;

import java.io.IOException;
import java.util.List;

public class App {

    public static void main(String[] args) {

        try {
            // STEP 1: Load simulated sensor readings
            CsvDataSource dataSource = new CsvDataSource();

            List<SensorReading> readings =
                    dataSource.readReadings(
                            "data/simulated_readings.csv");

            if (readings.isEmpty()) {
                System.out.println("No sensor readings found.");
                return;
            }

            // STEP 2: Validate readings
            SensorValidator validator = new SensorValidator();

            for (SensorReading reading : readings) {
                if (!validator.isValid(reading)) {
                    throw new IllegalArgumentException(
                            "Invalid sensor reading at timestamp: "
                                    + reading.getTimestamp());
                }
            }

            // STEP 3: Process optical readings using moving average
            OpticalProcessor opticalProcessor =
                    new OpticalProcessor(3);

            List<Double> processedOpticalValues =
                    opticalProcessor.calculateMovingAverage(readings);

            System.out.println("\n--------- Processed Optical Values ---------");

            for (int i = 0; i < readings.size(); i++) {
                System.out.printf(
                        "Time %d : Raw = %.2f, Processed = %.2f%n",
                        readings.get(i).getTimestamp(),
                        readings.get(i).getOpticalValue(),
                        processedOpticalValues.get(i));
            }

            // STEP 4: Generate optical readings graph
            double[] rawOpticalValues = new double[readings.size()];
            double[] processedValues =
                    new double[processedOpticalValues.size()];

            for (int i = 0; i < readings.size(); i++) {
                rawOpticalValues[i] =
                        readings.get(i).getOpticalValue();

                processedValues[i] =
                        processedOpticalValues.get(i);
            }

            SensorGraph.createOpticalGraph(
                    rawOpticalValues, processedValues);

            // STEP 5: Detect anomalies
            AnomalyDetector anomalyDetector =
                    new AnomalyDetector(3, 2.0);

            List<Integer> anomalyIndexes =
                    anomalyDetector.detectOpticalAnomalies(readings);

            System.out.println("\n--------- Anomaly Detection ---------");

            if (anomalyIndexes.isEmpty()) {
                System.out.println("No optical anomalies detected.");
            } else {
                for (int index : anomalyIndexes) {
                    SensorReading reading = readings.get(index);

                    System.out.printf(
                            "Anomaly at time %d : Optical = %.2f%n",
                            reading.getTimestamp(),
                            reading.getOpticalValue());
                }
            }

            // STEP 6: Configure the demonstration controller
            SensorDatabase.initializeDatabase();

            PhototherapyController controller =
                    new PhototherapyController();

            // Demonstration values only.
            // Replace these with validated measurements and
            // values specified by your research paper.
            double targetValue = 100.0;
            double referenceIllumination = 100.0;
            double elapsedSeconds = 1.0;

            System.out.println("\n--------- Formula Calculations ---------");

            // STEP 7: Calculate formulas for each reading
            for (int i = 0; i < readings.size(); i++) {

                SensorReading reading = readings.get(i);

                double rawValue = reading.getOpticalValue();
                double processedValue =
                        processedOpticalValues.get(i);

                boolean isAnomaly = anomalyIndexes.contains(i);

                // CF = Ipc / Ipe
                // Here, processedValue is used as Ipc and the
                // reference value as Ipe for demonstration.
                double correctionFactor =
                        controller.calculateCorrectionFactor(
                                processedValue,
                                referenceIllumination);

                // e(t) = Isp - Ic(t)
                double error =
                        controller.calculateError(
                                targetValue, processedValue);

                // PID output using elapsed time
                double pidOutput =
                        controller.calculatePidOutput(
                                targetValue,
                                processedValue,
                                elapsedSeconds);

                // Map simulated PID output to 0-100%
                double pwmPercentage =
                        controller.calculatePwmPercentage(pidOutput);

                // Demonstration duty-cycle calculation.
                // Use a 100-unit period; PWM percentage determines
                // ON time and the remaining time is OFF.
                double onTime = pwmPercentage;
                double offTime = 100.0 - pwmPercentage;

                double dutyCycle =
                        controller.calculateDutyCycle(
                                onTime, offTime);

                System.out.printf(
                        "Time: %d | CF: %.3f | Error: %.2f"
                                + " | PID: %.2f | PWM: %.2f%%"
                                + " | Duty Cycle: %.2f%%%n",
                        reading.getTimestamp(),
                        correctionFactor,
                        error,
                        pidOutput,
                        pwmPercentage,
                        dutyCycle);

                // Save the simulated PWM percentage as controller output.
                // The existing database schema is unchanged.
                SensorDatabase.saveReading(
                        rawValue,
                        processedValue,
                        isAnomaly,
                        pwmPercentage);
            }

            System.out.println(
                    "\nSensor readings and simulated controller outputs "
                            + "saved to SQLite.");

            // STEP 8: Calculate performance statistics
            PerformanceAnalyzer analyzer =
                    new PerformanceAnalyzer();

            double mean = analyzer.calculateMean(readings);

            double standardDeviation =
                    analyzer.calculateStandardDeviation(readings);

            double minimum = analyzer.findMinimum(readings);

            double maximum = analyzer.findMaximum(readings);

            double meanAbsoluteError =
                    analyzer.calculateMeanAbsoluteError(
                            readings, targetValue);

            double steadyStateError =
                    analyzer.calculateSteadyStateError(
                            readings, targetValue);

            double overshoot =
                    analyzer.calculateOvershoot(
                            readings, targetValue);

            double settlingTime =
                    analyzer.calculateSettlingTime(
                            readings, targetValue, 2.0);

            // STEP 9: Display performance report
            System.out.println("\n========================================");
            System.out.println("   PHOTOTHERAPY PERFORMANCE ANALYSIS");
            System.out.println("========================================");

            System.out.printf(
                    "Number of Readings     : %d%n", readings.size());

            System.out.printf(
                    "Target Optical Output  : %.2f%n", targetValue);

            System.out.println("\n--------- Optical Performance ---------");

            System.out.printf("Mean Output            : %.2f%n", mean);
            System.out.printf(
                    "Standard Deviation     : %.2f%n", standardDeviation);
            System.out.printf("Minimum Output         : %.2f%n", minimum);
            System.out.printf("Maximum Output         : %.2f%n", maximum);

            System.out.println("\n--------- Control Performance ---------");

            System.out.printf(
                    "Mean Absolute Error    : %.2f%n", meanAbsoluteError);
            System.out.printf(
                    "Steady-State Error     : %.2f%n", steadyStateError);
            System.out.printf("Overshoot              : %.2f%%%n", overshoot);

            if (settlingTime == -1) {
                System.out.println("Settling Time          : Not Settled");
            } else {
                System.out.printf(
                        "Settling Time          : %.2f seconds%n",
                        settlingTime);
            }

            System.out.println("========================================");

        } catch (IOException e) {
            System.out.println(
                    "Error loading sensor data or creating graph:");
            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input or formula parameters:");
            System.out.println(e.getMessage());
        }
    }
}