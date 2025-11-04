package app.Multibank.controller;

import app.Multibank.model.BankAccount;
import app.Multibank.model.Transaction;
import app.Multibank.service.BankAccountService;
import app.Multibank.service.BankDataAggregationService;
import app.Multibank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FinancialDataController {

    @Autowired
    private BankDataAggregationService bankDataAggregationService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            // Временные тестовые данные
            Map<String, Object> financialSummary = new HashMap<>();
            financialSummary.put("totalBalance", 150000.0);
            financialSummary.put("totalIncome", 50000.0);
            financialSummary.put("totalExpenses", 35000.0);
            financialSummary.put("activeAccounts", 3);
            financialSummary.put("netIncome", 15000.0);

            List<BankAccount> accounts = new ArrayList<>();
            List<Transaction> recentTransactions = new ArrayList<>();

            model.addAttribute("financialSummary", financialSummary);
            model.addAttribute("accounts", accounts);
            model.addAttribute("recentTransactions", recentTransactions);
            model.addAttribute("title", "Дашборд");

            return "dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            // Возвращаем страницу с пустыми данными в случае ошибки
            model.addAttribute("financialSummary", new HashMap<>());
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("recentTransactions", new ArrayList<>());
            model.addAttribute("title", "Дашборд");
            return "dashboard";
        }
    }

    @GetMapping("/accounts")
    public String showAccounts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            List<BankAccount> accounts = new ArrayList<>();
            double totalBalance = 0.0;

            model.addAttribute("accounts", accounts);
            model.addAttribute("totalBalance", totalBalance);
            model.addAttribute("title", "Мои счета");

            return "accounts";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("totalBalance", 0.0);
            model.addAttribute("title", "Мои счета");
            return "accounts";
        }
    }

    @GetMapping("/transactions")
    public String showTransactions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            List<Transaction> transactions = new ArrayList<>();
            double totalIncome = 0.0;
            double totalExpenses = 0.0;

            model.addAttribute("transactions", transactions);
            model.addAttribute("totalIncome", totalIncome);
            model.addAttribute("totalExpenses", totalExpenses);
            model.addAttribute("title", "Транзакции");

            return "transactions";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("transactions", new ArrayList<>());
            model.addAttribute("totalIncome", 0.0);
            model.addAttribute("totalExpenses", 0.0);
            model.addAttribute("title", "Транзакции");
            return "transactions";
        }
    }

    @PostMapping("/sync-data")
    public String syncData(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("success", "Данные успешно синхронизированы");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при синхронизации данных: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/account-details/{accountId}")
    public String showAccountDetails(@PathVariable Long accountId, Model model) {
        try {
            BankAccount account = bankAccountService.getAccountById(accountId);
            List<Transaction> transactions = new ArrayList<>();

            if (account == null) {
                return "redirect:/accounts";
            }

            model.addAttribute("account", account);
            model.addAttribute("transactions", transactions);
            model.addAttribute("title", "Детали счета - " + account.getAccountName());

            return "account-details";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/accounts";
        }
    }
}