
package com.phototherapy;

import com.phototherapy.model.SensorReading;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvDataSource {

    public List<SensorReading> readReadings(String filePath)
            throws IOException {

        List<SensorReading> readings = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(Path.of(filePath))) {

            String header = reader.readLine();

            if (header == null ||
                    !header.trim().equalsIgnoreCase(
                            "timestamp,temperature,optical_value,pwm")) {
                throw new IOException("Invalid or missing CSV header.");
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",", -1);

                if (values.length != 4) {
                    throw new IOException(
                            "Expected 4 columns at line " + lineNumber);
                }

                try {
                    int timestamp = Integer.parseInt(values[0].trim());
                    double temperature =
                            Double.parseDouble(values[1].trim());
                    double opticalValue =
                            Double.parseDouble(values[2].trim());
                    int pwm = Integer.parseInt(values[3].trim());

                    if (timestamp < 0 ||
                            !Double.isFinite(temperature) ||
                            !Double.isFinite(opticalValue) ||
                            temperature < -50 || temperature > 100 ||
                            opticalValue < 0 ||
                            pwm < 0 || pwm > 100) {
                        throw new IllegalArgumentException(
                                "Value outside software validation bounds");
                    }

                    readings.add(new SensorReading(
                            timestamp, temperature, opticalValue, pwm));

                
                } catch (IllegalArgumentException e) {
                    throw new IOException(
                            "Invalid data at CSV line " + lineNumber
                                    + ": " + line, e);
                }
            }
        }

        return readings;
    }
}