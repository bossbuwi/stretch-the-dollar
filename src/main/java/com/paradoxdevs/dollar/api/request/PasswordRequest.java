package com.paradoxdevs.dollar.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PasswordRequest {
    @NotBlank(message = "username is required.")
    private String username;
    @NotBlank(message = "password is required.")
    private String password;
    @NotBlank(message = "newPassword is required.")
    private String newPassword;
    @NotBlank(message = "confirmNewPassword is required.")
    private String confirmNewPassword;
}
