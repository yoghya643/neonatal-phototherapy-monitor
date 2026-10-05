package com.phototherapy;

import com.phototherapy.model.SensorReading;

public class SensorValidator {

    public boolean isValid(SensorReading reading) {

        if (reading == null) {
            return false;
        }

        // Check timestamp
        if (reading.getTimestamp() < 0) {
            return false;
        }

        // Check temperature
        if (!Double.isFinite(reading.getTemperature())) {
            return false;
        }

        // Software validation range only.
        // These are not clinical safety limits.
        if (reading.getTemperature() < -50 ||
                reading.getTemperature() > 100) {
            return false;
        }

        // Check optical value
        if (!Double.isFinite(reading.getOpticalValue())) {
            return false;
        }

        if (reading.getOpticalValue() < 0) {
            return false;
        }

        // Check PWM
        if (reading.getPwm() < 0 ||
                reading.getPwm() > 100) {
            return false;
        }

        return true;
    }
}