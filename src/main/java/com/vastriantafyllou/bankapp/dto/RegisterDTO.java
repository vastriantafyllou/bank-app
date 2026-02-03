package com.vastriantafyllou.bankapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

    @NotBlank(message = "Το username είναι υποχρεωτικό")
    @Size(min = 3, max = 64, message = "Το username πρέπει να είναι 3-64 χαρακτήρες")
    private String username;

    @NotBlank(message = "Το password είναι υποχρεωτικό")
    @Size(min = 6, max = 72, message = "Το password πρέπει να είναι 6-72 χαρακτήρες")
    private String password;

    @NotBlank(message = "Η επιβεβαίωση password είναι υποχρεωτική")
    private String confirmPassword;
}
