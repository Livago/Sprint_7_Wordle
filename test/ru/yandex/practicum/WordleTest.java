package ru.yandex.practicum;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.dictionary.WordleDictionary;
import ru.yandex.practicum.dictionary.WordleDictionaryLoader;
import ru.yandex.practicum.exception.DictionaryNotFoundException;
import ru.yandex.practicum.exception.EmptyDictionaryException;
import ru.yandex.practicum.exception.InvalidWordLengthException;
import ru.yandex.practicum.game.GuessResult;
import ru.yandex.practicum.game.WordleGame;

class WordleTest {
    private WordleDictionary dictionary() throws EmptyDictionaryException {
        return new WordleDictionary(Arrays.asList("гонец", "герой", "масса", "касса", "слово", "ЁЖИКИ"));
    }

    @Test
    @DisplayName("Нормализовать слова и отфильтровать словарь")
    void normalizesAndFiltersDictionary() throws Exception {
        WordleDictionary dictionary = dictionary();
        assertTrue(dictionary.contains("ёжики"));
        assertTrue(dictionary.contains("ЕЖИКИ"));
        assertEquals("ежики", WordleDictionary.normalize("ЁЖИКИ"));
    }

    @Test
    @DisplayName("Сравнить введённое слово с правильным ответом")
    void comparesWords() {
        assertEquals("+++++", WordleDictionary.compare("герой", "герой"));
        assertEquals("+^-^-", WordleDictionary.compare("гонец", "герой"));
        assertEquals("-++++", WordleDictionary.compare("касса", "масса"));
    }

    @Test
    @DisplayName("Не тратить попытку при некорректном ходе")
    void invalidMoveDoesNotSpendAttempt() throws Exception {
        WordleGame game = new WordleGame(dictionary(), "герой");
        assertThrows(InvalidWordLengthException.class, () -> game.makeMove("кот"));
        assertEquals(6, game.getAttemptsLeft());
    }

    @Test
    @DisplayName("Перевести игру в состояние победы после правильного ответа")
    void winChangesState() throws Exception {
        WordleGame game = new WordleGame(dictionary(), "герой");
        GuessResult result = game.makeMove("ГЕРОЙ");
        assertEquals("+++++", result.getFeedback());
        assertTrue(game.isWon());
        assertEquals(5, game.getAttemptsLeft());
    }

    @Test
    @DisplayName("Завершить игру с помощью совместимых автоматических подсказок")
    void hintsAreCompatibleAndCanFinishGame() throws Exception {
        WordleGame game = new WordleGame(dictionary(), "герой", new Random(1), null);
        while (!game.isFinished()) {
            String hint = game.getHint();
            assertFalse(game.getGuesses().contains(hint));
            game.makeMove(hint);
            assertTrue(game.getCandidateWords().contains("герой"));
        }
        assertTrue(game.isWon());
    }

    @Test
    @DisplayName("Загрузить словарь в UTF-8, нормализовать и отфильтровать слова")
    void loaderReadsUtf8NormalizesAndFilters() throws Exception {
        Path file = Files.createTempFile("wordle-dictionary", ".txt");
        try {
            Files.write(file, Arrays.asList("КОТ", "ЁЖИКИ", "герой", "english"), StandardCharsets.UTF_8);
            WordleDictionary loaded = new WordleDictionaryLoader().load(file);
            assertEquals(2, loaded.size());
            assertTrue(loaded.contains("ежики"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    @DisplayName("Отклонить пустой или отсутствующий файл словаря")
    void loaderRejectsEmptyAndMissingDictionary() throws Exception {
        Path empty = Files.createTempFile("wordle-empty", ".txt");
        try {
            assertThrows(EmptyDictionaryException.class, () -> new WordleDictionaryLoader().load(empty));
            assertThrows(DictionaryNotFoundException.class,
                    () -> new WordleDictionaryLoader().load(empty.resolveSibling("definitely-missing-wordle.txt")));
        } finally {
            Files.deleteIfExists(empty);
        }
    }

    @Test
    @DisplayName("Перевести игру в состояние поражения после шестого неверного хода")
    void sixthValidWrongMoveLosesGame() throws Exception {
        WordleGame game = new WordleGame(dictionary(), "герой");
        for (int i = 0; i < 6; i++) game.makeMove("гонец");
        assertEquals(WordleGame.GameState.LOST, game.getState());
        assertEquals(0, game.getAttemptsLeft());
    }
}
