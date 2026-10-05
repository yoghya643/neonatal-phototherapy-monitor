package com.phototherapy;

public class PIDController {

    private final double kp;
    private final double ki;
    private final double kd;

    private double previousError;
    private double integral;

    private final double minimumOutput;
    private final double maximumOutput;

    public PIDController(
            double kp,
            double ki,
            double kd,
            double minimumOutput,
            double maximumOutput) {

        this.kp = kp;
        this.ki = ki;
        this.kd = kd;

        this.minimumOutput = minimumOutput;
        this.maximumOutput = maximumOutput;

        this.previousError = 0.0;
        this.integral = 0.0;
    }

    public double calculate(
            double targetValue,
            double measuredValue,
            double deltaTime) {

        if (deltaTime <= 0) {
            throw new IllegalArgumentException(
                    "Delta time must be greater than zero."
            );
        }

        // Calculate error
        double error =
                targetValue - measuredValue;

        // Proportional term
        double proportional =
                kp * error;

        // Integral term
        integral =
                integral + error * deltaTime;

        double integralTerm =
                ki * integral;

        // Derivative term
        double derivative =
                (error - previousError)
                        / deltaTime;

        double derivativeTerm =
                kd * derivative;

        // Calculate total PID output
        double output =
                proportional
                        + integralTerm
                        + derivativeTerm;

        // Limit output to allowed range
        if (output > maximumOutput) {
            output = maximumOutput;
        }

        if (output < minimumOutput) {
            output = minimumOutput;
        }

        // Store current error
        // for the next calculation
        previousError = error;

        return output;
    }

    public void reset() {

        previousError = 0.0;
        integral = 0.0;
    }
}