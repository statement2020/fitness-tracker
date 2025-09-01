package uk.co.devinity.services;

import org.springframework.stereotype.Service;
import uk.co.devinity.entities.Entry;

import static uk.co.devinity.entities.Sex.MALE;

@Service
public class BmrServiceImpl implements BmrService {

    @Override
    public double calculateBmrForEntry(Entry entry) {
        int age = entry.getUser().getAge();
        double height = entry.getUser().getHeight();
        double weight = entry.getWeight();
        double bmr;
        if (entry.getUser().getSex() == MALE) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }

        if (entry.getUser().getActivityLevel() != null) {
            bmr *= entry.getUser().getActivityLevel().getMultiplier();
        }

        return bmr;
    }
}
