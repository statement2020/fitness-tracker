package uk.co.devinity.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;

import java.util.List;

public interface EntryRepository extends JpaRepository<Entry, Long> {
    List<Entry> findByUserOrderByDateAsc(User user);
}
