package ru.yandex.practicum.exception;

/** Файл найден, но прочитать его не удалось. */
public final class DictionaryReadException extends DictionaryException {
    public DictionaryReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
