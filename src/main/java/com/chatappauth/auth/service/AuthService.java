package com.chatappauth.auth.service;

import com.chatappauth.auth.controller.projection.EmailValidationDto;
import com.chatappauth.auth.dto.JwtResponseDto;
import com.chatappauth.auth.dto.UserPrincipalDto;
import com.chatappauth.auth.repository.AuthorityRepository;
import com.chatappauth.auth.repository.UserRepository;
import com.chatappauth.auth.util.JwtUtil;
import com.chatcomponents.Authority;
import com.chatcomponents.QUser;
import com.chatcomponents.User;
import com.chatcomponents.UserStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.ValidationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JavaMailSender javaMailSender;

    public AuthService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       AuthorityRepository authorityRepository,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       JavaMailSender javaMailSender) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.javaMailSender = javaMailSender;
    }

    public JwtResponseDto signinUser(User user) {
        return signin(user.getEmail(), user.getUserPassword());
    }

    private JwtResponseDto signin(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateJwtToken(authentication);

            UserPrincipalDto userDetails = (UserPrincipalDto) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            return new JwtResponseDto(jwt, userDetails.getUser().getId(), userDetails.getUser().getEmail(), userDetails.getUser().getName(), userDetails.getExpirationTime(), roles);
        } catch (Exception e) {
            String message = e.getMessage();
            if (e instanceof BadCredentialsException) {
                message = "Password is incorrect";
            } else if(e instanceof LockedException || e instanceof DisabledException) {
                message = "The user account is not enabled yet";
            }
            throw new RuntimeException(message);
        }
    }

    public EmailValidationDto signupUser(User user) throws ValidationException, MessagingException, IOException {
        if (userRepository.exists(QUser.user.email.eq(user.getEmail())))
            throw new ValidationException("This e-mail address is already being used");

        String validationCode = generateCode(6);

        user.setCodeValidation(validationCode);
        user.setStatus(UserStatus.USER_VALIDATION);
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        User savedUser = (User) userRepository.save(user);

        Authority authority = new Authority("ROLE_ADMIN", savedUser);
        authorityRepository.save(authority);

        sendEmail(validationCode, user.getEmail());

        EmailValidationDto emailValidationDto = new EmailValidationDto();
        emailValidationDto.setEmail(user.getEmail());
        return emailValidationDto;
    }

    private void sendEmail(String validationCode, String userEmail) throws MessagingException, IOException {
        Optional<User> user = userRepository.findOne(QUser.user.email.eq(userEmail));
        String htmlBody = loadEmailBody(validationCode, user.get().getName());

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        helper.setText(htmlBody, true);
        helper.setTo(userEmail);
        helper.setSubject("Chatapp code validation");
        helper.setFrom("support@chatapp.com");
        javaMailSender.send(mimeMessage);
    }

    private String loadEmailBody(String validationCode, String name) throws IOException {
        ClassPathResource resource =  new ClassPathResource("/email-body.html", AuthService.class);
        InputStream inputStream = resource.getInputStream();
        String htmlBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        return htmlBody.replace("{{codeVerification}}", validationCode)
                .replace("{{username}}", name);
    }

    private String generateCode(int codeLength) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < codeLength) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String ramdomCode = salt.toString();
        return ramdomCode;
    }

    public ResponseEntity validate(String validationCode, String username) {
        Optional<User> userValidation = userRepository.findOne(QUser.user.email.eq(username).and(QUser.user.codeValidation.eq(validationCode)));

        if (userValidation.isPresent()) {
            User user = userValidation.get();
            user.setStatus(UserStatus.ENABLED);
            userRepository.save(user);
        } else {
            throw new javax.validation.ValidationException("Invalid code");
        }
        return ResponseEntity.ok().build();
    }
}
