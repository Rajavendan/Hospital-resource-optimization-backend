package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    java.util.List<User> findByRole(com.hospital.resource.optimization.model.Role role);
}
