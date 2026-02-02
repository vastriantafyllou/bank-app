package com.vastriantafyllou.bankapp.repository;

import com.vastriantafyllou.bankapp.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIban(String iban);
    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByIban(String iban);
    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByOwner_Username(String username);
    Optional<Account> findByIbanAndOwner_Username(String iban, String username);
    boolean existsByIbanAndOwner_Username(String iban, String username);

    List<Account> findAllByOwnerIsNull();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.iban = :iban")
    Optional<Account> findByIbanForUpdate(@Param("iban") String iban);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.iban = :iban and a.owner.username = :username")
    Optional<Account> findByIbanForUpdateAndOwnerUsername(@Param("iban") String iban, @Param("username") String username);
}
