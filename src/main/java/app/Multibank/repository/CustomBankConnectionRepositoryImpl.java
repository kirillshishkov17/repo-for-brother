package app.Multibank.repository;

import app.Multibank.model.BankConnection;
import app.Multibank.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomBankConnectionRepositoryImpl implements CustomBankConnectionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<BankConnection> findActiveConnectionsByUser(User user) {
        String jpql = "SELECT bc FROM BankConnection bc WHERE bc.user = :user AND bc.active = true";
        TypedQuery<BankConnection> query = entityManager.createQuery(jpql, BankConnection.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    @Override
    public List<BankConnection> findExpiredConnections() {
        String jpql = "SELECT bc FROM BankConnection bc WHERE bc.tokenExpiry < CURRENT_TIMESTAMP AND bc.active = true";
        TypedQuery<BankConnection> query = entityManager.createQuery(jpql, BankConnection.class);
        return query.getResultList();
    }
}