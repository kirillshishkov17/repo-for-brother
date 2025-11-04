package app.Multibank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Добро пожаловать в Multibank!");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "О нас");
        model.addAttribute("content", "Multibank - это современная платформа для управления вашими банковскими счетами.");
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Контакты");
        model.addAttribute("email", "support@multibank.ru");
        return "contact";
    }

    // Демо-страницы для просмотра всех HTML
    @GetMapping("/demo")
    public String demoPages(Model model) {
        model.addAttribute("title", "Демо-страницы");
        return "demo";
    }
}