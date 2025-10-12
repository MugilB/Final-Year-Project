package com.securevoting.service;

import com.securevoting.model.User;
import com.securevoting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByVoterId(String voterId) {
        return userRepository.findByVoterId(voterId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteByVoterId(String voterId) {
        userRepository.deleteByVoterId(voterId);
    }

    public boolean existsByVoterId(String voterId) {
        return userRepository.existsByVoterId(voterId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserByVoterId(String voterId) {
        return userRepository.findByVoterId(voterId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
