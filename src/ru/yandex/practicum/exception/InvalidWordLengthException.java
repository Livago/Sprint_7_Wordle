package ru.yandex.practicum.exception;

/** Введённое слово состоит не из пяти букв. */
public final class InvalidWordLengthException extends GameException {
    public InvalidWordLengthException(String message) {
        super(message);
    }
}
