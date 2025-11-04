package app.Multibank.controller;

import app.Multibank.dto.RegistrationDto;
import app.Multibank.model.User;
import app.Multibank.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") RegistrationDto registrationDto, Model model) {
        try {
            logger.info("Начало регистрации пользователя: {}", registrationDto.getUsername());

            // Проверка совпадения паролей
            if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
                model.addAttribute("error", "Пароли не совпадают");
                return "register";
            }

            // Проверка существования пользователя
            if (userService.usernameExists(registrationDto.getUsername())) {
                model.addAttribute("error", "Имя пользователя уже занято");
                return "register";
            }

            // Создание пользователя
            User user = new User();
            user.setUsername(registrationDto.getUsername());
            user.setEmail(registrationDto.getEmail());
            user.setPassword(registrationDto.getPassword()); // Пароль зашифруется в сервисе

            User savedUser = userService.registerUser(user);
            logger.info("Пользователь успешно зарегистрирован: {}", savedUser.getUsername());

            model.addAttribute("success", "Регистрация прошла успешно! Теперь вы можете войти.");
            return "redirect:/login?success";

        } catch (Exception e) {
            logger.error("Ошибка при регистрации: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }
}