package app.Multibank.service;

import app.Multibank.model.BankConnection;
import app.Multibank.model.User;
import app.Multibank.repository.BankConnectionRepository;
import app.Multibank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class OAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankConnectionRepository bankConnectionRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauthUser = oauthToken.getPrincipal();

            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            String email = oauthUser.getAttribute("email");
            String name = oauthUser.getAttribute("name");

            // Создаем final переменные для использования в лямбде
            final String finalEmail;
            final String finalName;

            // Если email не пришел, создаем из имени
            if (email == null) {
                if (name != null) {
                    finalEmail = name.toLowerCase().replace(" ", ".") + "@oauth.com";
                    finalName = name;
                } else {
                    finalEmail = "user@" + registrationId + ".com";
                    finalName = "User";
                }
            } else {
                finalEmail = email;
                finalName = name != null ? name : "User";
            }

            // Создаем или находим пользователя
            User user = userRepository.findByEmail(finalEmail)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(finalEmail);
                        newUser.setEmail(finalEmail);
                        newUser.setPassword("OAUTH2_USER");
                        return userRepository.save(newUser);
                    });

            // Создаем или обновляем банковское подключение
            BankConnection connection = bankConnectionRepository.findByUserAndBankId(user, registrationId)
                    .orElse(new BankConnection());

            connection.setUser(user);
            connection.setBankId(registrationId);
            connection.setBankName(getBankName(registrationId));
            connection.setConnectedAt(LocalDateTime.now());
            connection.setActive(true);

            // Для демо-режима создаем mock токен
            connection.setAccessToken("mock_token_" + System.currentTimeMillis());
            connection.setRefreshToken("mock_refresh_token_" + System.currentTimeMillis());

            bankConnectionRepository.save(connection);

            System.out.println("OAuth2 успешный вход: " + finalEmail + " через " + registrationId);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String getBankName(String bankId) {
        return switch (bankId) {
            case "abank" -> "ABank";
            case "vbank" -> "VBank";
            case "sbank" -> "SBank";
            default -> bankId;
        };
    }
}