package com.chatappauth.auth.controller;

import com.chatappauth.auth.repository.UserRepository;
import com.chatappauth.auth.util.JwtUtil;
import com.chatcomponents.QUser;
import com.chatcomponents.User;
import com.chatcomponents.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/auth/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping(path = "code-validation/{code}")
    public ResponseEntity signinUser(@PathVariable String code, HttpServletRequest request) {
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
