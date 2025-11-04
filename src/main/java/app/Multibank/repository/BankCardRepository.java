package app.Multibank.repository;

import app.Multibank.model.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    List<BankCard> findByBankAccountId(Long bankAccountId);
    List<BankCard> findByBankAccountIdAndActive(Long bankAccountId, Boolean active);
    Optional<BankCard> findByBankAccountIdAndCardId(Long bankAccountId, String cardId);

    @Query("SELECT bc FROM BankCard bc WHERE bc.bankAccount.bankConnection.user.id = :userId AND bc.active = true")
    List<BankCard> findActiveByUserId(@Param("userId") Long userId);
}