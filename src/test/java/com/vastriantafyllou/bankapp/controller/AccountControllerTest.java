package com.vastriantafyllou.bankapp.controller;

import com.vastriantafyllou.bankapp.core.exception.*;
import com.vastriantafyllou.bankapp.dto.AccountInsertDTO;
import com.vastriantafyllou.bankapp.dto.AccountReadOnlyDTO;
import com.vastriantafyllou.bankapp.service.IAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private static final String TEST_IBAN = "GR1234567890123456789012345";
    private static final String TEST_ACCOUNT_NUMBER = "12345678901234567890";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static Authentication userAuth() {
        return new UsernamePasswordAuthenticationToken("testuser", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken("admin", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Nested
    @DisplayName("GET /accounts")
    class ListAccountsTests {

        @Test
        @DisplayName("should return accounts list for authenticated user")
        void listAccounts_authenticated() throws Exception {
            AccountReadOnlyDTO dto = new AccountReadOnlyDTO(1L, TEST_IBAN, TEST_ACCOUNT_NUMBER, new BigDecimal("1000.00"));
            when(accountService.getAllAccounts("testuser", false)).thenReturn(List.of(dto));

            mockMvc.perform(get("/accounts").principal(userAuth()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/list"))
                    .andExpect(model().attributeExists("accounts"));
        }

        @Test
        @DisplayName("admin should see all accounts")
        void listAccounts_admin() throws Exception {
            when(accountService.getAllAccounts("admin", true)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/accounts").principal(adminAuth()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/list"));
        }
    }

    @Nested
    @DisplayName("GET /accounts/new")
    class ShowCreateFormTests {

        @Test
        @DisplayName("should show create form")
        void showCreateForm() throws Exception {
            mockMvc.perform(get("/accounts/new").principal(userAuth()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/create"))
                    .andExpect(model().attributeExists("accountDTO"));
        }
    }

    @Nested
    @DisplayName("POST /accounts/new")
    class CreateAccountTests {

        @Test
        @DisplayName("should create account successfully")
        void createAccount_success() throws Exception {
            AccountReadOnlyDTO result = new AccountReadOnlyDTO(1L, TEST_IBAN, TEST_ACCOUNT_NUMBER, new BigDecimal("500.00"));
            when(accountService.createAccount(any(AccountInsertDTO.class), eq("testuser"))).thenReturn(result);

            mockMvc.perform(post("/accounts/new")
                            .principal(userAuth())
                            .param("iban", TEST_IBAN)
                            .param("accountNumber", TEST_ACCOUNT_NUMBER)
                            .param("balance", "500.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts"))
                    .andExpect(flash().attributeExists("successMessage"));
        }

        @Test
        @DisplayName("should return form with errors on invalid IBAN")
        void createAccount_invalidIban() throws Exception {
            mockMvc.perform(post("/accounts/new")
                            .principal(userAuth())
                            .param("iban", "INVALID")
                            .param("accountNumber", TEST_ACCOUNT_NUMBER)
                            .param("balance", "500.00"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/create"))
                    .andExpect(model().attributeHasFieldErrors("accountDTO", "iban"));
        }

        @Test
        @DisplayName("should return form with errors on blank fields")
        void createAccount_blankFields() throws Exception {
            mockMvc.perform(post("/accounts/new")
                            .principal(userAuth())
                            .param("iban", "")
                            .param("accountNumber", "")
                            .param("balance", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/create"))
                    .andExpect(model().attributeHasFieldErrors("accountDTO", "iban", "accountNumber", "balance"));
        }

        @Test
        @DisplayName("should show error when IBAN already exists")
        void createAccount_ibanExists() throws Exception {
            when(accountService.createAccount(any(AccountInsertDTO.class), eq("testuser")))
                    .thenThrow(new AccountAlreadyExistsException("IBAN exists"));

            mockMvc.perform(post("/accounts/new")
                            .principal(userAuth())
                            .param("iban", TEST_IBAN)
                            .param("accountNumber", TEST_ACCOUNT_NUMBER)
                            .param("balance", "500.00"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/create"))
                    .andExpect(model().attributeHasFieldErrors("accountDTO", "iban"));
        }
    }

    @Nested
    @DisplayName("GET /accounts/{iban}")
    class ViewAccountTests {

        @Test
        @DisplayName("should show account details")
        void viewAccount_success() throws Exception {
            AccountReadOnlyDTO dto = new AccountReadOnlyDTO(1L, TEST_IBAN, TEST_ACCOUNT_NUMBER, new BigDecimal("1000.00"));
            when(accountService.getAccountByIban(TEST_IBAN, "testuser", false)).thenReturn(dto);
            when(accountService.getTransactionHistory(TEST_IBAN, "testuser", false)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/accounts/" + TEST_IBAN).principal(userAuth()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/view"))
                    .andExpect(model().attributeExists("account", "transactionDTO", "transferDTO", "transactions"));
        }

        @Test
        @DisplayName("should redirect when account not found")
        void viewAccount_notFound() throws Exception {
            when(accountService.getAccountByIban(TEST_IBAN, "testuser", false))
                    .thenThrow(new AccountNotFoundException("Not found"));

            mockMvc.perform(get("/accounts/" + TEST_IBAN).principal(userAuth()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts"));
        }
    }

    @Nested
    @DisplayName("POST /accounts/{iban}/deposit")
    class DepositTests {

        @Test
        @DisplayName("should deposit successfully")
        void deposit_success() throws Exception {
            mockMvc.perform(post("/accounts/" + TEST_IBAN + "/deposit")
                            .principal(userAuth())
                            .param("amount", "200.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts/" + TEST_IBAN))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(accountService).deposit(TEST_IBAN, new BigDecimal("200.00"), "testuser", false);
        }

        @Test
        @DisplayName("should redirect with error on negative amount exception")
        void deposit_negativeAmount() throws Exception {
            doThrow(new NegativeAmountException("Negative")).when(accountService)
                    .deposit(eq(TEST_IBAN), any(BigDecimal.class), eq("testuser"), eq(false));

            mockMvc.perform(post("/accounts/" + TEST_IBAN + "/deposit")
                            .principal(userAuth())
                            .param("amount", "100.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts/" + TEST_IBAN));
        }
    }

    @Nested
    @DisplayName("POST /accounts/{iban}/withdraw")
    class WithdrawTests {

        @Test
        @DisplayName("should withdraw successfully")
        void withdraw_success() throws Exception {
            mockMvc.perform(post("/accounts/" + TEST_IBAN + "/withdraw")
                            .principal(userAuth())
                            .param("amount", "300.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts/" + TEST_IBAN))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(accountService).withdraw(TEST_IBAN, new BigDecimal("300.00"), "testuser", false);
        }

        @Test
        @DisplayName("should redirect with error on insufficient balance")
        void withdraw_insufficientBalance() throws Exception {
            doThrow(new InsufficientBalanceException("Insufficient")).when(accountService)
                    .withdraw(eq(TEST_IBAN), any(BigDecimal.class), eq("testuser"), eq(false));

            mockMvc.perform(post("/accounts/" + TEST_IBAN + "/withdraw")
                            .principal(userAuth())
                            .param("amount", "5000.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts/" + TEST_IBAN));
        }
    }

    @Nested
    @DisplayName("POST /accounts/{iban}/transfer")
    class TransferTests {

        private static final String TO_IBAN = "GR9876543210987654321098765";

        @Test
        @DisplayName("should transfer successfully")
        void transfer_success() throws Exception {
            mockMvc.perform(post("/accounts/" + TEST_IBAN + "/transfer")
                            .principal(userAuth())
                            .param("toIban", TO_IBAN)
                            .param("amount", "150.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts/" + TEST_IBAN))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(accountService).transfer(TEST_IBAN, TO_IBAN, new BigDecimal("150.00"), "testuser", false);
        }

        @Test
        @DisplayName("should redirect with error on invalid transfer")
        void transfer_invalidTransfer() throws Exception {
            doThrow(new InvalidTransferException("Same account")).when(accountService)
                    .transfer(eq(TEST_IBAN), eq(TEST_IBAN), any(BigDecimal.class), eq("testuser"), eq(false));

            mockMvc.perform(post("/accounts/" + TEST_IBAN + "/transfer")
                            .principal(userAuth())
                            .param("toIban", TEST_IBAN)
                            .param("amount", "100.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts/" + TEST_IBAN));
        }

        @Test
        @DisplayName("should show validation error when toIban is blank")
        void transfer_blankToIban() throws Exception {
            AccountReadOnlyDTO dto = new AccountReadOnlyDTO(1L, TEST_IBAN, TEST_ACCOUNT_NUMBER, new BigDecimal("1000.00"));
            when(accountService.getAccountByIban(TEST_IBAN, "testuser", false)).thenReturn(dto);
            when(accountService.getTransactionHistory(TEST_IBAN, "testuser", false)).thenReturn(Collections.emptyList());

            mockMvc.perform(post("/accounts/" + TEST_IBAN + "/transfer")
                            .principal(userAuth())
                            .param("toIban", "")
                            .param("amount", "100.00"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/view"));

            verify(accountService, never()).transfer(any(), any(), any(), any(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("POST /accounts/{iban}/delete")
    class DeleteAccountTests {

        @Test
        @DisplayName("should delete account successfully")
        void deleteAccount_success() throws Exception {
            mockMvc.perform(post("/accounts/" + TEST_IBAN + "/delete")
                            .principal(userAuth()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(accountService).deleteAccount(TEST_IBAN, "testuser", false);
        }

        @Test
        @DisplayName("should redirect with error when account not found")
        void deleteAccount_notFound() throws Exception {
            doThrow(new AccountNotFoundException("Not found")).when(accountService)
                    .deleteAccount(TEST_IBAN, "testuser", false);

            mockMvc.perform(post("/accounts/" + TEST_IBAN + "/delete")
                            .principal(userAuth()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts"));
        }
    }
}
