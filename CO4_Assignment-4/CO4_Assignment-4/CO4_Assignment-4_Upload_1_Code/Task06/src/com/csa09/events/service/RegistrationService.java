package com.csa09.events.service;

import com.csa09.events.dao.*;
import com.csa09.events.model.Registration;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Business layer: validation, registration, paging, export. Contains NO Swing code. */
public class RegistrationService {
    public static final int PAGE_SIZE = 20;
    private static final Pattern REG = Pattern.compile("[A-Za-z0-9]{6,15}");
    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z .'-]{1,59}");
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+");
    private final RegistrationDao dao;

    public RegistrationService(RegistrationDao dao) { this.dao = dao; }

    /** Fast, pure check - safe to call on the event-dispatch thread. */
    public List<String> validate(String reg, String name, String email, String event) {
        List<String> p = new ArrayList<>();
        if (reg == null || !REG.matcher(reg.trim()).matches()) p.add("Register number must be 6-15 letters/digits.");
        if (name == null || !NAME.matcher(name.trim()).matches()) p.add("Name must be 2-60 letters (spaces . ' - allowed).");
        if (email == null || !EMAIL.matcher(email.trim()).matches()) p.add("Email address is not valid.");
        if (event == null || event.isBlank()) p.add("Please choose an event.");
        return p;
    }

    /** Slow (database). Call from a worker thread. */
    public void register(String reg, String name, String email, String event) throws ValidationException, DaoException {
        List<String> p = validate(reg, name, email, event);
        if (!p.isEmpty()) throw new ValidationException(p);
        dao.add(reg.trim().toUpperCase(), name.trim(), email.trim().toLowerCase(), event);   // UNIQUE constraint rejects duplicates
    }

    public List<String> events() throws DaoException { return dao.eventNames(); }
    public int total(String event) throws DaoException { return dao.count(event); }
    public List<Registration> page(String event, int pageIndex) throws DaoException { return dao.page(event, pageIndex * PAGE_SIZE, PAGE_SIZE); }

    public int exportCsv(Path file, String event) throws DaoException, IOException {
        List<Registration> all = dao.all(event);
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8))) {
            w.println("id,register_no,name,email,event,registered_at");
            for (Registration r : all)
                w.println(r.id() + "," + q(r.studentReg()) + "," + q(r.studentName()) + "," + q(r.email()) + "," + q(r.eventName()) + "," + r.registeredAt());
        }
        return all.size();
    }
    private static String q(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }
}
