package com.vastriantafyllou.bankapp.controller;

import com.vastriantafyllou.bankapp.core.exception.AccountAlreadyExistsException;
import com.vastriantafyllou.bankapp.core.exception.AccountNumberAlreadyExistsException;
import com.vastriantafyllou.bankapp.core.exception.AccountNotFoundException;
import com.vastriantafyllou.bankapp.core.exception.InvalidTransferException;
import com.vastriantafyllou.bankapp.core.exception.InsufficientBalanceException;
import com.vastriantafyllou.bankapp.core.exception.NegativeAmountException;
import com.vastriantafyllou.bankapp.dto.AccountInsertDTO;
import com.vastriantafyllou.bankapp.dto.AccountReadOnlyDTO;
import com.vastriantafyllou.bankapp.dto.TransferDTO;
import com.vastriantafyllou.bankapp.dto.TransactionDTO;
import com.vastriantafyllou.bankapp.model.AccountTransaction;
import com.vastriantafyllou.bankapp.service.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public String listAccounts(Model model) {
        List<AccountReadOnlyDTO> accounts = accountService.getAllAccounts();
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
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "accounts/create";
        }

        try {
            accountService.createAccount(dto);
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
    public String viewAccount(@PathVariable String iban, Model model, RedirectAttributes redirectAttributes) {
        try {
            AccountReadOnlyDTO account = accountService.getAccountByIban(iban);
            List<AccountTransaction> transactions = accountService.getTransactionHistory(iban);
            model.addAttribute("account", account);
            model.addAttribute("transactionDTO", new TransactionDTO());
            model.addAttribute("transferDTO", new TransferDTO());
            model.addAttribute("transactions", transactions);
            return "accounts/view";
        } catch (AccountNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts";
        }
    }

    @PostMapping("/{iban}/deposit")
    public String deposit(@PathVariable String iban,
                          @Valid @ModelAttribute("transactionDTO") TransactionDTO dto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            try {
                model.addAttribute("account", accountService.getAccountByIban(iban));
                model.addAttribute("transferDTO", new TransferDTO());
                model.addAttribute("transactions", accountService.getTransactionHistory(iban));
            } catch (AccountNotFoundException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/accounts";
            }
            return "accounts/view";
        }

        try {
            accountService.deposit(iban, dto.getAmount());
            redirectAttributes.addFlashAttribute("successMessage", "Η κατάθεση ολοκληρώθηκε επιτυχώς!");
        } catch (AccountNotFoundException | NegativeAmountException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/accounts/" + iban;
    }

    @PostMapping("/{iban}/transfer")
    public String transfer(@PathVariable String iban,
                           @Valid @ModelAttribute("transferDTO") TransferDTO dto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            try {
                model.addAttribute("account", accountService.getAccountByIban(iban));
                model.addAttribute("transactionDTO", new TransactionDTO());
                model.addAttribute("transactions", accountService.getTransactionHistory(iban));
            } catch (AccountNotFoundException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/accounts";
            }
            return "accounts/view";
        }

        try {
            accountService.transfer(iban, dto.getToIban(), dto.getAmount());
            redirectAttributes.addFlashAttribute("successMessage", "Η μεταφορά ολοκληρώθηκε επιτυχώς!");
        } catch (AccountNotFoundException | NegativeAmountException | InsufficientBalanceException | InvalidTransferException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/accounts/" + iban;
    }

    @PostMapping("/{iban}/withdraw")
    public String withdraw(@PathVariable String iban,
                           @Valid @ModelAttribute("transactionDTO") TransactionDTO dto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            try {
                model.addAttribute("account", accountService.getAccountByIban(iban));
                model.addAttribute("transferDTO", new TransferDTO());
                model.addAttribute("transactions", accountService.getTransactionHistory(iban));
            } catch (AccountNotFoundException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/accounts";
            }
            return "accounts/view";
        }

        try {
            accountService.withdraw(iban, dto.getAmount());
            redirectAttributes.addFlashAttribute("successMessage", "Η ανάληψη ολοκληρώθηκε επιτυχώς!");
        } catch (AccountNotFoundException | NegativeAmountException | InsufficientBalanceException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/accounts/" + iban;
    }

    @PostMapping("/{iban}/delete")
    public String deleteAccount(@PathVariable String iban, RedirectAttributes redirectAttributes) {
        try {
            accountService.deleteAccount(iban);
            redirectAttributes.addFlashAttribute("successMessage", "Ο λογαριασμός διαγράφηκε επιτυχώς!");
        } catch (AccountNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/accounts";
    }
}
