
package com.phototherapy.model;

public class SensorReading {

    private final int timestamp;
    private final double temperature;
    private final double opticalValue;
    private final int pwm;

    public SensorReading(int timestamp, double temperature,
                         double opticalValue, int pwm) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.opticalValue = opticalValue;
        this.pwm = pwm;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getOpticalValue() {
        return opticalValue;
    }

    public int getPwm() {
        return pwm;
    }
}