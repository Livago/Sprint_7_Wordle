package ru.yandex.practicum.exception;

/** Корректно написанного слова нет в игровом словаре. */
public final class WordNotFoundInDictionaryException extends GameException {
    public WordNotFoundInDictionaryException(String message) {
        super(message);
    }
}
