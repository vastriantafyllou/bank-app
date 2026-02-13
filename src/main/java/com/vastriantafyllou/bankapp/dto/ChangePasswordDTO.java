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
public class ChangePasswordDTO {

    @NotBlank(message = "Το τρέχον password είναι υποχρεωτικό")
    private String currentPassword;

    @NotBlank(message = "Το νέο password είναι υποχρεωτικό")
    @Size(min = 6, max = 72, message = "Το νέο password πρέπει να είναι 6-72 χαρακτήρες")
    private String newPassword;

    @NotBlank(message = "Η επιβεβαίωση password είναι υποχρεωτική")
    private String confirmNewPassword;
}
