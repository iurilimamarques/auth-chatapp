package com.chatappauth.auth.controller;

import com.chatappauth.auth.dto.JwtValidation;
import com.chatappauth.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/jwt-validation")
public class JwtValidationController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping(path = "validate-token/{token}")
    public ResponseEntity validateToken(@PathVariable("token") String token) {
        JwtValidation jwtValidation = jwtUtil.validateJwtToken(token);
        return ResponseEntity.ok(jwtValidation);
    }


}
