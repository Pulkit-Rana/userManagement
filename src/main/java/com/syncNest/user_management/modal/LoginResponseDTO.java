package com.syncNest.user_management.modal;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private int expiresIn;
    private String tokenType;
    private UserDTO user;
}
