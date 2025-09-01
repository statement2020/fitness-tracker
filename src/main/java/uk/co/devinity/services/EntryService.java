package uk.co.devinity.services;

import uk.co.devinity.entities.User;

import java.util.List;
import java.util.Map;

public interface EntryService {

    List<Map<String, Object>> getEntriesForUser(User user);
}
