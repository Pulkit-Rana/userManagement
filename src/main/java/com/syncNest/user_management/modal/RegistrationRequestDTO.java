package com.syncNest.user_management.modal;

import com.syncNest.user_management.util.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@PasswordMatch(passwordField = "password", passwordConfirmationField = "passwordConfirmation")
@Builder
public class RegistrationRequestDTO {

    @NotEmpty(message = "The username cannot be empty")
    @Email(message = "Email is not valid")
    private String username;

    @NotEmpty(message = "The password cannot be empty")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "must contain at least one uppercase letter, one lowercase letter, and one digit.")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    private String passwordConfirmation;
}
