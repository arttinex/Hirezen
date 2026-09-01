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
}
