package com.phototherapy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SensorDatabase {

    private static final String DB_URL =
            "jdbc:sqlite:phototherapy.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String sql = """
                CREATE TABLE IF NOT EXISTS sensor_readings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL,
                    raw_optical_value REAL,
                    processed_optical_value REAL,
                    anomaly INTEGER NOT NULL DEFAULT 0,
                    controller_output REAL
                )
                """;

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);

            System.out.println(
                    "Sensor database initialized successfully."
            );

        } catch (SQLException e) {
            System.err.println(
                    "Failed to initialize sensor database."
            );
            e.printStackTrace();
        }
    }

    public static void saveReading(
            double rawOpticalValue,
            double processedOpticalValue,
            boolean anomaly,
            double controllerOutput) {

        String sql = """
                INSERT INTO sensor_readings (
                    timestamp,
                    raw_optical_value,
                    processed_optical_value,
                    anomaly,
                    controller_output
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, Instant.now().toString());
            statement.setDouble(2, rawOpticalValue);
            statement.setDouble(3, processedOpticalValue);
            statement.setInt(4, anomaly ? 1 : 0);
            statement.setDouble(5, controllerOutput);

            statement.executeUpdate();

            System.out.println(
                    "Sensor reading saved successfully."
            );

        } catch (SQLException e) {
            System.err.println(
                    "Failed to save sensor reading."
            );
            e.printStackTrace();
        }
    }

    public static List<DatabaseReading> getAllReadings() {
        List<DatabaseReading> readings = new ArrayList<>();

        String sql = """
                SELECT id, timestamp, raw_optical_value,
                       processed_optical_value, anomaly,
                       controller_output
                FROM sensor_readings
                ORDER BY id ASC
                """;

        try (Connection connection = connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                readings.add(new DatabaseReading(
                        resultSet.getInt("id"),
                        resultSet.getString("timestamp"),
                        resultSet.getDouble("raw_optical_value"),
                        resultSet.getDouble("processed_optical_value"),
                        resultSet.getInt("anomaly") == 1,
                        resultSet.getDouble("controller_output")
                ));
            }

        } catch (SQLException e) {
            System.err.println(
                    "Failed to read sensor readings from database."
            );
            e.printStackTrace();
        }

        return readings;
    }

    public static class DatabaseReading {

        private final int id;
        private final String timestamp;
        private final double rawOpticalValue;
        private final double processedOpticalValue;
        private final boolean anomaly;
        private final double controllerOutput;

        public DatabaseReading(
                int id,
                String timestamp,
                double rawOpticalValue,
                double processedOpticalValue,
                boolean anomaly,
                double controllerOutput) {

            this.id = id;
            this.timestamp = timestamp;
            this.rawOpticalValue = rawOpticalValue;
            this.processedOpticalValue = processedOpticalValue;
            this.anomaly = anomaly;
            this.controllerOutput = controllerOutput;
        }

        public int getId() {
            return id;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public double getRawOpticalValue() {
            return rawOpticalValue;
        }

        public double getProcessedOpticalValue() {
            return processedOpticalValue;
        }

        public boolean isAnomaly() {
            return anomaly;
        }

        public double getControllerOutput() {
            return controllerOutput;
        }
    }
}

