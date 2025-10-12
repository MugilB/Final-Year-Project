package com.securevoting.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.securevoting.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    // FIX: Remove the id field
    // private Long id;

    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;
    
    // Add approval status to track user approval
    private Integer approvalStatus;

    // FIX: Remove 'id' from the constructor
    public UserDetailsImpl(String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities, Integer approvalStatus) {
        // this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.approvalStatus = approvalStatus;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new UserDetailsImpl(
                // user.getId(), // <-- Remove this
                user.getVoterId(), // Use voterId as the primary identifier
                user.getEmail(), // Use actual email from User model
                user.getPassword(),
                authorities,
                user.getApprovalStatus());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // FIX: Remove the getId() method
    // public Long getId() {
    //     return id;
    // }

    public String getEmail() {
        return email;
    }

    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Only allow login if user is approved (approvalStatus = 1)
        // 0 = rejected, 1 = approved, 2 = pending
        return approvalStatus != null && approvalStatus == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(username, user.username); // <-- Compare by username now
    }
}