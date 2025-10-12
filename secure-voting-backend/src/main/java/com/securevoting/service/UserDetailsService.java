package com.securevoting.service;

import com.securevoting.model.UserDetails;
import com.securevoting.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsService {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    public UserDetails save(UserDetails userDetails) {
        return userDetailsRepository.save(userDetails);
    }

    public Optional<UserDetails> findByVoterId(String voterId) {
        return userDetailsRepository.findByVoterId(voterId);
    }


    public List<UserDetails> findAll() {
        return userDetailsRepository.findAll();
    }

    public List<UserDetails> findByWardId(Integer wardId) {
        return userDetailsRepository.findByWardId(wardId);
    }

    public void deleteByVoterId(String voterId) {
        userDetailsRepository.deleteByVoterId(voterId);
    }

    public boolean existsByVoterId(String voterId) {
        return userDetailsRepository.existsByVoterId(voterId);
    }


    public Optional<UserDetails> getUserDetailsByVoterId(String voterId) {
        return userDetailsRepository.findByVoterId(voterId);
    }
}

