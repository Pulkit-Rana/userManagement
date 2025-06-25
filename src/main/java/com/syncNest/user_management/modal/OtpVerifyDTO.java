package com.syncNest.user_management.modal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyDTO {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String otp;
}