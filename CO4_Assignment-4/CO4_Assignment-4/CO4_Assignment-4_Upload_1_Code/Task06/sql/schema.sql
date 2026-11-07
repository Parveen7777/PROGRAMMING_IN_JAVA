-- Campus Event Registration - MySQL 8 schema
CREATE DATABASE IF NOT EXISTS campus_events CHARACTER SET utf8mb4;
USE campus_events;

CREATE TABLE IF NOT EXISTS events (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80) NOT NULL UNIQUE,
    event_date  DATE        NOT NULL,
    venue       VARCHAR(80) NOT NULL
);

CREATE TABLE IF NOT EXISTS registrations (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_reg   VARCHAR(15)  NOT NULL,
    student_name  VARCHAR(60)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    event_id      INT          NOT NULL,
    registered_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(id),
    -- RULE: a student cannot register twice for the same event (enforced by the database itself)
    CONSTRAINT uq_student_event UNIQUE (student_reg, event_id),
    INDEX idx_event (event_id)
);

INSERT IGNORE INTO events(name, event_date, venue) VALUES
 ('Hackathon 2026',   '2026-12-05', 'Main Auditorium'),
 ('Dance Fiesta',     '2026-12-12', 'Open Air Theatre'),
 ('Quiz Bowl',        '2026-12-15', 'Seminar Hall 2'),
 ('Photography Walk', '2026-12-20', 'Campus Garden');

-- Sample rows used for the demonstration
INSERT IGNORE INTO registrations(student_reg, student_name, email, event_id) VALUES
 ('192211001', 'Aarav Kumar',  'aarav@example.com', 1),
 ('192211002', 'Diya Sharma',  'diya@example.com',  1),
 ('192211003', 'Rohan Mehta',  'rohan@example.com', 2);

-- Queries used by the application (for the report)
-- Page of 20 rows for one event:
--   SELECT r.id, r.student_reg, r.student_name, r.email, e.name, r.registered_at
--   FROM registrations r JOIN events e ON e.id = r.event_id
--   WHERE e.name = ? ORDER BY r.id LIMIT 20 OFFSET 40;
-- Duplicate test (must fail with error 1062):
--   INSERT INTO registrations(student_reg, student_name, email, event_id) VALUES ('192211001','Aarav Kumar','aarav@example.com',1);
