# Hirezen — Phase 1 (Signup / Signin / Dashboard)

This is the first working slice of Hirezen: registration, login, and
role-based dashboards for the three roles (`ADMIN`, `RECRUITER`,
`JOB_SEEKER`), backed by MySQL via Spring Data JPA.

## What's included

- **Signup** (`/signup`) — creates a `JOB_SEEKER` or `RECRUITER` account.
  Admin accounts are intentionally *not* self-registerable (see "Creating
  an admin" below) — this was a real bug in the original demo, where the
  signup form let anyone pick `ROLE_ADMIN`.
- **Signin** (`/signin`) — Spring Security form login, email + password.
- **Dashboards** — `/dashboard` redirects to whichever of
  `/dashboard/admin`, `/dashboard/recruiter`, `/dashboard/job-seeker`
  matches the signed-in user's role. Each is protected so only that role
  can view it. The stat cards are wired up but show 0s for now — they'll
  populate once the Job/Application entities from later phases exist.

## Fixes made vs. the original demo code

- Passwords are now checked with `PasswordEncoder.matches(...)` everywhere
  (the demo's leftover `UserService.login()` compared raw strings).
- Login no longer reveals whether an email exists vs. the password being
  wrong — both fail with the same generic message.
- Roles are a proper enum instead of free-text strings, so the DB and
  `hasRole(...)` checks can't drift out of sync from a typo.
- Signup can't be used to create an ADMIN account, and the role is
  validated server-side regardless of what the form sends.
- Removed `System.out.println`/debug prints in favor of SLF4J logging.
- Switched from an in-memory `List<User>` to MySQL + Spring Data JPA, per
  the doc's Phase 2.

## Running it

1. Make sure MySQL is running locally (or update the connection details).
2. Edit `src/main/resources/application.properties` if your MySQL
   username/password/port differ from the defaults (`root` / `root` /
   `3306`). The database `hirezen` is created automatically on first run.
3. `mvn spring-boot:run`
4. Visit `http://localhost:8080/signup` to create an account, then
   `http://localhost:8080/signin`.

## Creating an admin account

There's no self-service admin signup by design. For now, the simplest way
to get an admin account is to sign up as normal, then update the row
directly:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

A dedicated admin-provisioning flow (e.g. seeded on startup, or an
"invite" mechanism) is a good candidate for a later phase.

## Next steps (per the project doc)

- `Job`, `Company`, `Application`, `Resume`, `Interview` entities
- Recruiter job-posting CRUD
- Job seeker search/filter/apply flow
- Wiring the dashboard stat cards to real queries
- REST API layer under `/api/**`

## Project coordinates

- Base package: `com.hirezen`
- Maven: `groupId=com.hirezen`, `artifactId=hirezen`
- Database name: `hirezen`
