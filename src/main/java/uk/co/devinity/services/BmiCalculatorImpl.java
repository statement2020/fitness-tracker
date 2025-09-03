package uk.co.devinity.services;

import org.springframework.stereotype.Service;

@Service
public class BmiCalculatorImpl implements BmiCalculator {
    @Override
    public double calculateBmi(double weight, double heightCm) {
        if (weight <= 0 || heightCm <= 0) {
            throw new IllegalArgumentException("Weight and height must be positive values.");
        }

        double heightMeters = heightCm / 100.0;

        return weight / (heightMeters * heightMeters);
    }
}
