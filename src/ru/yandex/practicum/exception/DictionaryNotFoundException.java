package ru.yandex.practicum.exception;

/** Файл словаря не найден или путь к нему не указан. */
public final class DictionaryNotFoundException extends DictionaryException {
    public DictionaryNotFoundException(String message) {
        super(message);
    }

    public DictionaryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
