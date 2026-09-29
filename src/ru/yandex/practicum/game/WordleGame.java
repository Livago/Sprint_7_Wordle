package ru.yandex.practicum.game;

import ru.yandex.practicum.dictionary.WordleDictionary;
import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.InvalidGameStateException;
import ru.yandex.practicum.exception.InvalidWordFormatException;
import ru.yandex.practicum.exception.InvalidWordLengthException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Хранит состояние одной партии и выполняет игровые правила.
 * Класс ничего не читает из консоли и ничего в неё не выводит.
 */
public final class WordleGame {
    public static final int MAX_ATTEMPTS = 6;

    public enum GameState {
        IN_PROGRESS,
        WON,
        LOST
    }

    private final WordleDictionary dictionary;
    private final String answer;
    private final Random random;
    private final PrintWriter log;

    // История партии. Элементы guesses и feedbackHistory соответствуют друг другу.
    private final List<String> guesses = new ArrayList<>();
    private final List<String> feedbackHistory = new ArrayList<>();
    private final Set<String> suggestedWords = new HashSet<>();

    // Слова, которые всё ещё могут быть правильным ответом.
    private List<String> candidateWords;
    private int attemptsLeft = MAX_ATTEMPTS;
    private GameState state = GameState.IN_PROGRESS;

    public WordleGame(WordleDictionary dictionary) {
        this(dictionary, new Random(), null);
    }

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this(dictionary, new Random(), log);
    }

    public WordleGame(WordleDictionary dictionary, Random random, PrintWriter log) {
        this(dictionary, chooseRandomAnswer(dictionary, random), random, log);
    }

    /** Этот конструктор позволяет задать ответ заранее, например в тесте. */
    public WordleGame(WordleDictionary dictionary, String answer) {
        this(dictionary, answer, new Random(), null);
    }

    public WordleGame(
            WordleDictionary dictionary,
            String answer,
            Random random,
            PrintWriter log
    ) {
        checkDictionary(dictionary);

        this.dictionary = dictionary;
        this.answer = WordleDictionary.normalize(answer);
        this.random = random == null ? new Random() : random;
        this.log = log;

        if (!dictionary.contains(this.answer)) {
            throw new IllegalArgumentException("Загаданного слова нет в словаре");
        }

        candidateWords = new ArrayList<>(dictionary.getWords());
        writeToLog("Новая игра; ответ=" + this.answer
                + ", кандидатов=" + candidateWords.size());
        checkInvariants();
    }

    /** Выполняет один ход и возвращает его результат. */
    public GuessResult makeMove(String input) throws GameException {
        ensureGameIsActive();

        String word = validateWord(input);
        String feedback = WordleDictionary.compare(word, answer);

        saveMove(word, feedback);
        attemptsLeft--;
        removeUnsuitableCandidates(word, feedback);
        updateGameState(word);

        writeToLog("Ход=" + word
                + ", результат=" + feedback
                + ", осталось попыток=" + attemptsLeft
                + ", кандидатов=" + candidateWords.size()
                + ", состояние=" + state);

        checkInvariants();
        return new GuessResult(word, feedback, attemptsLeft, state);
    }

    /** Выбирает новое слово только из вариантов, совместимых с прошлыми ходами. */
    public String getHint() {
        ensureGameIsActive();
        checkInvariants();

        List<String> unusedCandidates = findUnusedCandidates();
        if (unusedCandidates.isEmpty()) {
            throw new InvalidGameStateException("Нет неиспользованных слов для подсказки");
        }

        String hint;
        if (attemptsLeft == 1) {
            // Так игра только с подсказками всегда закончится победой за шесть ходов.
            hint = answer;
        } else {
            int randomIndex = random.nextInt(unusedCandidates.size());
            hint = unusedCandidates.get(randomIndex);
        }

        suggestedWords.add(hint);
        writeToLog("Подсказка=" + hint + ", кандидатов=" + candidateWords.size());
        return hint;
    }

    private String validateWord(String input) throws GameException {
        if (input == null || input.isEmpty()) {
            throw new InvalidWordFormatException("Введите слово из пяти русских букв.");
        }

        String word = WordleDictionary.normalize(input);

        if (word.length() != WordleDictionary.WORD_LENGTH) {
            throw new InvalidWordLengthException(
                    "Слово должно состоять ровно из пяти букв.");
        }
        if (!WordleDictionary.isValidFormat(word)) {
            throw new InvalidWordFormatException("Допустимы только русские буквы.");
        }
        if (!dictionary.contains(word)) {
            throw new WordNotFoundInDictionaryException(
                    "Слова «" + word + "» нет в словаре.");
        }

        return word;
    }

    private void saveMove(String word, String feedback) {
        guesses.add(word);
        feedbackHistory.add(feedback);
    }

    private void updateGameState(String word) {
        if (word.equals(answer)) {
            state = GameState.WON;
        } else if (attemptsLeft == 0) {
            state = GameState.LOST;
        }
    }

    private void removeUnsuitableCandidates(String guess, String actualFeedback) {
        List<String> suitableCandidates = new ArrayList<>();

        for (String candidate : candidateWords) {
            String candidateFeedback = WordleDictionary.compare(guess, candidate);
            if (actualFeedback.equals(candidateFeedback)) {
                suitableCandidates.add(candidate);
            }
        }

        candidateWords = suitableCandidates;
    }

    private List<String> findUnusedCandidates() {
        List<String> unusedCandidates = new ArrayList<>();

        for (String candidate : candidateWords) {
            if (!suggestedWords.contains(candidate)) {
                unusedCandidates.add(candidate);
            }
        }

        return unusedCandidates;
    }

    private void ensureGameIsActive() {
        if (state != GameState.IN_PROGRESS) {
            throw new InvalidGameStateException("Игра уже завершена");
        }
    }

    /** Проверяет внутренние правила, которые никогда не должны нарушаться. */
    private void checkInvariants() {
        if (attemptsLeft < 0 || attemptsLeft > MAX_ATTEMPTS) {
            throw new InvalidGameStateException(
                    "Недопустимое количество попыток: " + attemptsLeft);
        }
        if (!candidateWords.contains(answer)) {
            throw new InvalidGameStateException(
                    "Среди возможных слов нет правильного ответа");
        }
        if (state == GameState.IN_PROGRESS && attemptsLeft == 0) {
            throw new InvalidGameStateException(
                    "Игра активна, хотя попытки закончились");
        }
    }

    private void writeToLog(String message) {
        if (log != null) {
            log.println(message);
            log.flush();
        }
    }

    private static String chooseRandomAnswer(
            WordleDictionary dictionary,
            Random random
    ) {
        checkDictionary(dictionary);
        Random actualRandom = random == null ? new Random() : random;
        return dictionary.getRandomWord(actualRandom);
    }

    private static void checkDictionary(WordleDictionary dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("Словарь не должен быть null");
        }
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public GameState getState() {
        return state;
    }

    public boolean isFinished() {
        return state != GameState.IN_PROGRESS;
    }

    public boolean isWon() {
        return state == GameState.WON;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getGuesses() {
        return Collections.unmodifiableList(guesses);
    }

    public List<String> getFeedbackHistory() {
        return Collections.unmodifiableList(feedbackHistory);
    }

    public List<String> getCandidateWords() {
        return Collections.unmodifiableList(candidateWords);
    }

    public Set<String> getSuggestedWords() {
        return Collections.unmodifiableSet(suggestedWords);
    }
}
