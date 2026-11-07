package com.csa09.events.dao;

/** Any data-access failure. Wraps SQLException so upper layers never depend on JDBC types. */
public class DaoException extends Exception {
    public DaoException(String message, Throwable cause) { super(message, cause); }
    public DaoException(String message) { super(message); }
}
