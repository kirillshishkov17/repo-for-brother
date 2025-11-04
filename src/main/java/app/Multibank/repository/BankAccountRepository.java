package app.Multibank.repository;

import app.Multibank.model.BankAccount;
import app.Multibank.model.BankConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByBankConnection(BankConnection bankConnection);
    List<BankAccount> findByBankConnectionAndActive(BankConnection bankConnection, Boolean active);
    Optional<BankAccount> findByBankConnectionAndAccountId(BankConnection bankConnection, String accountId);
    boolean existsByBankConnectionAndAccountId(BankConnection bankConnection, String accountId);

    @Query("SELECT ba FROM BankAccount ba WHERE ba.bankConnection.user.id = :userId AND ba.active = true")
    List<BankAccount> findActiveByUserId(@Param("userId") Long userId);


    @Query("SELECT SUM(ba.balance) FROM BankAccount ba WHERE ba.bankConnection.user.id = :userId AND ba.active = true")
    Double getTotalBalanceByUserId(@Param("userId") Long userId);
}
