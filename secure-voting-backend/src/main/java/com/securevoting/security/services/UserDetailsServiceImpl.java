package com.securevoting.security.services;

import com.securevoting.model.User;
import com.securevoting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Find user by VoterID (primary key)
        User user = userRepository.findByVoterId(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with VoterID: " + identifier));

        return UserDetailsImpl.build(user);
    }

}