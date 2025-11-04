package app.Multibank.dto;

public record UserRecord(String name) {
    // Автоматически создаются:
    // - Публичный конструктор
    // - Геттер name()
    // - equals(), hashCode(), toString()
}
