package uk.co.devinity.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.devinity.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {}
