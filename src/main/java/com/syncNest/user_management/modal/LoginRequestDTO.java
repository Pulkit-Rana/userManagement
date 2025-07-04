package com.syncNest.user_management.modal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
public class LoginRequestDTO {

    @NotEmpty(message = "The username cannot be empty")
    @NotBlank(message = "The username cannot be blank")
    @Email(message = "Email is not valid")
    private String username;

    @NotEmpty(message = "The password cannot be empty")
    @NotBlank(message = "The password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
