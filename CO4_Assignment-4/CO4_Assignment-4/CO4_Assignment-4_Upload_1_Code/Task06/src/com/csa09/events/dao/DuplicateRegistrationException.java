package com.csa09.events.dao;

/** Thrown when (student, event) already exists - maps MySQL error 1062 / SQLState 23000. */
public class DuplicateRegistrationException extends DaoException {
    public DuplicateRegistrationException(String message) { super(message); }
}
