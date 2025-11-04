package app.Multibank.service;

import app.Multibank.model.*;
import app.Multibank.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class BankDataAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(BankDataAggregationService.class);

    @Autowired
    private BankConnectionRepository bankConnectionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankCardRepository bankCardRepository;

    @Autowired
    private BankingApiService bankingApiService; // ЗАМЕНИЛИ BankApiService на BankingApiService

    @Autowired
    private UserService userService;

    /**
     * Синхронизация всех данных для пользователя
     */
    public void syncAllUserData(Long userId) {
        logger.info("Синхронизация данных для пользователя: {}", userId);

        // Используем существующего пользователя вместо создания нового
        User user = userService.findById(userId);
        if (user == null) {
            logger.error("Пользователь не найден: {}", userId);
            return;
        }

        List<BankConnection> connections = bankConnectionRepository.findByUserAndActive(user, true);

        for (BankConnection connection : connections) {
            try {
                syncBankConnectionData(connection);
            } catch (Exception e) {
                logger.error("Ошибка синхронизации для подключения {}: {}", connection.getBankName(), e.getMessage());
            }
        }

        logger.info("Синхронизация данных завершена для пользователя: {}", userId);
    }

    /**
     * Синхронизация данных для конкретного банковского подключения
     */
    public void syncBankConnectionData(BankConnection connection) {
        logger.info("Синхронизация данных для подключения: {}", connection.getBankName());

        try {
            // Получаем счета из реального Banking API
            Map<String, Object> accountsResponse = bankingApiService.getAccounts().block();
            List<BankAccount> accounts = adaptApiAccounts(connection, accountsResponse);
            syncAccounts(connection, accounts);

            // Для каждого счета получаем транзакции и карты
            for (BankAccount account : accounts) {
                if (account.isActive()) {
                    syncTransactions(account);
                    syncCards(account);
                }
            }

            // Обновляем время последней синхронизации
            connection.setLastSyncAt(LocalDateTime.now());
            bankConnectionRepository.save(connection);

        } catch (Exception e) {
            logger.error("Ошибка синхронизации данных для {}: {}", connection.getBankName(), e.getMessage());
            throw new RuntimeException("Ошибка синхронизации данных", e);
        }
    }

    /**
     * Адаптация счетов из Banking API
     */
    private List<BankAccount> adaptApiAccounts(BankConnection connection, Map<String, Object> apiResponse) {
        List<BankAccount> accounts = new ArrayList<>();

        if (apiResponse != null && apiResponse.containsKey("accounts")) {
            List<Map<String, Object>> apiAccounts = (List<Map<String, Object>>) apiResponse.get("accounts");

            for (Map<String, Object> apiAccount : apiAccounts) {
                BankAccount account = new BankAccount();
                account.setBankConnection(connection);
                account.setAccountId((String) apiAccount.get("id"));
                account.setAccountNumber((String) apiAccount.get("number"));
                account.setAccountName((String) apiAccount.get("name"));
                account.setAccountType((String) apiAccount.get("type"));
                account.setCurrency((String) apiAccount.get("currency"));

                // Преобразование баланса
                Object balanceObj = apiAccount.get("balance");
                if (balanceObj != null) {
                    if (balanceObj instanceof Number) {
                        account.setBalance(BigDecimal.valueOf(((Number) balanceObj).doubleValue()));
                    } else if (balanceObj instanceof String) {
                        account.setBalance(new BigDecimal((String) balanceObj));
                    }
                }

                account.setActive(true);
                account.setLastSyncAt(LocalDateTime.now());
                accounts.add(account);
            }
        }

        return accounts;
    }

    /**
     * Синхронизация счетов
     */
    private void syncAccounts(BankConnection connection, List<BankAccount> apiAccounts) {
        for (BankAccount apiAccount : apiAccounts) {
            BankAccount existingAccount = bankAccountRepository
                    .findByBankConnectionAndAccountId(connection, apiAccount.getAccountId())
                    .orElse(null);

            if (existingAccount == null) {
                // Новый счет
                apiAccount.setBankConnection(connection);
                apiAccount.setLastSyncAt(LocalDateTime.now());
                bankAccountRepository.save(apiAccount);
                logger.info("Добавлен новый счет: {}", apiAccount.getAccountName());
            } else {
                // Обновление существующего счета
                existingAccount.setBalance(apiAccount.getBalance());
                existingAccount.setAvailableBalance(apiAccount.getAvailableBalance());
                existingAccount.setAccountName(apiAccount.getAccountName());
                existingAccount.setAccountType(apiAccount.getAccountType());
                existingAccount.setCurrency(apiAccount.getCurrency());
                existingAccount.setLastSyncAt(LocalDateTime.now());
                bankAccountRepository.save(existingAccount);
            }
        }
    }

    /**
     * Синхронизация транзакций
     */
    private void syncTransactions(BankAccount account) {
        LocalDateTime lastSyncDate = account.getLastSyncAt();
        if (lastSyncDate == null) {
            // Если первая синхронизация, берем транзакции за последние 30 дней
            lastSyncDate = LocalDateTime.now().minusDays(30);
        }

        // Получаем транзакции из Banking API
        String fromDate = lastSyncDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String toDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        try {
            Map<String, Object> transactionsResponse = bankingApiService
                    .getAccountTransactions(account.getAccountId(), fromDate, toDate)
                    .block();

            List<Transaction> transactions = adaptApiTransactions(account, transactionsResponse);

            for (Transaction transaction : transactions) {
                if (!transactionRepository.findByBankAccountIdAndTransactionId(account.getId(), transaction.getTransactionId()).isPresent()) {
                    transaction.setBankAccount(account);
                    transactionRepository.save(transaction);
                }
            }

            logger.info("Синхронизировано {} транзакций для счета: {}", transactions.size(), account.getAccountName());
        } catch (Exception e) {
            logger.error("Ошибка синхронизации транзакций для счета {}: {}", account.getAccountName(), e.getMessage());
        }
    }

    /**
     * Адаптация транзакций из Banking API
     */
    private List<Transaction> adaptApiTransactions(BankAccount account, Map<String, Object> apiResponse) {
        List<Transaction> transactions = new ArrayList<>();

        if (apiResponse != null && apiResponse.containsKey("transactions")) {
            List<Map<String, Object>> apiTransactions = (List<Map<String, Object>>) apiResponse.get("transactions");

            for (Map<String, Object> apiTransaction : apiTransactions) {
                Transaction transaction = new Transaction();
                transaction.setBankAccount(account);
                transaction.setTransactionId((String) apiTransaction.get("id"));

                // Преобразование суммы
                Object amountObj = apiTransaction.get("amount");
                if (amountObj != null) {
                    if (amountObj instanceof Number) {
                        transaction.setAmount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
                    } else if (amountObj instanceof String) {
                        transaction.setAmount(new BigDecimal((String) amountObj));
                    }
                }

                transaction.setCurrency((String) apiTransaction.get("currency"));
                transaction.setDescription((String) apiTransaction.get("description"));
                transaction.setMerchantName((String) apiTransaction.get("merchantName"));
                transaction.setCategory((String) apiTransaction.get("category"));

                // Преобразование дат
                Object dateObj = apiTransaction.get("transactionDate");
                if (dateObj instanceof String) {
                    transaction.setTransactionDate(LocalDateTime.parse((String) dateObj));
                }

                transaction.setTransactionType((String) apiTransaction.get("transactionType"));
                transaction.setStatus((String) apiTransaction.get("status"));

                transactions.add(transaction);
            }
        }

        return transactions;
    }

    /**
     * Синхронизация карт
     */
    private void syncCards(BankAccount account) {
        try {
            Map<String, Object> cardsResponse = bankingApiService.getCards().block();
            List<BankCard> cards = adaptApiCards(account, cardsResponse);

            for (BankCard card : cards) {
                BankCard existingCard = bankCardRepository
                        .findByBankAccountIdAndCardId(account.getId(), card.getCardId())
                        .orElse(null);

                if (existingCard == null) {
                    card.setBankAccount(account);
                    card.setLastSyncAt(LocalDateTime.now());
                    bankCardRepository.save(card);
                } else {
                    existingCard.setActive(card.getActive());
                    existingCard.setExpiryDate(card.getExpiryDate());
                    existingCard.setCardHolderName(card.getCardHolderName());
                    existingCard.setLastSyncAt(LocalDateTime.now());
                    bankCardRepository.save(existingCard);
                }
            }

            logger.info("Синхронизировано {} карт для счета: {}", cards.size(), account.getAccountName());
        } catch (Exception e) {
            logger.error("Ошибка синхронизации карт для счета {}: {}", account.getAccountName(), e.getMessage());
        }
    }

    /**
     * Адаптация карт из Banking API
     */
    private List<BankCard> adaptApiCards(BankAccount account, Map<String, Object> apiResponse) {
        List<BankCard> cards = new ArrayList<>();

        if (apiResponse != null && apiResponse.containsKey("cards")) {
            List<Map<String, Object>> apiCards = (List<Map<String, Object>>) apiResponse.get("cards");

            for (Map<String, Object> apiCard : apiCards) {
                BankCard card = new BankCard();
                card.setBankAccount(account);
                card.setCardId((String) apiCard.get("id"));
                card.setMaskedCardNumber((String) apiCard.get("maskedNumber"));
                card.setCardHolderName((String) apiCard.get("cardHolderName"));
                card.setCardType((String) apiCard.get("cardType"));
                card.setPaymentSystem((String) apiCard.get("paymentSystem"));
                card.setActive(true);
                card.setLastSyncAt(LocalDateTime.now());

                cards.add(card);
            }
        }

        return cards;
    }

    /**
     * Получение агрегированной статистики по пользователю
     */
    public Map<String, Object> getUserFinancialSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();

        // Общий баланс
        Double totalBalance = bankAccountRepository.getTotalBalanceByUserId(userId);
        summary.put("totalBalance", totalBalance != null ? totalBalance : 0.0);

        // Баланс по валютам
        List<BankAccount> accounts = bankAccountRepository.findActiveByUserId(userId);
        Map<String, Double> balanceByCurrency = new HashMap<>();
        for (BankAccount account : accounts) {
            String currency = account.getCurrency();
            Double balance = balanceByCurrency.getOrDefault(currency, 0.0);
            balance += account.getBalance() != null ? account.getBalance().doubleValue() : 0.0;
            balanceByCurrency.put(currency, balance);
        }
        summary.put("balanceByCurrency", balanceByCurrency);

        // Статистика по транзакциям за последние 30 дней
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);

        Double totalExpenses = transactionRepository.getTotalExpensesByUserIdAndDateRange(userId, startDate, endDate);
        Double totalIncome = transactionRepository.getTotalIncomeByUserIdAndDateRange(userId, startDate, endDate);

        summary.put("totalExpenses", totalExpenses != null ? totalExpenses : 0.0);
        summary.put("totalIncome", totalIncome != null ? totalIncome : 0.0);
        summary.put("netIncome", (totalIncome != null ? totalIncome : 0.0) - (totalExpenses != null ? totalExpenses : 0.0));

        // Количество активных счетов и карт
        long activeAccounts = accounts.stream().filter(BankAccount::isActive).count();
        long activeCards = bankCardRepository.findActiveByUserId(userId).size();

        summary.put("activeAccounts", activeAccounts);
        summary.put("activeCards", activeCards);

        return summary;
    }

    /**
     * Автоматическая синхронизация данных каждые 30 минут
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 минут
    public void scheduledSync() {
        logger.info("Запуск плановой синхронизации данных через Banking API");

        // Здесь можно добавить логику для синхронизации активных пользователей
        // Например: получить всех активных пользователей и синхронизировать их данные
        // Пока что это заглушка
        logger.info("Плановая синхронизация завершена");
    }

    /**
     * Синхронизация по требованию для конкретного пользователя
     */
    public void syncUserDataOnDemand(Long userId) {
        logger.info("Синхронизация по требованию для пользователя: {}", userId);
        syncAllUserData(userId);
    }
}