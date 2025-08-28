package uk.co.devinity.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.profiles.active=test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class EntryRepositoryTest {

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void persistAndQueryEntriesByUserOrderedByDate() {
        User u = new User();
        u.setName("Erin");
        u.setBmr(1600);
        userRepository.save(u);

        Entry e1 = new Entry();
        e1.setUser(u);
        e1.setDate(LocalDate.of(2025, 8, 26));
        e1.setCaloriesConsumed(2000);
        e1.setCaloriesBurnt(400);
        e1.setWeight(70.0);

        Entry e0 = new Entry();
        e0.setUser(u);
        e0.setDate(LocalDate.of(2025, 8, 25));
        e0.setCaloriesConsumed(1900);
        e0.setCaloriesBurnt(350);
        e0.setWeight(70.2);

        entryRepository.save(e1);
        entryRepository.save(e0);

        List<Entry> results = entryRepository.findByUserOrderByDateAsc(u);
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getDate()).isEqualTo(LocalDate.of(2025, 8, 25));
        assertThat(results.get(1).getDate()).isEqualTo(LocalDate.of(2025, 8, 26));
    }
}
