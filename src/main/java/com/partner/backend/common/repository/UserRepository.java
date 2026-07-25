package com.partner.backend.common.repository;

import com.partner.backend.common.entity.User;
import com.partner.backend.common.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /** True if this CNIC is already used for the given role (one account per CNIC per module). */
    boolean existsByNormalizedCnicAndRole(String normalizedCnic, UserRole role);
    long countByRole(UserRole role);
}
