package com.learning.api.security;


import com.learning.api.entity.*;
import com.learning.api.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {

    private final User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    // role = Authorities
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        String role = "";

        if (user.getRole() == UserRole.STUDENT){
            role = "ROLE_STUDENT";
        }else if(user.getRole() == UserRole.TUTOR){
            role = "ROLE_TUTOR";
        }else if(user.getRole() == UserRole.ADMIN){
            role = "ROLE_ADMIN";
        }else{
            throw new IllegalArgumentException("未知角色: " + user.getRole());
        }

        return List.of(new SimpleGrantedAuthority(role));
    }

    public User getUser() {
        return user;
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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
        return true;
    }
}
