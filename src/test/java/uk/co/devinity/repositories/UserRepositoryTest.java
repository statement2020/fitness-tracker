package uk.co.devinity.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.co.devinity.entities.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.profiles.active=test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void persistAndLoadUser() {
        User u = new User();
        u.setName("Dave");
        u.setBmr(1650);
        User saved = userRepository.save(u);

        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Dave");
        assertThat(found.getBmr()).isEqualTo(1650);
    }
}
