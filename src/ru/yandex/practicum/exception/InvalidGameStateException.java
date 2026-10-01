package ru.yandex.practicum.exception;

/** Внутреннее состояние игры нарушает её обязательные правила. */
public final class InvalidGameStateException extends RuntimeException {
    public InvalidGameStateException(String message) {
        super(message);
    }
}
