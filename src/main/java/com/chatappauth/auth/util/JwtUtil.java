package com.chatappauth.auth.util;

import com.auth0.jwt.JWT;
import com.chatappauth.auth.dto.JwtValidationDto;
import com.chatappauth.auth.dto.UserPrincipalDto;
import com.chatappauth.auth.repository.UserRepository;
import com.chatcomponents.QUser;
import com.chatcomponents.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {

    private final UserRepository userRepository;

    @Value("${auth-chatapp.key-secret}")
    private String keySecret;

    public JwtUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateJwtToken(Authentication authentication) {
        UserPrincipalDto userPrincipalDto = (UserPrincipalDto) authentication.getPrincipal();
        ZonedDateTime zdt = userPrincipalDto.getExpirationTime().atZone(ZoneId.of("America/Sao_Paulo"));

        return Jwts.builder()
                .setSubject((userPrincipalDto.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(Date.from(zdt.toInstant()))
                .signWith(SignatureAlgorithm.HS512, keySecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(keySecret).parseClaimsJws(token).getBody().getSubject();
    }

    public JwtValidationDto validateJwtToken(String authToken) {
        JwtValidationDto jwtValidationDto = null;

        try {
            Jwts.parser().setSigningKey(keySecret).parseClaimsJws(authToken);

            Optional optional = userRepository.findOne(QUser.user.email.eq(getUserNameFromJwtToken(authToken)));
            User user = (User) optional.get();

            LocalDateTime ldt = LocalDateTime.ofInstant(JWT.decode(authToken).getExpiresAt().toInstant(), ZoneId.systemDefault());
            jwtValidationDto = new JwtValidationDto("JWT_VALID", "", user.getId(), user.getEmail(), ldt);

            return jwtValidationDto;
        } catch (SignatureException e) {
            jwtValidationDto = new JwtValidationDto("JWT_NOT_VALID", "Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            jwtValidationDto = new JwtValidationDto("JWT_NOT_VALID", "Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            jwtValidationDto = new JwtValidationDto("JWT_NOT_VALID", "JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            jwtValidationDto = new JwtValidationDto("JWT_NOT_VALID", "JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            jwtValidationDto = new JwtValidationDto("JWT_NOT_VALID", "JWT claims string is empty: " + e.getMessage());
        }
        return jwtValidationDto;
    }

    public String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer "))
            return headerAuth.substring(7, headerAuth.length());

        return null;
    }

}
