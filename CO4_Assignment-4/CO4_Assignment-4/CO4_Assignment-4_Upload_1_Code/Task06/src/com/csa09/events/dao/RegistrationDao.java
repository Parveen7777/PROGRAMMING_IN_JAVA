package com.csa09.events.dao;

import com.csa09.events.model.Registration;
import java.util.List;

/** Data-access contract. eventOrNull == null means "all events". */
public interface RegistrationDao {
    List<String> eventNames() throws DaoException;
    void add(String studentReg, String name, String email, String eventName) throws DaoException;
    int count(String eventOrNull) throws DaoException;
    List<Registration> page(String eventOrNull, int offset, int limit) throws DaoException;
    List<Registration> all(String eventOrNull) throws DaoException;
}
