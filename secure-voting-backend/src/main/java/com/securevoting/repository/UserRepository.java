package com.securevoting.repository;

import com.securevoting.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByVoterId(String voterId);
    Optional<User> findByEmail(String email);
    Boolean existsByVoterId(String voterId);
    Boolean existsByEmail(String email);
    void deleteByVoterId(String voterId);
}