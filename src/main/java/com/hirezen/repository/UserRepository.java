package com.hirezen.repository;

import com.hirezen.model.Role;
import com.hirezen.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Used to generate the next sequential Hirezen ID for a role (e.g. HZS1, HZS2...). */
    long countByRole(Role role);

    /** Powers the navbar search - matches an exact Hirezen ID or a partial name. */
    List<User> findByHirezenIdIgnoreCaseOrNameContainingIgnoreCase(String hirezenId, String name);
}
