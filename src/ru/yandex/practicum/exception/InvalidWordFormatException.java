package ru.yandex.practicum.exception;

/** Введённое значение содержит недопустимые символы. */
public final class InvalidWordFormatException extends GameException {
    public InvalidWordFormatException(String message) {
        super(message);
    }
}
