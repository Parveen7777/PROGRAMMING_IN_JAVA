package com.csa09.events.model;

import java.time.LocalDateTime;

/** One row of the registrations table (joined with the event name). */
public record Registration(long id, String studentReg, String studentName, String email,
                           String eventName, LocalDateTime registeredAt) { }
