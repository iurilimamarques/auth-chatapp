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
import com.google.common.collect.Lists;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.service.EmailService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.bind.ValidationException;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
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
    private EmailService emailService;

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

    public EmailValidationProjection signupUser(User user) throws ValidationException, AddressException, IOException {
        if (userRepository.exists(QUser.user.email.eq(user.getEmail())))
            throw new ValidationException("This e-mail address is already being used");

        String validationCode = generateCode(6);
        sendEmail(validationCode, user.getEmail());

        user.setCodeValidation(validationCode);
        user.setStatus(UserStatus.USER_VALIDATION);
        user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        User savedUser = (User) userRepository.save(user);

        Authority authority = new Authority("ROLE_ADMIN", savedUser);
        authorityRepository.save(authority);

        EmailValidationProjection emailValidationProjection = new EmailValidationProjection();
        emailValidationProjection.setEmail(user.getEmail());
        return emailValidationProjection;
    }

    private void sendEmail(String validationCode, String userEmail) throws AddressException, IOException {
        URL url = getClass().getResource("../java/com/chatappauth/auth/email-body.html");
        File fis = new File(url.getPath());

        final Email email = DefaultEmail.builder()
                .from(new InternetAddress("support@chatapp.com"))
                .to(Lists.newArrayList(new InternetAddress(userEmail)))
                .subject("Chatapp code validation")
                .body(fis.toString())
                .encoding(String.valueOf(Charset.forName("UTF-8"))).build();

        emailService.send(email);
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
