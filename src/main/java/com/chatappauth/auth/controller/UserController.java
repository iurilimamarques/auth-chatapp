package com.chatappauth.auth.controller;

import com.chatappauth.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "code-validation/{code}")
    public ResponseEntity<Object> validateUserCode(@PathVariable String code, HttpServletRequest request) {
        return userService.validateUserCode(code, request);
    }
}
