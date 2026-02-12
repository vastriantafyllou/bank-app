package com.vastriantafyllou.bankapp.service;

import com.vastriantafyllou.bankapp.dto.AccountInsertDTO;
import com.vastriantafyllou.bankapp.dto.AccountReadOnlyDTO;
import com.vastriantafyllou.bankapp.model.AccountTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface IAccountService {
    AccountReadOnlyDTO createAccount(AccountInsertDTO dto, String username);
    void deposit(String iban, BigDecimal amount, String username, boolean isAdmin);
    void withdraw(String iban, BigDecimal amount, String username, boolean isAdmin);
    void transfer(String fromIban, String toIban, BigDecimal amount, String username, boolean isAdmin);
    BigDecimal getBalance(String iban, String username, boolean isAdmin);
    List<AccountReadOnlyDTO> getAllAccounts(String username, boolean isAdmin);
    AccountReadOnlyDTO getAccountByIban(String iban, String username, boolean isAdmin);
    List<AccountTransaction> getTransactionHistory(String iban, String username, boolean isAdmin);
    void deleteAccount(String iban, String username, boolean isAdmin);
}
