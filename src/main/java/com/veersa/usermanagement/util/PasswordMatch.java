package com.veersa.usermanagement.util;

public @interface PasswordMatch {
    String message() default "Passwords do not match";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};

    String passwordField();

    String passwordConfirmationField();
}
