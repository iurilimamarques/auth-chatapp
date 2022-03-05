package com.chatappauth.auth.service.springsecurityfilter;

import com.chatappauth.auth.dto.UserPrincipalDto;
import com.chatappauth.auth.repository.UserRepository;
import com.chatcomponents.QUser;
import com.chatcomponents.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Value("${auth-chatapp.expirationTime}")
    private long expirationTime;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserPrincipalDto loggedUser = null;

        try {
            User user = (User) userRepository.findOne(QUser.user.email.eq(username))
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            long expirantionMilli = Instant.now().toEpochMilli() + expirationTime;
            LocalDateTime dateExpiration = Instant.ofEpochMilli(expirantionMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();

            loggedUser = new UserPrincipalDto(user, dateExpiration);
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("Username not found");
        }

        return loggedUser;
    }
}
