package com.phototherapy;

import com.phototherapy.model.SensorReading;

import java.io.IOException;
import java.util.List;

public class App {

    public static void main(String[] args) {

        try {

            // -----------------------------------------
            // STEP 1: Load sensor data from CSV
            // -----------------------------------------

            CsvDataSource dataSource = new CsvDataSource();

            List<SensorReading> readings =
                    dataSource.readReadings(
                            "data/simulated_readings.csv"
                    );

            // -----------------------------------------
            // STEP 2: Validate sensor readings
            // -----------------------------------------

            SensorValidator validator = new SensorValidator();

            for (SensorReading reading : readings) {

                if (!validator.isValid(reading)) {
                    throw new IllegalArgumentException(
                            "Invalid sensor reading at timestamp: "
                                    + reading.getTimestamp()
                    );
                }
            }

            // -----------------------------------------
            // STEP 3: Process optical readings
            // -----------------------------------------

            OpticalProcessor opticalProcessor =
                    new OpticalProcessor(3);

            List<Double> processedOpticalValues =
                    opticalProcessor.calculateMovingAverage(readings);

            System.out.println();
            System.out.println(
                    "--------- Processed Optical Values ---------"
            );

            for (int i = 0;
                 i < processedOpticalValues.size();
                 i++) {

                System.out.printf(
                        "Time %d : Raw = %.2f, Processed = %.2f%n",
                        readings.get(i).getTimestamp(),
                        readings.get(i).getOpticalValue(),
                        processedOpticalValues.get(i)
                );
            }

            // -----------------------------------------
            // STEP 3B: Generate optical readings graph
            // -----------------------------------------

            double[] rawOpticalValues =
                    new double[readings.size()];

            double[] processedValues =
                    new double[processedOpticalValues.size()];

            for (int i = 0; i < readings.size(); i++) {
                rawOpticalValues[i] =
                        readings.get(i).getOpticalValue();
            }

            for (int i = 0;
                 i < processedOpticalValues.size();
                 i++) {

                processedValues[i] =
                        processedOpticalValues.get(i);
            }

            SensorGraph.createOpticalGraph(
                    rawOpticalValues,
                    processedValues
            );

            // -----------------------------------------
            // STEP 4: Detect optical anomalies
            // -----------------------------------------

            AnomalyDetector anomalyDetector =
                    new AnomalyDetector(3, 2.0);

            List<Integer> anomalyIndexes =
                    anomalyDetector.detectOpticalAnomalies(readings);

            System.out.println();
            System.out.println(
                    "--------- Anomaly Detection ---------"
            );

            if (anomalyIndexes.isEmpty()) {

                System.out.println(
                        "No optical anomalies detected."
                );

            } else {

                for (int index : anomalyIndexes) {

                    SensorReading reading = readings.get(index);

                    System.out.printf(
                            "Anomaly detected at time %d : Optical = %.2f%n",
                            reading.getTimestamp(),
                            reading.getOpticalValue()
                    );
                }
            }

            // -----------------------------------------
            // STEP 4B: Initialize and save to SQLite
            // -----------------------------------------

            SensorDatabase.initializeDatabase();

            System.out.println();
            System.out.println(
                    "--------- Saving Sensor Readings ---------"
            );

            for (int i = 0; i < readings.size(); i++) {

                SensorReading reading = readings.get(i);

                double rawValue =
                        reading.getOpticalValue();

                double processedValue =
                        processedOpticalValues.get(i);

                boolean isAnomaly =
                        anomalyIndexes.contains(i);

                // Placeholder until a real PID controller
                // output is connected to the application.
                double controllerOutput = 0.0;

                SensorDatabase.saveReading(
                        rawValue,
                        processedValue,
                        isAnomaly,
                        controllerOutput
                );
            }

            System.out.println(
                    "Sensor readings saved to SQLite database."
            );

            // -----------------------------------------
            // STEP 5: Set target optical value
            // -----------------------------------------

            double targetValue = 100.0;

            // -----------------------------------------
            // STEP 6: Create Performance Analyzer
            // -----------------------------------------

            PerformanceAnalyzer analyzer =
                    new PerformanceAnalyzer();

            // -----------------------------------------
            // STEP 7: Calculate performance metrics
            // -----------------------------------------

            double mean =
                    analyzer.calculateMean(readings);

            double standardDeviation =
                    analyzer.calculateStandardDeviation(readings);

            double minimum =
                    analyzer.findMinimum(readings);

            double maximum =
                    analyzer.findMaximum(readings);

            double meanAbsoluteError =
                    analyzer.calculateMeanAbsoluteError(
                            readings,
                            targetValue
                    );

            double steadyStateError =
                    analyzer.calculateSteadyStateError(
                            readings,
                            targetValue
                    );

            double overshoot =
                    analyzer.calculateOvershoot(
                            readings,
                            targetValue
                    );

            double settlingTime =
                    analyzer.calculateSettlingTime(
                            readings,
                            targetValue,
                            2.0
                    );

            // -----------------------------------------
            // STEP 8: Display Performance Report
            // -----------------------------------------

            System.out.println();
            System.out.println(
                    "========================================"
            );
            System.out.println(
                    "   PHOTOTHERAPY PERFORMANCE ANALYSIS"
            );
            System.out.println(
                    "========================================"
            );

            System.out.println();

            System.out.printf(
                    "Number of Readings     : %d%n",
                    readings.size()
            );

            System.out.printf(
                    "Target Optical Output  : %.2f%n",
                    targetValue
            );

            System.out.println();
            System.out.println(
                    "--------- Optical Performance ---------"
            );

            System.out.printf(
                    "Mean Output            : %.2f%n",
                    mean
            );

            System.out.printf(
                    "Standard Deviation     : %.2f%n",
                    standardDeviation
            );

            System.out.printf(
                    "Minimum Output         : %.2f%n",
                    minimum
            );

            System.out.printf(
                    "Maximum Output         : %.2f%n",
                    maximum
            );

            System.out.println();
            System.out.println(
                    "--------- Control Performance ---------"
            );

            System.out.printf(
                    "Mean Absolute Error    : %.2f%n",
                    meanAbsoluteError
            );

            System.out.printf(
                    "Steady-State Error     : %.2f%n",
                    steadyStateError
            );

            System.out.printf(
                    "Overshoot              : %.2f%%%n",
                    overshoot
            );

            if (settlingTime == -1) {

                System.out.println(
                        "Settling Time          : Not Settled"
                );

            } else {

                System.out.printf(
                        "Settling Time          : %.2f seconds%n",
                        settlingTime
                );
            }

            System.out.println();
            System.out.println(
                    "========================================"
            );

        } catch (IOException e) {

            System.out.println(
                    "Error while loading sensor data or creating graph:"
            );

            System.out.println(e.getMessage());

        } catch (IllegalArgumentException e) {

            System.out.println("Invalid sensor data:");
            System.out.println(e.getMessage());
        }
    }
}
