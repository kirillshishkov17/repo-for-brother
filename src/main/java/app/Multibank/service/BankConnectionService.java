package app.Multibank.service;

import app.Multibank.model.BankConnection;
import app.Multibank.model.User;
import app.Multibank.repository.BankConnectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BankConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(BankConnectionService.class);

    @Autowired
    private BankConnectionRepository bankConnectionRepository;

    @Autowired
    private UserService userService;

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД
    public BankConnection saveConnection(BankConnection connection) {
        return bankConnectionRepository.save(connection);
    }

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД
    public void activateConnection(User user, String bankId) {
        bankConnectionRepository.findByUserAndBankId(user, bankId)
                .ifPresent(connection -> {
                    connection.setActive(true);
                    bankConnectionRepository.save(connection);
                    logger.info("Банковское подключение активировано: {} для пользователя {}", bankId, user.getUsername());
                });
    }

    public BankConnection saveOrUpdateConnection(OAuth2AuthenticationToken authentication,
                                                 OAuth2AuthorizedClient authorizedClient) {
        String providerId = authentication.getAuthorizedClientRegistrationId();
        String username = authentication.getName();

        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Пользователь не найден: " + username);
        }

        Optional<BankConnection> existingConnection =
                bankConnectionRepository.findByUserAndBankId(user, providerId);

        BankConnection connection = existingConnection.orElse(new BankConnection());

        // Обновляем данные подключения
        connection.setUser(user);
        connection.setBankId(providerId);
        connection.setBankName(getBankName(providerId));
        connection.setAccessToken(authorizedClient.getAccessToken().getTokenValue());

        if (authorizedClient.getRefreshToken() != null) {
            connection.setRefreshToken(authorizedClient.getRefreshToken().getTokenValue());
        }

        // Устанавливаем время истечения токена
        if (authorizedClient.getAccessToken().getExpiresAt() != null) {
            connection.setTokenExpiry(LocalDateTime.ofInstant(
                    authorizedClient.getAccessToken().getExpiresAt(),
                    java.time.ZoneId.systemDefault()
            ));
        }

        connection.setActive(true);

        return bankConnectionRepository.save(connection);
    }

    public List<BankConnection> getUserConnections(User user) {
        return bankConnectionRepository.findByUserAndActive(user, true);
    }

    public Optional<BankConnection> getConnection(User user, String bankId) {
        return bankConnectionRepository.findByUserAndBankId(user, bankId);
    }

    public void disconnectBank(User user, String bankId) {
        bankConnectionRepository.findByUserAndBankId(user, bankId)
                .ifPresent(connection -> {
                    connection.setActive(false);
                    bankConnectionRepository.save(connection);
                    logger.info("Банковское подключение отключено: {} для пользователя {}", bankId, user.getUsername());
                });
    }

    public void deleteConnection(User user, String bankId) {
        bankConnectionRepository.findByUserAndBankId(user, bankId)
                .ifPresent(connection -> {
                    bankConnectionRepository.delete(connection);
                    logger.info("Банковское подключение удалено: {} для пользователя {}", bankId, user.getUsername());
                });
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