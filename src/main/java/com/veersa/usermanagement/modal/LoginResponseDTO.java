package com.veersa.usermanagement.modal;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LoginResponseDTO {

    private String accessToken;
    private String token;
}
