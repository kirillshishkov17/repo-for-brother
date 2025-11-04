package app.Multibank.service;

import app.Multibank.model.BankAccount;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankAccountService {

    public List<BankAccount> getUserAccounts(Long userId) {
        // Временная заглушка - возвращаем пустой список
        return List.of();
    }

    public BankAccount getAccountById(Long accountId) {
        return null;
    }

    public Double getTotalBalance(Long userId) {
        return 0.0;
    }

    public double calculateTotalBalance(List<BankAccount> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return 0.0;
        }
        return accounts.stream()
                .mapToDouble(account -> account.getBalance() != null ? account.getBalance().doubleValue() : 0.0)
                .sum();
    }
}