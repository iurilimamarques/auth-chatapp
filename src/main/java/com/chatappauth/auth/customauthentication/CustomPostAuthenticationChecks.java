package com.chatappauth.auth.customauthentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

public class CustomPostAuthenticationChecks implements UserDetailsChecker {

    protected final Log logger = LogFactory.getLog(this.getClass());
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    public CustomPostAuthenticationChecks() {
    }

    @Override
    public void check(UserDetails userDetails) {
        if (!userDetails.isAccountNonLocked()) {
            CustomPostAuthenticationChecks.this.logger.debug("Failed to authenticate since user account is locked");
            throw new LockedException(CustomPostAuthenticationChecks.this.messages.getMessage("CustomPostAuthenticationChecks.locked", "User account is locked"));
        } else if (!userDetails.isEnabled()) {
            CustomPostAuthenticationChecks.this.logger.debug("Failed to authenticate since user account is disabled");
            throw new DisabledException(CustomPostAuthenticationChecks.this.messages.getMessage("CustomPostAuthenticationChecks.disabled", "User is disabled"));
        } else if (!userDetails.isAccountNonExpired()) {
            CustomPostAuthenticationChecks.this.logger.debug("Failed to authenticate since user account has expired");
            throw new AccountExpiredException(CustomPostAuthenticationChecks.this.messages.getMessage("CustomPostAuthenticationChecks.expired", "User account has expired"));
        } else if (!userDetails.isCredentialsNonExpired()) {
            CustomPostAuthenticationChecks.this.logger.debug("Failed to authenticate since user account credentials have expired");
            throw new CredentialsExpiredException(CustomPostAuthenticationChecks.this.messages.getMessage("CustomPostAuthenticationChecks.credentialsExpired", "User credentials have expired"));
        }
    }
}
