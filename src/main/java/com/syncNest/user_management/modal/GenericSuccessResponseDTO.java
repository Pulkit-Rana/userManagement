// com.syncNest.user_management.modal.GenericSuccessResponseDTO.java
package com.syncNest.user_management.modal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericSuccessResponseDTO {
    private String message;
    private Object data;
}
