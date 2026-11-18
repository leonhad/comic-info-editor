package com.github.leonhad.integration;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class Staff implements Serializable {

    private String name;
    private String role;

}
