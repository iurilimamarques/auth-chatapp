package com.chatappauth.auth.controller;

import com.chatappauth.auth.dto.JwtValidationDto;
import com.chatappauth.auth.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/jwt-validation")
public class JwtValidationController {

    private final JwtUtil jwtUtil;

    public JwtValidationController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping(path = "validate-token/{token}")
    public ResponseEntity<Object> validateToken(@PathVariable("token") String token) {
        JwtValidationDto jwtValidationDto = jwtUtil.validateJwtToken(token);
        return ResponseEntity.ok(jwtValidationDto);
    }
}
