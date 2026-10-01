package ru.yandex.practicum.exception;

/** Общий тип для всех ошибок загрузки словаря. */
public class DictionaryException extends Exception {
    public DictionaryException(String message) {
        super(message);
    }

    public DictionaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
