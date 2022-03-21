package com.chatappauth.auth.customauthentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

public class CustomPreAuthenticationChecks implements UserDetailsChecker {

    public CustomPreAuthenticationChecks() {
    }

    @Override
    public void check(UserDetails userDetails) {
    }
}
