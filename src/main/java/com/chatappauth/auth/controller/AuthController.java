package com.chatappauth.auth.controller;

import com.chatappauth.auth.service.AuthService;
import com.chatappauth.auth.controller.projection.EmailValidationDto;
import com.chatappauth.auth.dto.JwtResponseDto;
import com.chatcomponents.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.xml.bind.ValidationException;
import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "signin")
    public JwtResponseDto signinUser(@RequestBody User user) {
        return authService.signinUser(user);
    }

    @PostMapping(path = "signup")
    public EmailValidationDto registerUser(@RequestBody User user) throws ValidationException, MessagingException, IOException {
        return authService.signupUser(user);
    }

    @GetMapping(path = "validate")
    public ResponseEntity validate(@RequestParam(name = "validationCode") String validationCode,
                                   @RequestParam(name = "username") String username) {
        return authService.validate(validationCode, username);
    }
}
