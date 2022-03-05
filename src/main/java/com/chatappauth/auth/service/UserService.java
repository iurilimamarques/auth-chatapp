package com.chatappauth.auth.service;

import com.chatappauth.auth.repository.UserRepository;
import com.chatappauth.auth.util.JwtUtil;
import com.chatcomponents.QUser;
import com.chatcomponents.User;
import com.chatcomponents.UserStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class UserService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public UserService(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    public ResponseEntity<Object> validateUserCode(String code, HttpServletRequest request) {
        String email = jwtUtil.getUserNameFromJwtToken(request.getHeader("Authorization").replace("Bearer ", ""));
        Optional optional = userRepository.findOne(QUser.user.email.eq(email).and(QUser.user.status.eq(UserStatus.USER_VALIDATION)));
        User user = (User) optional.get();
        if (user.getCodeValidation().equals(code)) {
            user.setStatus(UserStatus.ENABLED);
            userRepository.save(user);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().body("Code doens't match");
    }
}
