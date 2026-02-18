package com.vastriantafyllou.bankapp.controller;

import com.vastriantafyllou.bankapp.core.enums.Role;
import com.vastriantafyllou.bankapp.dto.UserReadOnlyDTO;
import com.vastriantafyllou.bankapp.service.IAccountService;
import com.vastriantafyllou.bankapp.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;
    private final IAccountService accountService;

    @GetMapping
    public String dashboard(Model model) {
        long userCount = userService.countUsers();
        List<?> allAccounts = accountService.getAllAccounts(null, true);
        int accountCount = allAccounts.size();
        BigDecimal totalBalance = allAccounts.stream()
                .map(a -> ((com.vastriantafyllou.bankapp.dto.AccountReadOnlyDTO) a).getBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("userCount", userCount);
        model.addAttribute("accountCount", accountCount);
        model.addAttribute("totalBalance", totalBalance);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<UserReadOnlyDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/{id}/block")
    public String blockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.blockUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Ο χρήστης αποκλείστηκε επιτυχώς!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unblock")
    public String unblockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.unblockUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Ο αποκλεισμός του χρήστη αφαιρέθηκε!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/assign-role")
    public String assignRole(@PathVariable Long id,
                             @RequestParam("role") String roleName,
                             RedirectAttributes redirectAttributes) {
        Role role = Role.valueOf(roleName);
        userService.assignRole(id, role);
        redirectAttributes.addFlashAttribute("successMessage", "Ο ρόλος ανατέθηκε επιτυχώς!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/remove-role")
    public String removeRole(@PathVariable Long id,
                             @RequestParam("role") String roleName,
                             RedirectAttributes redirectAttributes) {
        Role role = Role.valueOf(roleName);
        userService.removeRole(id, role);
        redirectAttributes.addFlashAttribute("successMessage", "Ο ρόλος αφαιρέθηκε επιτυχώς!");
        return "redirect:/admin/users";
    }
}
