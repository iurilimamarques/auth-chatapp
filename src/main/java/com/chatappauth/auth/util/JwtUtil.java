package com.chatappauth.auth.util;

import com.auth0.jwt.JWT;
import com.chatappauth.auth.config.SecurityConstants;
import com.chatappauth.auth.dto.JwtValidation;
import com.chatappauth.auth.dto.UserPrincipal;
import com.chatappauth.auth.repository.UserRepository;
import com.chatcomponents.QUser;
import com.chatcomponents.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {

    @Autowired
    private UserRepository userRepository;

    public String generateJwtToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ZonedDateTime zdt = userPrincipal.getExpirationTime().atZone(ZoneId.of("America/Sao_Paulo"));

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(Date.from(zdt.toInstant()))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.KEY)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(SecurityConstants.KEY).parseClaimsJws(token).getBody().getSubject();
    }

    public JwtValidation validateJwtToken(String authToken) {
        JwtValidation jwtValidation = null;

        try {
            Jwts.parser().setSigningKey(SecurityConstants.KEY).parseClaimsJws(authToken);

            Optional optional = userRepository.findOne(QUser.user.email.eq(getUserNameFromJwtToken(authToken)));
            User user = (User) optional.get();

            LocalDateTime ldt = LocalDateTime.ofInstant(JWT.decode(authToken).getExpiresAt().toInstant(), ZoneId.systemDefault());
            jwtValidation = new JwtValidation("JWT_VALID", "", user.getId(), user.getEmail(), ldt);

            return jwtValidation;
        } catch (SignatureException e) {
            jwtValidation = new JwtValidation("JWT_NOT_VALID", "Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            jwtValidation = new JwtValidation("JWT_NOT_VALID", "Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            jwtValidation = new JwtValidation("JWT_NOT_VALID", "JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            jwtValidation = new JwtValidation("JWT_NOT_VALID", "JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            jwtValidation = new JwtValidation("JWT_NOT_VALID", "JWT claims string is empty: " + e.getMessage());
        }
        return jwtValidation;
    }

    public String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer "))
            return headerAuth.substring(7, headerAuth.length());

        return null;
    }

}
