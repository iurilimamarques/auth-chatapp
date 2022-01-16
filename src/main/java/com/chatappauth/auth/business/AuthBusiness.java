package com.chatappauth.auth.business;

import com.chatappauth.auth.controller.projection.EmailValidationProjection;
import com.chatappauth.auth.dto.JwtResponse;
import com.chatappauth.auth.dto.UserPrincipal;
import com.chatappauth.auth.repository.AuthorityRepository;
import com.chatappauth.auth.repository.UserRepository;
import com.chatappauth.auth.util.JwtUtil;
import com.chatcomponents.Authority;
import com.chatcomponents.QUser;
import com.chatcomponents.User;
import com.chatcomponents.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AuthBusiness {


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender javaMailSender;

    public JwtResponse signinUser(User user) {
        return signin(user.getEmail(), user.getUserPassword());
    }

    private JwtResponse signin(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateJwtToken(authentication);

            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            return new JwtResponse(jwt, userDetails.getUser().getId(), userDetails.getUser().getEmail(), userDetails.getUser().getName(), userDetails.getExpirationTime(), roles);
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

    public EmailValidationProjection signupUser(User user) throws ValidationException, MessagingException, IOException {
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

        EmailValidationProjection emailValidationProjection = new EmailValidationProjection();
        emailValidationProjection.setEmail(user.getEmail());
        return emailValidationProjection;
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
        ClassPathResource resource =  new ClassPathResource("/email-body.html", AuthBusiness.class);
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
