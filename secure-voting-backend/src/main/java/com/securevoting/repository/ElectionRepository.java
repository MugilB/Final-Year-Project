package com.securevoting.repository;

import com.securevoting.model.Election;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectionRepository extends JpaRepository<Election, Integer> {
    List<Election> findByStatus(String status);
    List<Election> findByStatusIn(List<String> statuses);
}