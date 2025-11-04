package app.Multibank.repository;




import app.Multibank.model.BankConnection;
import app.Multibank.model.User;

import java.util.List;

public interface CustomBankConnectionRepository {
    List<BankConnection> findActiveConnectionsByUser(User user);
    List<BankConnection> findExpiredConnections();
}