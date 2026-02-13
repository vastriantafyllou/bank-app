package com.vastriantafyllou.bankapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {

    @Size(max = 64, message = "Το όνομα δεν μπορεί να υπερβαίνει τους 64 χαρακτήρες")
    private String firstName;

    @Size(max = 64, message = "Το επώνυμο δεν μπορεί να υπερβαίνει τους 64 χαρακτήρες")
    private String lastName;

    @Email(message = "Μη έγκυρη διεύθυνση email")
    @Size(max = 128, message = "Το email δεν μπορεί να υπερβαίνει τους 128 χαρακτήρες")
    private String email;

    @Size(max = 20, message = "Το τηλέφωνο δεν μπορεί να υπερβαίνει τους 20 χαρακτήρες")
    private String phone;
}
