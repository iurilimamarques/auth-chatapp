package com.chatappauth.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtValidationDto {

    private String status;
    private String message;

    private Long userId;
    private String email;
    private LocalDateTime expirationTime;

    public JwtValidationDto(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
