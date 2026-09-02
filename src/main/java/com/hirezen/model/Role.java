package com.hirezen.model;

/**
 * The three user roles supported by the platform.
 * Stored in the database as plain names (ADMIN / RECRUITER / JOB_SEEKER);
 * the "ROLE_" prefix required by Spring Security is added only when the
 * GrantedAuthority is built (see CustomAuthenticationProvider), so it never
 * has to be duplicated or kept in sync in two places.
 */
public enum Role {
    ADMIN,
    RECRUITER,
    JOB_SEEKER;

    public String authority() {
        return "ROLE_" + name();
    }

    /** Prefix used to build each user's public Hirezen ID, e.g. HZS1, HZR1, HZA1. */
    public String idPrefix() {
        return switch (this) {
            case ADMIN -> "HZA";
            case RECRUITER -> "HZR";
            case JOB_SEEKER -> "HZS";
        };
    }
}
