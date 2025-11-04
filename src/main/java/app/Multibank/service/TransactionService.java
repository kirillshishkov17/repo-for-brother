package app.Multibank.service;

import app.Multibank.model.Transaction;
import app.Multibank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findByBankAccountIdOrderByTransactionDateDesc(accountId);
    }

    public List<Transaction> getRecentTransactions(Long userId, int limit) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .limit(limit)
                .toList();
    }
}