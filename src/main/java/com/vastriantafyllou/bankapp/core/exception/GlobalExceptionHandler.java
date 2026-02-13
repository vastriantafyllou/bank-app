package com.vastriantafyllou.bankapp.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public String handleAccountNotFound(AccountNotFoundException e, HttpServletRequest request) {
        addFlashError(request, e.getMessage());
        return "redirect:/accounts";
    }

    @ExceptionHandler({NegativeAmountException.class, InsufficientBalanceException.class, InvalidTransferException.class})
    public String handleTransactionError(RuntimeException e, HttpServletRequest request) {
        addFlashError(request, e.getMessage());
        String iban = extractIbanFromUri(request.getRequestURI());
        if (iban != null) {
            return "redirect:/accounts/" + iban;
        }
        return "redirect:/accounts";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {
        addFlashError(request, e.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(WrongPasswordException.class)
    public String handleWrongPassword(WrongPasswordException e, HttpServletRequest request) {
        addFlashError(request, e.getMessage());
        return "redirect:/profile";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericError(Exception e, HttpServletRequest request) {
        addFlashError(request, "Παρουσιάστηκε ένα απρόσμενο σφάλμα.");
        return "redirect:/";
    }

    private void addFlashError(HttpServletRequest request, String message) {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("errorMessage", message);
        FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
        if (flashMapManager != null) {
            flashMapManager.saveOutputFlashMap(flashMap, request, null);
        }
    }

    private String extractIbanFromUri(String uri) {
        // URI pattern: /accounts/{iban}/deposit|withdraw|transfer
        String[] parts = uri.split("/");
        if (parts.length >= 3 && "accounts".equals(parts[1])) {
            return parts[2];
        }
        return null;
    }
}
