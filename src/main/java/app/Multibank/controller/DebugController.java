package app.Multibank.controller;

import app.Multibank.model.User;
import app.Multibank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DebugController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/debug/users")
    public String showUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("encoder", passwordEncoder);
        return "debug-users";
    }
}