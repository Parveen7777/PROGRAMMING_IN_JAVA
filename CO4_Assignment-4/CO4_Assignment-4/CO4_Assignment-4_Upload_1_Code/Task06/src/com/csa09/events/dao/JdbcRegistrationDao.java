package com.csa09.events.dao;

import com.csa09.events.model.Registration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** MySQL implementation. PreparedStatement everywhere; every resource closed by try-with-resources. */
public class JdbcRegistrationDao implements RegistrationDao {
    private static final String SELECT =
        "SELECT r.id, r.student_reg, r.student_name, r.email, e.name, r.registered_at " +
        "FROM registrations r JOIN events e ON e.id = r.event_id ";
    private final String url, user, password;

    public JdbcRegistrationDao(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }
    private Connection open() throws SQLException { return DriverManager.getConnection(url, user, password); }

    @Override public List<String> eventNames() throws DaoException {
        try (Connection c = open(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT name FROM events ORDER BY event_date")) {
            List<String> names = new ArrayList<>();
            while (rs.next()) names.add(rs.getString(1));
            return names;
        } catch (SQLException ex) { throw new DaoException("Cannot load events: " + ex.getMessage(), ex); }
    }

    @Override public void add(String reg, String name, String email, String event) throws DaoException {
        String sql = "INSERT INTO registrations(student_reg, student_name, email, event_id) SELECT ?, ?, ?, id FROM events WHERE name = ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reg); ps.setString(2, name); ps.setString(3, email); ps.setString(4, event);
            if (ps.executeUpdate() == 0) throw new DaoException("Unknown event: " + event);
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new DuplicateRegistrationException("Student " + reg + " is already registered for " + event + ".");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062 || "23000".equals(ex.getSQLState()))
                throw new DuplicateRegistrationException("Student " + reg + " is already registered for " + event + ".");
            throw new DaoException("Database error while saving: " + ex.getMessage(), ex);
        }
    }

    @Override public int count(String event) throws DaoException {
        String sql = "SELECT COUNT(*) FROM registrations r JOIN events e ON e.id = r.event_id" + (event == null ? "" : " WHERE e.name = ?");
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (event != null) ps.setString(1, event);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException ex) { throw new DaoException("Cannot count registrations: " + ex.getMessage(), ex); }
    }

    @Override public List<Registration> page(String event, int offset, int limit) throws DaoException {
        String sql = SELECT + (event == null ? "" : "WHERE e.name = ? ") + "ORDER BY r.id LIMIT ? OFFSET ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (event != null) ps.setString(i++, event);
            ps.setInt(i++, limit); ps.setInt(i, offset);
            return read(ps);
        } catch (SQLException ex) { throw new DaoException("Cannot load page: " + ex.getMessage(), ex); }
    }

    @Override public List<Registration> all(String event) throws DaoException {
        String sql = SELECT + (event == null ? "" : "WHERE e.name = ? ") + "ORDER BY r.id";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (event != null) ps.setString(1, event);
            return read(ps);
        } catch (SQLException ex) { throw new DaoException("Cannot export: " + ex.getMessage(), ex); }
    }

    private List<Registration> read(PreparedStatement ps) throws SQLException {
        List<Registration> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Registration(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getTimestamp(6).toLocalDateTime()));
        }
        return list;
    }
}
