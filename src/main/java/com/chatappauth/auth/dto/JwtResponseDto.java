package com.chatappauth.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class JwtResponseDto {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String name;
    private LocalDateTime expirationTime;
    private List<String> roles;

    public JwtResponseDto(String token, Long id, String username, String name, LocalDateTime expirationTime, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.name = name;
        this.expirationTime = expirationTime;
        this.roles = roles;
    }
}
