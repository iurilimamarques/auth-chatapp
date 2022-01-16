package com.chatappauth.auth.service.springsecurityfilter;

import com.chatappauth.auth.config.SecurityConstants;
import com.chatappauth.auth.dto.UserPrincipal;
import com.chatappauth.auth.repository.UserRepository;
import com.chatcomponents.QUser;
import com.chatcomponents.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserPrincipal loggedUser = null;

        try {
            User user = (User) userRepository.findOne(QUser.user.email.eq(username))
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            long expirantionMilli = Instant.now().toEpochMilli() + SecurityConstants.EXPIRATION_TIME;
            LocalDateTime dateExpiration = Instant.ofEpochMilli(expirantionMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();

            loggedUser = new UserPrincipal(user, dateExpiration);
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("Username not found");
        }

        return loggedUser;
    }
}
