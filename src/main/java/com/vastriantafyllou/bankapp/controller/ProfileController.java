package com.vastriantafyllou.bankapp.controller;

import com.vastriantafyllou.bankapp.core.exception.WrongPasswordException;
import com.vastriantafyllou.bankapp.dto.ChangePasswordDTO;
import com.vastriantafyllou.bankapp.dto.UpdateProfileDTO;
import com.vastriantafyllou.bankapp.model.AppUser;
import com.vastriantafyllou.bankapp.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IUserService userService;

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        AppUser user = userService.getByUsername(authentication.getName());

        UpdateProfileDTO profileDTO = new UpdateProfileDTO(
                user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone()
        );
        model.addAttribute("user", user);
        model.addAttribute("profileDTO", profileDTO);
        model.addAttribute("passwordDTO", new ChangePasswordDTO());
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("profileDTO") UpdateProfileDTO dto,
                                BindingResult bindingResult,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            AppUser user = userService.getByUsername(authentication.getName());
            model.addAttribute("user", user);
            model.addAttribute("passwordDTO", new ChangePasswordDTO());
            return "profile";
        }

        userService.updateProfile(authentication.getName(), dto);
        redirectAttributes.addFlashAttribute("successMessage", "Το προφίλ ενημερώθηκε επιτυχώς!");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDTO") ChangePasswordDTO dto,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors() && !dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            bindingResult.rejectValue("confirmNewPassword", "error.confirmNewPassword", "Τα passwords δεν ταιριάζουν");
        }

        if (bindingResult.hasErrors()) {
            AppUser user = userService.getByUsername(authentication.getName());
            model.addAttribute("user", user);
            model.addAttribute("profileDTO", new UpdateProfileDTO(
                    user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone()
            ));
            return "profile";
        }

        try {
            userService.changePassword(authentication.getName(), dto);
        } catch (WrongPasswordException e) {
            bindingResult.rejectValue("currentPassword", "error.currentPassword", e.getMessage());
            AppUser user = userService.getByUsername(authentication.getName());
            model.addAttribute("user", user);
            model.addAttribute("profileDTO", new UpdateProfileDTO(
                    user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone()
            ));
            return "profile";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Το password άλλαξε επιτυχώς!");
        return "redirect:/profile";
    }
}
