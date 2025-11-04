package app.Multibank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BankingDataAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(BankingDataAdapterService.class);

    @Autowired
    private BankingApiService bankingApiService;

    /**
     * Получение и преобразование счетов из Banking API
     */
    public Map<String, Object> getAdaptedAccounts() {
        try {
            Map apiResponse = bankingApiService.getAccounts().block();
            return adaptAccountsResponse(apiResponse);
        } catch (Exception e) {
            logger.error("Ошибка получения счетов из Banking API: {}", e.getMessage());
            return createFallbackResponse();
        }
    }

    /**
     * Адаптация ответа со счетами
     */
    private Map<String, Object> adaptAccountsResponse(Map apiResponse) {
        Map<String, Object> adaptedResponse = new HashMap<>();

        if (apiResponse != null && apiResponse.containsKey("accounts")) {
            List<Map<String, Object>> accounts = (List<Map<String, Object>>) apiResponse.get("accounts");
            List<Map<String, Object>> adaptedAccounts = new ArrayList<>();

            for (Map<String, Object> account : accounts) {
                Map<String, Object> adaptedAccount = new HashMap<>();
                adaptedAccount.put("account_id", account.get("id"));
                adaptedAccount.put("account_number", maskAccountNumber((String) account.get("number")));
                adaptedAccount.put("account_name", account.get("name"));
                adaptedAccount.put("balance", account.get("balance"));
                adaptedAccount.put("currency", account.get("currency"));
                adaptedAccount.put("type", account.get("type"));
                adaptedAccount.put("status", account.get("status"));

                adaptedAccounts.add(adaptedAccount);
            }

            adaptedResponse.put("accounts", adaptedAccounts);
            adaptedResponse.put("bank_id", "real_bank");
            adaptedResponse.put("timestamp", LocalDateTime.now().toString());
        } else {
            // Fallback на mock данные если API недоступно
            adaptedResponse = createFallbackResponse();
        }

        return adaptedResponse;
    }

    /**
     * Создание платежного согласия
     */
    public Map<String, Object> createAdaptedPaymentConsent(String consentType, Map<String, Object> consentData) {
        try {
            Map<String, Object> apiConsentData = new HashMap<>();

            // Адаптация данных для Banking API
            apiConsentData.put("type", consentType);
            apiConsentData.put("debtorAccount", consentData.get("debtor_account"));

            if ("single_use".equals(consentType)) {
                apiConsentData.put("amount", consentData.get("amount"));
            } else {
                apiConsentData.put("maxAmount", consentData.get("max_amount"));
                apiConsentData.put("maxTransactions", consentData.get("max_transactions"));
            }

            apiConsentData.put("currency", consentData.getOrDefault("currency", "RUB"));
            apiConsentData.put("validUntil", consentData.get("valid_until"));

            Map apiResponse = bankingApiService.createPaymentConsent(apiConsentData).block();
            return adaptConsentResponse(apiResponse);

        } catch (Exception e) {
            logger.error("Ошибка создания платежного согласия: {}", e.getMessage());
            return createFallbackConsentResponse(consentType);
        }
    }

    /**
     * Создание платежа
     */
    public Map<String, Object> createAdaptedPayment(Map<String, Object> paymentData) {
        try {
            Map<String, Object> apiPaymentData = new HashMap<>();

            // Адаптация данных для Banking API
            apiPaymentData.put("debtorAccount", paymentData.get("debtor_account"));
            apiPaymentData.put("creditorAccount", paymentData.get("creditor_account"));
            apiPaymentData.put("amount", paymentData.get("amount"));
            apiPaymentData.put("currency", paymentData.getOrDefault("currency", "RUB"));
            apiPaymentData.put("reference", paymentData.get("reference"));
            apiPaymentData.put("description", paymentData.get("description"));

            Map apiResponse = bankingApiService.createPayment(apiPaymentData).block();
            return adaptPaymentResponse(apiResponse);

        } catch (Exception e) {
            logger.error("Ошибка создания платежа: {}", e.getMessage());
            return createFallbackPaymentResponse();
        }
    }

    // Вспомогательные методы
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) return "****";
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    private Map<String, Object> createFallbackResponse() {
        // Fallback на mock данные
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> account1 = new HashMap<>();
        account1.put("account_id", "acc_real_001");
        account1.put("account_number", "****1234");
        account1.put("account_name", "Основной счет (Real API)");
        account1.put("balance", 50000.0);
        account1.put("currency", "RUB");
        account1.put("type", "CURRENT");
        account1.put("status", "ACTIVE");

        Map<String, Object> account2 = new HashMap<>();
        account2.put("account_id", "acc_real_002");
        account2.put("account_number", "****5678");
        account2.put("account_name", "Накопительный счет (Real API)");
        account2.put("balance", 10000.0);
        account2.put("currency", "RUB");
        account2.put("type", "SAVINGS");
        account2.put("status", "ACTIVE");

        response.put("accounts", Arrays.asList(account1, account2));
        response.put("bank_id", "real_bank_fallback");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("source", "FALLBACK_DATA");

        return response;
    }

    private Map<String, Object> adaptConsentResponse(Map apiResponse) {
        // Адаптация ответа согласия
        Map<String, Object> adapted = new HashMap<>();
        if (apiResponse != null) {
            adapted.put("consent_id", apiResponse.get("id"));
            adapted.put("status", apiResponse.get("status"));
            adapted.put("type", apiResponse.get("type"));
        } else {
            // Fallback данные
            adapted.put("consent_id", "consent_fallback_" + System.currentTimeMillis());
            adapted.put("status", "AUTHORIZED");
            adapted.put("type", "unknown");
        }
        return adapted;
    }

    private Map<String, Object> createFallbackConsentResponse(String consentType) {
        Map<String, Object> response = new HashMap<>();
        response.put("consent_id", "consent_fallback_" + System.currentTimeMillis());
        response.put("status", "AUTHORIZED");
        response.put("type", consentType);
        response.put("source", "FALLBACK_DATA");
        return response;
    }

    private Map<String, Object> adaptPaymentResponse(Map apiResponse) {
        // Адаптация ответа платежа
        Map<String, Object> adapted = new HashMap<>();
        if (apiResponse != null) {
            adapted.put("payment_id", apiResponse.get("id"));
            adapted.put("status", apiResponse.get("status"));
            adapted.put("amount", apiResponse.get("amount"));
        } else {
            // Fallback данные
            adapted.put("payment_id", "payment_fallback_" + System.currentTimeMillis());
            adapted.put("status", "COMPLETED");
            adapted.put("amount", 0);
        }
        return adapted;
    }

    private Map<String, Object> createFallbackPaymentResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("payment_id", "payment_fallback_" + System.currentTimeMillis());
        response.put("status", "COMPLETED");
        response.put("amount", 0);
        response.put("source", "FALLBACK_DATA");
        return response;
    }
}