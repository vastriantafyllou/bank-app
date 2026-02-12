package com.vastriantafyllou.bankapp.controller;

import com.vastriantafyllou.bankapp.core.exception.AccountAlreadyExistsException;
import com.vastriantafyllou.bankapp.core.exception.AccountNumberAlreadyExistsException;
import com.vastriantafyllou.bankapp.dto.AccountInsertDTO;
import com.vastriantafyllou.bankapp.dto.AccountReadOnlyDTO;
import com.vastriantafyllou.bankapp.dto.TransferDTO;
import com.vastriantafyllou.bankapp.dto.TransactionDTO;
import com.vastriantafyllou.bankapp.model.AccountTransaction;
import com.vastriantafyllou.bankapp.service.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;

    private static boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping
    public String listAccounts(Authentication authentication, Model model) {
        String username = authentication.getName();
        boolean admin = isAdmin(authentication);
        List<AccountReadOnlyDTO> accounts = accountService.getAllAccounts(username, admin);
        model.addAttribute("accounts", accounts);
        return "accounts/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("accountDTO", new AccountInsertDTO());
        return "accounts/create";
    }

    @PostMapping("/new")
    public String createAccount(@Valid @ModelAttribute("accountDTO") AccountInsertDTO dto,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "accounts/create";
        }

        try {
            accountService.createAccount(dto, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Ο λογαριασμός δημιουργήθηκε επιτυχώς!");
            return "redirect:/accounts";
        } catch (AccountAlreadyExistsException e) {
            bindingResult.rejectValue("iban", "error.iban", e.getMessage());
            return "accounts/create";
        } catch (AccountNumberAlreadyExistsException e) {
            bindingResult.rejectValue("accountNumber", "error.accountNumber", e.getMessage());
            return "accounts/create";
        }
    }

    @GetMapping("/{iban}")
    public String viewAccount(@PathVariable String iban, Authentication authentication, Model model) {
        String username = authentication.getName();
        boolean admin = isAdmin(authentication);
        AccountReadOnlyDTO account = accountService.getAccountByIban(iban, username, admin);
        List<AccountTransaction> transactions = accountService.getTransactionHistory(iban, username, admin);
        model.addAttribute("account", account);
        model.addAttribute("transactionDTO", new TransactionDTO());
        model.addAttribute("transferDTO", new TransferDTO());
        model.addAttribute("transactions", transactions);
        return "accounts/view";
    }

    @PostMapping("/{iban}/deposit")
    public String deposit(@PathVariable String iban,
                          @Valid @ModelAttribute("transactionDTO") TransactionDTO dto,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String username = authentication.getName();
            boolean admin = isAdmin(authentication);
            model.addAttribute("account", accountService.getAccountByIban(iban, username, admin));
            model.addAttribute("transferDTO", new TransferDTO());
            model.addAttribute("transactions", accountService.getTransactionHistory(iban, username, admin));
            return "accounts/view";
        }

        accountService.deposit(iban, dto.getAmount(), authentication.getName(), isAdmin(authentication));
        redirectAttributes.addFlashAttribute("successMessage", "Η κατάθεση ολοκληρώθηκε επιτυχώς!");
        return "redirect:/accounts/" + iban;
    }

    @PostMapping("/{iban}/transfer")
    public String transfer(@PathVariable String iban,
                           @Valid @ModelAttribute("transferDTO") TransferDTO dto,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String username = authentication.getName();
            boolean admin = isAdmin(authentication);
            model.addAttribute("account", accountService.getAccountByIban(iban, username, admin));
            model.addAttribute("transactionDTO", new TransactionDTO());
            model.addAttribute("transactions", accountService.getTransactionHistory(iban, username, admin));
            return "accounts/view";
        }

        accountService.transfer(iban, dto.getToIban(), dto.getAmount(), authentication.getName(), isAdmin(authentication));
        redirectAttributes.addFlashAttribute("successMessage", "Η μεταφορά ολοκληρώθηκε επιτυχώς!");
        return "redirect:/accounts/" + iban;
    }

    @PostMapping("/{iban}/withdraw")
    public String withdraw(@PathVariable String iban,
                           @Valid @ModelAttribute("transactionDTO") TransactionDTO dto,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String username = authentication.getName();
            boolean admin = isAdmin(authentication);
            model.addAttribute("account", accountService.getAccountByIban(iban, username, admin));
            model.addAttribute("transferDTO", new TransferDTO());
            model.addAttribute("transactions", accountService.getTransactionHistory(iban, username, admin));
            return "accounts/view";
        }

        accountService.withdraw(iban, dto.getAmount(), authentication.getName(), isAdmin(authentication));
        redirectAttributes.addFlashAttribute("successMessage", "Η ανάληψη ολοκληρώθηκε επιτυχώς!");
        return "redirect:/accounts/" + iban;
    }

    @PostMapping("/{iban}/delete")
    public String deleteAccount(@PathVariable String iban, Authentication authentication, RedirectAttributes redirectAttributes) {
        accountService.deleteAccount(iban, authentication.getName(), isAdmin(authentication));
        redirectAttributes.addFlashAttribute("successMessage", "Ο λογαριασμός διαγράφηκε επιτυχώς!");
        return "redirect:/accounts";
    }
}
