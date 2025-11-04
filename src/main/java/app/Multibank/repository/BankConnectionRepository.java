package app.Multibank.repository;

import app.Multibank.model.BankConnection;
import app.Multibank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankConnectionRepository extends JpaRepository<BankConnection, Long> {

    // Базовые методы
    List<BankConnection> findByUser(User user);
    List<BankConnection> findByUserAndActive(User user, Boolean active);
    Optional<BankConnection> findByUserAndBankId(User user, String bankId);
    boolean existsByUserAndBankId(User user, String bankId);

    // Активные подключения
    List<BankConnection> findByActiveTrue();

    // Используем @Query для более сложных запросов
    @Query("SELECT bc FROM BankConnection bc WHERE bc.user = :user AND bc.active = true ORDER BY bc.connectedAt DESC")
    List<BankConnection> findActiveByUser(@Param("user") User user);

    @Query("SELECT bc FROM BankConnection bc WHERE bc.user = :user AND bc.active = true AND bc.bankId = :bankId")
    Optional<BankConnection> findActiveByUserAndBankId(@Param("user") User user, @Param("bankId") String bankId);

    // Подсчет активных подключений
    @Query("SELECT COUNT(bc) FROM BankConnection bc WHERE bc.user = :user AND bc.active = true")
    long countActiveConnectionsByUser(@Param("user") User user);
}