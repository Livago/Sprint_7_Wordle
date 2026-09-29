package ru.yandex.practicum.exception;

/** Ошибка пользовательского хода, после которой игру можно продолжить. */
public class GameException extends Exception {
    public GameException(String message) {
        super(message);
    }
}
