package uk.co.devinity.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BmiCalculatorImplTest {

    private BmiCalculator underTest = new BmiCalculatorImpl();

    @Test
    public void whenGivenNormalWeightAndHeight_thenBmiIsCalculatedCorrectly() {
        double weightKg = 70.0;
        double heightCm = 175.0;

        double expectedBmi = 22.86;
        double actualBmi = underTest.calculateBmi(weightKg, heightCm);

        assertEquals(expectedBmi, actualBmi, 0.01);
    }

    @Test
    public void whenGivenUnderweight_thenBmiIsCalculatedCorrectly() {
        double weightKg = 50.0;
        double heightCm = 170.0;
        double expectedBmi = 17.30;
        double actualBmi = underTest.calculateBmi(weightKg, heightCm);
        assertEquals(expectedBmi, actualBmi, 0.01);
    }

    @Test
    public void whenGivenOverweight_thenBmiIsCalculatedCorrectly() {
        double weightKg = 85.0;
        double heightCm = 175.0;
        double expectedBmi = 27.76;
        double actualBmi = underTest.calculateBmi(weightKg, heightCm);
        assertEquals(expectedBmi, actualBmi, 0.01);
    }

    @Test
    public void whenGivenObese_thenBmiIsCalculatedCorrectly() {
        double weightKg = 100.0;
        double heightCm = 170.0;
        double expectedBmi = 34.60;
        double actualBmi = underTest.calculateBmi(weightKg, heightCm);
        assertEquals(expectedBmi, actualBmi, 0.01);
    }

    @Test
    public void whenGivenZeroWeight_thenExceptionIsThrown() {
        double weightKg = 0.0;
        double heightCm = 170.0;
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.calculateBmi(weightKg, heightCm);
        });
    }

    @Test
    public void whenGivenNegativeHeight_thenExceptionIsThrown() {
        double weightKg = 70.0;
        double heightCm = -170.0;
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.calculateBmi(weightKg, heightCm);
        });
    }
}
