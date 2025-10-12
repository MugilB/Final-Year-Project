package com.securevoting.repository;

import com.securevoting.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, String> {

    Optional<UserDetails> findByVoterId(String voterId);

    List<UserDetails> findByWardId(Integer wardId);

    boolean existsByVoterId(String voterId);

    void deleteByVoterId(String voterId);
}