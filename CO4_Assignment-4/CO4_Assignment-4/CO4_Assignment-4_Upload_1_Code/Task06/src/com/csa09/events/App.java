package com.csa09.events;

import com.csa09.events.dao.*;
import com.csa09.events.service.RegistrationService;
import com.csa09.events.ui.MainFrame;
import javax.swing.SwingUtilities;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/** Entry point. Uses MySQL when db.properties works; otherwise starts in clearly labelled DEMO mode. */
public class App {
    public static void main(String[] args) {
        RegistrationDao dao; String mode;
        try {
            Properties p = new Properties();
            try (FileInputStream in = new FileInputStream("db.properties")) { p.load(in); }
            JdbcRegistrationDao jdbc = new JdbcRegistrationDao(p.getProperty("url"), p.getProperty("user"), p.getProperty("password"));
            jdbc.eventNames();                                   // connection test
            dao = jdbc; mode = "  [MySQL]";
        } catch (IOException | DaoException | RuntimeException ex) {
            System.out.println("No usable MySQL connection (" + ex.getMessage() + ") - starting DEMO mode with in-memory data.");
            dao = new InMemoryRegistrationDao(63); mode = "  [DEMO - in-memory]";
        }
        final RegistrationService svc = new RegistrationService(dao); final String m = mode;
        SwingUtilities.invokeLater(() -> new MainFrame(svc, m).setVisible(true));
    }
}
