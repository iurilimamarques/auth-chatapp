package com.chatappauth.auth.dto;

import com.chatcomponents.User;
import com.chatcomponents.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;

@Getter
public class UserPrincipal implements UserDetails {


    private User user;
    private LocalDateTime expirationTime;

    public UserPrincipal(User user, LocalDateTime expirationTime) {
        this.user = user;
        this.expirationTime = expirationTime;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getUserAuthorities();
    }

    @Override
    public String getPassword() {
        return user.getUserPassword();
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
        return !user.getStatus().equals(UserStatus.USER_VALIDATION);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        ZonedDateTime expirationTimeMillis = ZonedDateTime.of(getExpirationTime(), ZoneId.systemDefault());
        return expirationTimeMillis.toInstant().toEpochMilli() > new Date().getTime();
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus().equals(UserStatus.ENABLED);
    }
}
