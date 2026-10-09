
package com.phototherapy;

public class PhototherapyController {

    // PID constants used in the paper's example
    private final double kp = 0.9;
    private final double ki = 0.1;
    private final double kd = 0.001;

    private double integral = 0.0;
    private double previousError = 0.0;
    private boolean firstReading = true;

    // Formula: CF = Ipc / Ipe
    public double calculateCorrectionFactor(
            double measuredIllumination,
            double referenceIllumination) {

        if (referenceIllumination <= 0) {
            throw new IllegalArgumentException(
                    "Reference illumination must be greater than zero.");
        }

        return measuredIllumination / referenceIllumination;
    }

    // Formula: e(t) = Isp - Ic(t)
    public double calculateError(
            double setPoint,
            double measuredIllumination) {

        return setPoint - measuredIllumination;
    }

    // Formula:
    // PID = kp * error + ki * integral(error) + kd * derivative(error)
    public double calculatePidOutput(
            double setPoint,
            double measuredIllumination,
            double elapsedSeconds) {

        if (elapsedSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Elapsed time must be greater than zero.");
        }

        double error = calculateError(
                setPoint, measuredIllumination);

        integral += error * elapsedSeconds;

        double derivative = 0.0;

        if (!firstReading) {
            derivative = (error - previousError) / elapsedSeconds;
        }

        double output =
                (kp * error)
                + (ki * integral)
                + (kd * derivative);

        previousError = error;
        firstReading = false;

        return output;
    }

    // Formula: duty cycle = Ton / (Ton + Toff) * 100
    public double calculateDutyCycle(
            double onTime,
            double offTime) {

        if (onTime < 0 || offTime < 0
                || onTime + offTime <= 0) {
            throw new IllegalArgumentException(
                    "On/off times must be non-negative "
                    + "and their total must be greater than zero.");
        }

        return (onTime / (onTime + offTime)) * 100.0;
    }

    // Convert a requested PID output into a simulated PWM percentage.
    // This is a software demonstration, not physical LED control.
    public double calculatePwmPercentage(double pidOutput) {
        return Math.max(0.0, Math.min(100.0, pidOutput));
    }
}