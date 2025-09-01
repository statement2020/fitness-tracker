package uk.co.devinity.services;

import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;

public interface BmrService {

    public double calculateBmrForEntry(Entry entry);
}
