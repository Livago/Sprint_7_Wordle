package ru.yandex.practicum.exception;

/** После фильтрации в словаре не осталось игровых слов. */
public final class EmptyDictionaryException extends DictionaryException {
    public EmptyDictionaryException(String message) {
        super(message);
    }
}
