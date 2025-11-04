package app.Multibank.controller;

import app.Multibank.service.BankingApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ApiModeController {

    @Autowired
    private BankingApiService bankingApiService;

    /**
     * Страница управления режимом API
     */
    @GetMapping("/admin/api-mode")
    public String showApiModePage() {
        return "api-mode";
    }

    /**
     * Переключение между реальным и mock режимом
     */
    @PostMapping("/admin/toggle-api-mode")
    public String toggleApiMode(@RequestParam boolean useRealApi,
                                RedirectAttributes redirectAttributes) {
        bankingApiService.setMockEnabled(!useRealApi);

        if (useRealApi) {
            redirectAttributes.addFlashAttribute("success", "✅ Режим переключен на РЕАЛЬНЫЙ Banking API");
        } else {
            redirectAttributes.addFlashAttribute("success", "✅ Режим переключен на MOCK данные");
        }

        return "redirect:/admin/api-mode";
    }

    /**
     * API endpoint для проверки статуса
     */
    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Object> getApiStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("mock_enabled", bankingApiService.isMockEnabled());
        status.put("timestamp", java.time.LocalDateTime.now().toString());
        status.put("message", bankingApiService.isMockEnabled() ?
                "Приложение работает в MOCK режиме" : "Приложение работает с РЕАЛЬНЫМ Banking API");
        return status;
    }

    /**
     * Тестовый endpoint для проверки API
     */
    @GetMapping("/api/test")
    @ResponseBody
    public Map<String, Object> testApi() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> accounts = bankingApiService.getAccounts().block();
            result.put("success", true);
            result.put("mode", bankingApiService.isMockEnabled() ? "MOCK" : "REAL");
            result.put("accounts_count", ((Object[])accounts.get("accounts")).length);
            result.put("source", accounts.get("source"));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}