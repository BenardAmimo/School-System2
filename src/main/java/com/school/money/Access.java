package com.school.money;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

/** Role checks that do not depend on @EnableMethodSecurity being switched on. */
public final class Access {
    private Access() {}

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ADMIN = "ADMIN";
    public static final String BURSAR = "BURSAR";

    /** The signed-in username (the login email). */
    public static String user(Authentication a) {
        if (a == null || !a.isAuthenticated() || a.getName() == null) throw MoneyException.forbidden("Not signed in.");
        return a.getName();
    }

    public static boolean has(Authentication a, String role) {
        if (a == null) return false;
        for (GrantedAuthority g : a.getAuthorities()) {
            String n = g.getAuthority();
            if (n.equals(role) || n.equals("ROLE_" + role)) return true;
        }
        return false;
    }

    public static boolean hasAny(Authentication a, String... roles) {
        for (String r : roles) if (has(a, r)) return true;
        return false;
    }

    /** Throws 403 unless the caller has one of the roles. Returns the username. */
    public static String require(Authentication a, String... roles) {
        String u = user(a);
        if (!hasAny(a, roles)) throw MoneyException.forbidden("Your role cannot do this. Needs one of: " + String.join(", ", roles));
        return u;
    }

    /** ADMIN or SUPER_ADMIN: may approve. Returns the username. */
    public static String approver(Authentication a) { return require(a, ADMIN, SUPER_ADMIN); }

    /** BURSAR, ADMIN or SUPER_ADMIN: may prepare. Returns the username. */
    public static String preparer(Authentication a) { return require(a, BURSAR, ADMIN, SUPER_ADMIN); }

    public static final Set<String> ALL = Set.of(SUPER_ADMIN, ADMIN, BURSAR);
}