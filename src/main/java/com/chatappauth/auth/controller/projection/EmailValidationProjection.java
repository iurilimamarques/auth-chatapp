package com.chatappauth.auth.controller.projection;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EmailValidationProjection implements Serializable {

    private String email;
}
