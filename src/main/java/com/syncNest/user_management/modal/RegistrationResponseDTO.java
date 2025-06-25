package com.syncNest.user_management.modal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationResponseDTO {

    private Long id;
    private String username;
    private String message;
}
