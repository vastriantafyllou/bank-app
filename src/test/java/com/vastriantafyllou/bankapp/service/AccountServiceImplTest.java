package com.vastriantafyllou.bankapp.service;

import com.vastriantafyllou.bankapp.core.enums.TransactionType;
import com.vastriantafyllou.bankapp.core.exception.*;
import com.vastriantafyllou.bankapp.dto.AccountInsertDTO;
import com.vastriantafyllou.bankapp.dto.AccountReadOnlyDTO;
import com.vastriantafyllou.bankapp.model.Account;
import com.vastriantafyllou.bankapp.model.AccountTransaction;
import com.vastriantafyllou.bankapp.model.AppUser;
import com.vastriantafyllou.bankapp.repository.AccountRepository;
import com.vastriantafyllou.bankapp.repository.AccountTransactionRepository;
import com.vastriantafyllou.bankapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AppUser testUser;
    private AppUser otherUser;
    private Account testAccount;

    private static final String TEST_IBAN = "GR1234567890123456789012345";
    private static final String TEST_IBAN_2 = "GR9876543210987654321098765";
    private static final String TEST_ACCOUNT_NUMBER = "12345678901234567890";
    private static final String TEST_USERNAME = "testuser";
    private static final String OTHER_USERNAME = "otheruser";

    @BeforeEach
    void setUp() {
        testUser = AppUser.builder().id(1L).username(TEST_USERNAME).password("encoded").build();
        otherUser = AppUser.builder().id(2L).username(OTHER_USERNAME).password("encoded").build();
        testAccount = Account.builder()
                .id(1L)
                .iban(TEST_IBAN)
                .accountNumber(TEST_ACCOUNT_NUMBER)
                .balance(new BigDecimal("1000.00"))
                .owner(testUser)
                .build();
    }

    @Nested
    @DisplayName("createAccount")
    class CreateAccountTests {

        @Test
        @DisplayName("should create account successfully")
        void createAccount_success() {
            AccountInsertDTO dto = new AccountInsertDTO(TEST_IBAN, TEST_ACCOUNT_NUMBER, new BigDecimal("500.00"));
            when(accountRepository.existsByIban(TEST_IBAN)).thenReturn(false);
            when(accountRepository.existsByAccountNumber(TEST_ACCOUNT_NUMBER)).thenReturn(false);
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
            when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
                Account saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            AccountReadOnlyDTO result = accountService.createAccount(dto, TEST_USERNAME);

            assertThat(result).isNotNull();
            assertThat(result.getIban()).isEqualTo(TEST_IBAN);
            assertThat(result.getAccountNumber()).isEqualTo(TEST_ACCOUNT_NUMBER);
            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("should throw when IBAN already exists")
        void createAccount_ibanExists() {
            AccountInsertDTO dto = new AccountInsertDTO(TEST_IBAN, TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
            when(accountRepository.existsByIban(TEST_IBAN)).thenReturn(true);

            assertThatThrownBy(() -> accountService.createAccount(dto, TEST_USERNAME))
                    .isInstanceOf(AccountAlreadyExistsException.class);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when Account Number already exists")
        void createAccount_accountNumberExists() {
            AccountInsertDTO dto = new AccountInsertDTO(TEST_IBAN, TEST_ACCOUNT_NUMBER, BigDecimal.ZERO);
            when(accountRepository.existsByIban(TEST_IBAN)).thenReturn(false);
            when(accountRepository.existsByAccountNumber(TEST_ACCOUNT_NUMBER)).thenReturn(true);

            assertThatThrownBy(() -> accountService.createAccount(dto, TEST_USERNAME))
                    .isInstanceOf(AccountNumberAlreadyExistsException.class);

            verify(accountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deposit")
    class DepositTests {

        @Test
        @DisplayName("should deposit successfully as user")
        void deposit_success() {
            BigDecimal amount = new BigDecimal("200.00");
            when(accountRepository.findByIbanForUpdateAndOwnerUsername(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

            accountService.deposit(TEST_IBAN, amount, TEST_USERNAME, false);

            assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1200.00"));
            verify(accountRepository).save(testAccount);

            ArgumentCaptor<AccountTransaction> txCaptor = ArgumentCaptor.forClass(AccountTransaction.class);
            verify(accountTransactionRepository).save(txCaptor.capture());
            AccountTransaction tx = txCaptor.getValue();
            assertThat(tx.getType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(tx.getAmount()).isEqualByComparingTo(amount);
            assertThat(tx.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("1200.00"));
        }

        @Test
        @DisplayName("should deposit successfully as admin")
        void deposit_asAdmin() {
            BigDecimal amount = new BigDecimal("100.00");
            when(accountRepository.findByIbanForUpdate(TEST_IBAN))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

            accountService.deposit(TEST_IBAN, amount, TEST_USERNAME, true);

            assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1100.00"));
            verify(accountRepository).findByIbanForUpdate(TEST_IBAN);
        }

        @Test
        @DisplayName("should throw on negative amount")
        void deposit_negativeAmount() {
            assertThatThrownBy(() -> accountService.deposit(TEST_IBAN, new BigDecimal("-50.00"), TEST_USERNAME, false))
                    .isInstanceOf(NegativeAmountException.class);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw on zero amount")
        void deposit_zeroAmount() {
            assertThatThrownBy(() -> accountService.deposit(TEST_IBAN, BigDecimal.ZERO, TEST_USERNAME, false))
                    .isInstanceOf(NegativeAmountException.class);
        }

        @Test
        @DisplayName("should throw when account not found")
        void deposit_accountNotFound() {
            when(accountRepository.findByIbanForUpdateAndOwnerUsername(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.deposit(TEST_IBAN, new BigDecimal("100.00"), TEST_USERNAME, false))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("withdraw")
    class WithdrawTests {

        @Test
        @DisplayName("should withdraw successfully")
        void withdraw_success() {
            BigDecimal amount = new BigDecimal("300.00");
            when(accountRepository.findByIbanForUpdateAndOwnerUsername(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

            accountService.withdraw(TEST_IBAN, amount, TEST_USERNAME, false);

            assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));

            ArgumentCaptor<AccountTransaction> txCaptor = ArgumentCaptor.forClass(AccountTransaction.class);
            verify(accountTransactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getType()).isEqualTo(TransactionType.WITHDRAW);
        }

        @Test
        @DisplayName("should throw on negative amount")
        void withdraw_negativeAmount() {
            assertThatThrownBy(() -> accountService.withdraw(TEST_IBAN, new BigDecimal("-10.00"), TEST_USERNAME, false))
                    .isInstanceOf(NegativeAmountException.class);
        }

        @Test
        @DisplayName("should throw on insufficient balance")
        void withdraw_insufficientBalance() {
            when(accountRepository.findByIbanForUpdateAndOwnerUsername(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.of(testAccount));

            assertThatThrownBy(() -> accountService.withdraw(TEST_IBAN, new BigDecimal("2000.00"), TEST_USERNAME, false))
                    .isInstanceOf(InsufficientBalanceException.class);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when account not found")
        void withdraw_accountNotFound() {
            when(accountRepository.findByIbanForUpdateAndOwnerUsername(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.withdraw(TEST_IBAN, new BigDecimal("100.00"), TEST_USERNAME, false))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        @DisplayName("should withdraw exact balance successfully")
        void withdraw_exactBalance() {
            when(accountRepository.findByIbanForUpdateAndOwnerUsername(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

            accountService.withdraw(TEST_IBAN, new BigDecimal("1000.00"), TEST_USERNAME, false);

            assertThat(testAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("transfer")
    class TransferTests {

        private Account toAccount;

        @BeforeEach
        void setUp() {
            toAccount = Account.builder()
                    .id(2L)
                    .iban(TEST_IBAN_2)
                    .accountNumber("98765432109876543210")
                    .balance(new BigDecimal("500.00"))
                    .owner(testUser)
                    .build();
        }

        @Test
        @DisplayName("should transfer successfully between own accounts")
        void transfer_success() {
            BigDecimal amount = new BigDecimal("200.00");

            String firstIban = TEST_IBAN.compareTo(TEST_IBAN_2) < 0 ? TEST_IBAN : TEST_IBAN_2;
            String secondIban = TEST_IBAN.compareTo(TEST_IBAN_2) < 0 ? TEST_IBAN_2 : TEST_IBAN;
            Account firstAccount = firstIban.equals(TEST_IBAN) ? testAccount : toAccount;
            Account secondAccount = secondIban.equals(TEST_IBAN_2) ? toAccount : testAccount;

            when(accountRepository.findByIbanForUpdateAndOwnerUsername(firstIban, TEST_USERNAME))
                    .thenReturn(Optional.of(firstAccount));
            when(accountRepository.findByIbanForUpdateAndOwnerUsername(secondIban, TEST_USERNAME))
                    .thenReturn(Optional.of(secondAccount));

            accountService.transfer(TEST_IBAN, TEST_IBAN_2, amount, TEST_USERNAME, false);

            assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
            assertThat(toAccount.getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));

            verify(accountTransactionRepository, times(2)).save(any(AccountTransaction.class));
        }

        @Test
        @DisplayName("should throw on negative amount")
        void transfer_negativeAmount() {
            assertThatThrownBy(() -> accountService.transfer(TEST_IBAN, TEST_IBAN_2, new BigDecimal("-50.00"), TEST_USERNAME, false))
                    .isInstanceOf(NegativeAmountException.class);
        }

        @Test
        @DisplayName("should throw on same IBAN")
        void transfer_sameIban() {
            assertThatThrownBy(() -> accountService.transfer(TEST_IBAN, TEST_IBAN, new BigDecimal("100.00"), TEST_USERNAME, false))
                    .isInstanceOf(InvalidTransferException.class);
        }

        @Test
        @DisplayName("should throw on insufficient balance")
        void transfer_insufficientBalance() {
            BigDecimal amount = new BigDecimal("5000.00");

            String firstIban = TEST_IBAN.compareTo(TEST_IBAN_2) < 0 ? TEST_IBAN : TEST_IBAN_2;
            String secondIban = TEST_IBAN.compareTo(TEST_IBAN_2) < 0 ? TEST_IBAN_2 : TEST_IBAN;
            Account firstAccount = firstIban.equals(TEST_IBAN) ? testAccount : toAccount;
            Account secondAccount = secondIban.equals(TEST_IBAN_2) ? toAccount : testAccount;

            when(accountRepository.findByIbanForUpdateAndOwnerUsername(firstIban, TEST_USERNAME))
                    .thenReturn(Optional.of(firstAccount));
            when(accountRepository.findByIbanForUpdateAndOwnerUsername(secondIban, TEST_USERNAME))
                    .thenReturn(Optional.of(secondAccount));

            assertThatThrownBy(() -> accountService.transfer(TEST_IBAN, TEST_IBAN_2, amount, TEST_USERNAME, false))
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @Test
        @DisplayName("should throw when transferring to different owner's account")
        void transfer_differentOwner() {
            toAccount.setOwner(otherUser);

            String firstIban = TEST_IBAN.compareTo(TEST_IBAN_2) < 0 ? TEST_IBAN : TEST_IBAN_2;
            String secondIban = TEST_IBAN.compareTo(TEST_IBAN_2) < 0 ? TEST_IBAN_2 : TEST_IBAN;
            Account firstAccount = firstIban.equals(TEST_IBAN) ? testAccount : toAccount;
            Account secondAccount = secondIban.equals(TEST_IBAN_2) ? toAccount : testAccount;

            when(accountRepository.findByIbanForUpdateAndOwnerUsername(firstIban, TEST_USERNAME))
                    .thenReturn(Optional.of(firstAccount));
            when(accountRepository.findByIbanForUpdateAndOwnerUsername(secondIban, TEST_USERNAME))
                    .thenReturn(Optional.of(secondAccount));

            assertThatThrownBy(() -> accountService.transfer(TEST_IBAN, TEST_IBAN_2, new BigDecimal("100.00"), TEST_USERNAME, false))
                    .isInstanceOf(InvalidTransferException.class);
        }

        @Test
        @DisplayName("should throw when source account not found")
        void transfer_sourceNotFound() {
            String firstIban = TEST_IBAN.compareTo(TEST_IBAN_2) < 0 ? TEST_IBAN : TEST_IBAN_2;

            when(accountRepository.findByIbanForUpdateAndOwnerUsername(firstIban, TEST_USERNAME))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.transfer(TEST_IBAN, TEST_IBAN_2, new BigDecimal("100.00"), TEST_USERNAME, false))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getBalance")
    class GetBalanceTests {

        @Test
        @DisplayName("should return balance for user's account")
        void getBalance_success() {
            when(accountRepository.findByIbanAndOwner_Username(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.of(testAccount));

            BigDecimal balance = accountService.getBalance(TEST_IBAN, TEST_USERNAME, false);

            assertThat(balance).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("should return balance as admin")
        void getBalance_asAdmin() {
            when(accountRepository.findByIban(TEST_IBAN)).thenReturn(Optional.of(testAccount));

            BigDecimal balance = accountService.getBalance(TEST_IBAN, TEST_USERNAME, true);

            assertThat(balance).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("should throw when account not found")
        void getBalance_notFound() {
            when(accountRepository.findByIbanAndOwner_Username(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getBalance(TEST_IBAN, TEST_USERNAME, false))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccountsTests {

        @Test
        @DisplayName("should return user's accounts")
        void getAllAccounts_asUser() {
            when(accountRepository.findByOwner_Username(TEST_USERNAME))
                    .thenReturn(List.of(testAccount));

            List<AccountReadOnlyDTO> result = accountService.getAllAccounts(TEST_USERNAME, false);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIban()).isEqualTo(TEST_IBAN);
        }

        @Test
        @DisplayName("should return all accounts as admin")
        void getAllAccounts_asAdmin() {
            Account otherAccount = Account.builder().id(2L).iban(TEST_IBAN_2)
                    .accountNumber("98765432109876543210").balance(BigDecimal.TEN).owner(otherUser).build();
            when(accountRepository.findAll()).thenReturn(List.of(testAccount, otherAccount));

            List<AccountReadOnlyDTO> result = accountService.getAllAccounts(TEST_USERNAME, true);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getAccountByIban")
    class GetAccountByIbanTests {

        @Test
        @DisplayName("should return account for user")
        void getAccountByIban_success() {
            when(accountRepository.findByIbanAndOwner_Username(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.of(testAccount));

            AccountReadOnlyDTO result = accountService.getAccountByIban(TEST_IBAN, TEST_USERNAME, false);

            assertThat(result.getIban()).isEqualTo(TEST_IBAN);
            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("should throw when account not found")
        void getAccountByIban_notFound() {
            when(accountRepository.findByIbanAndOwner_Username(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccountByIban(TEST_IBAN, TEST_USERNAME, false))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getTransactionHistory")
    class GetTransactionHistoryTests {

        @Test
        @DisplayName("should return transactions for existing account")
        void getTransactionHistory_success() {
            when(accountRepository.existsByIbanAndOwner_Username(TEST_IBAN, TEST_USERNAME)).thenReturn(true);
            AccountTransaction tx = AccountTransaction.builder()
                    .id(1L).account(testAccount).type(TransactionType.DEPOSIT)
                    .amount(new BigDecimal("100.00")).balanceAfter(new BigDecimal("1100.00")).build();
            when(accountTransactionRepository.findByAccount_IbanOrderByCreatedAtDesc(TEST_IBAN))
                    .thenReturn(List.of(tx));

            List<AccountTransaction> result = accountService.getTransactionHistory(TEST_IBAN, TEST_USERNAME, false);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(TransactionType.DEPOSIT);
        }

        @Test
        @DisplayName("should throw when account not found")
        void getTransactionHistory_notFound() {
            when(accountRepository.existsByIbanAndOwner_Username(TEST_IBAN, TEST_USERNAME)).thenReturn(false);

            assertThatThrownBy(() -> accountService.getTransactionHistory(TEST_IBAN, TEST_USERNAME, false))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccountTests {

        @Test
        @DisplayName("should delete account successfully")
        void deleteAccount_success() {
            when(accountRepository.findByIbanAndOwner_Username(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.of(testAccount));

            accountService.deleteAccount(TEST_IBAN, TEST_USERNAME, false);

            verify(accountTransactionRepository).deleteByAccount_Iban(TEST_IBAN);
            verify(accountRepository).delete(testAccount);
        }

        @Test
        @DisplayName("should delete as admin")
        void deleteAccount_asAdmin() {
            when(accountRepository.findByIban(TEST_IBAN)).thenReturn(Optional.of(testAccount));

            accountService.deleteAccount(TEST_IBAN, TEST_USERNAME, true);

            verify(accountTransactionRepository).deleteByAccount_Iban(TEST_IBAN);
            verify(accountRepository).delete(testAccount);
        }

        @Test
        @DisplayName("should throw when account not found")
        void deleteAccount_notFound() {
            when(accountRepository.findByIbanAndOwner_Username(TEST_IBAN, TEST_USERNAME))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.deleteAccount(TEST_IBAN, TEST_USERNAME, false))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(accountRepository, never()).delete(any());
        }
    }
}
