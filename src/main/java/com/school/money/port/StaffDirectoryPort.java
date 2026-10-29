package com.school.money.port;

import java.util.List;

/** Reads the school's user table (UserReg). Only place that knows about it. */
public interface StaffDirectoryPort {

    /** A staff member. username is the login name (UserReg.getUsername() returns the email). */
    record Staff(String username, String name, String role) {}

    /** Everyone who works at the school: all enabled users except parents and students. */
    List<Staff> staff();

    /** Email addresses of enabled users holding any of these roles (for alerts). */
    List<String> emailsOfRoles(String... roles);

    /** Display name for a login name, or the login name itself if unknown. */
    String nameOf(String username);
}