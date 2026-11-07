package com.csa09.events.dao;

import com.csa09.events.model.Registration;
import java.time.LocalDateTime;
import java.util.*;

/** Demo / test DAO with the same rules as MySQL (unique student+event). Used when no database is configured. */
public class InMemoryRegistrationDao implements RegistrationDao {
    private final List<String> events = List.of("Hackathon 2026", "Dance Fiesta", "Quiz Bowl", "Photography Walk");
    private final List<Registration> rows = new ArrayList<>();
    private final Set<String> keys = new HashSet<>();
    private long nextId = 1;

    public InMemoryRegistrationDao(int seedRows) {
        String[] first = {"Aarav", "Diya", "Rohan", "Isha", "Kabir", "Meera", "Vihaan", "Anaya", "Arjun", "Sara"};
        String[] last = {"Kumar", "Sharma", "Mehta", "Nair", "Reddy", "Iyer", "Khan", "Das"};
        for (int i = 0; i < seedRows; i++) {
            String reg = String.valueOf(192210000 + i);
            String nm = first[i % first.length] + " " + last[(i / 3) % last.length];
            try { add(reg, nm, (nm.replace(' ', '.') + "@example.com").toLowerCase(), events.get(i % events.size())); } catch (DaoException ignored) { }
        }
    }

    @Override public synchronized List<String> eventNames() { return events; }

    @Override public synchronized void add(String reg, String name, String email, String event) throws DaoException {
        if (!events.contains(event)) throw new DaoException("Unknown event: " + event);
        if (!keys.add(reg + "|" + event)) throw new DuplicateRegistrationException("Student " + reg + " is already registered for " + event + ".");
        rows.add(new Registration(nextId++, reg, name, email, event, LocalDateTime.now()));
    }
    private List<Registration> filtered(String event) {
        if (event == null) return rows;
        List<Registration> l = new ArrayList<>();
        for (Registration r : rows) if (r.eventName().equals(event)) l.add(r);
        return l;
    }
    @Override public synchronized int count(String event) { return filtered(event).size(); }
    @Override public synchronized List<Registration> page(String event, int offset, int limit) {
        List<Registration> f = filtered(event);
        return new ArrayList<>(f.subList(Math.min(offset, f.size()), Math.min(offset + limit, f.size())));
    }
    @Override public synchronized List<Registration> all(String event) { return new ArrayList<>(filtered(event)); }
}
