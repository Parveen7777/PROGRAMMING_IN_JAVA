package com.csa09.events.service;

import java.util.List;

/** Carries every validation message at once so the user can fix all fields in one go. */
public class ValidationException extends Exception {
    private final List<String> problems;
    public ValidationException(List<String> problems) { super(String.join("; ", problems)); this.problems = problems; }
    public List<String> problems() { return problems; }
}
