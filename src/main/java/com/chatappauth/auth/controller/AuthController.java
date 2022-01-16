package com.chatappauth.auth.controller;

import com.chatappauth.auth.business.AuthBusiness;
import com.chatappauth.auth.controller.projection.EmailValidationProjection;
import com.chatappauth.auth.dto.JwtResponse;
import com.chatcomponents.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.websocket.server.PathParam;
import javax.xml.bind.ValidationException;
import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthBusiness authBusiness;

    @PostMapping(path = "signin")
    public JwtResponse signinUser(@RequestBody User user) {
        return authBusiness.signinUser(user);
    }

    @PostMapping(path = "signup")
    public EmailValidationProjection registerUser(@RequestBody User user) throws ValidationException, MessagingException, IOException {
        return authBusiness.signupUser(user);
    }

    @GetMapping(path = "validate")
    public ResponseEntity validate(@RequestParam(name = "validationCode") String validationCode,
                                   @RequestParam(name = "username") String username) {
        return authBusiness.validate(validationCode, username);
    }
}
