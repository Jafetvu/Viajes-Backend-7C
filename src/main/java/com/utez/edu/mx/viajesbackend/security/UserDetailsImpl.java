package com.utez.edu.mx.viajesbackend.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.utez.edu.mx.viajesbackend.modules.user.User;

public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String name;
    private final String paternalSurname;
    private final String maternalSurname;
    private final String email;
    private final String phone;
    private final java.time.LocalDateTime createdAt;
    private final boolean isActive;
    private final List<GrantedAuthority> authorities;

    
    public UserDetailsImpl(User user) {
        this.id         = user.getId();
        this.username   = user.getUsername();
        this.password   = user.getPassword();
        this.name       = user.getName();
        this.paternalSurname = user.getSurname();
        this.maternalSurname = user.getLastname();
        this.email      = user.getEmail();
        this.phone      = user.getPhoneNumber();
        this.createdAt  = user.getCreatedAt();
        this.isActive   = user.isStatus();
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + roleName)
        );
    }

    public String getName() { return name; }
    public String getPaternalSurname() { return paternalSurname; }
    public String getMaternalSurname() { return maternalSurname; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return isActive; 
    }
    
    @Override 
    public boolean isAccountNonLocked() { 
        return isActive; 
    }
    
    @Override 
    public boolean isCredentialsNonExpired() { 
        return true; 
    }
    
    @Override 
    public boolean isEnabled() { 
        return isActive; 
    }

    public Long getId() { 
        return id; 
    }
}
