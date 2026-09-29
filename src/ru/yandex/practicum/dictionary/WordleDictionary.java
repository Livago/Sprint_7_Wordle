package ru.yandex.practicum.dictionary;

import ru.yandex.practicum.exception.EmptyDictionaryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

/** Хранит подготовленные слова и выполняет операции над ними. */
public final class WordleDictionary {
    public static final int WORD_LENGTH = 5;

    private static final Pattern RUSSIAN_WORD_PATTERN =
            Pattern.compile("[а-я]{5}");

    // List нужен для выбора слова по случайному номеру.
    private final List<String> words;

    // Set нужен для быстрой проверки наличия слова.
    private final Set<String> wordSet;

    public WordleDictionary(List<String> sourceWords)
            throws EmptyDictionaryException {
        if (sourceWords == null) {
            throw new IllegalArgumentException("Список слов не должен быть null");
        }

        Set<String> preparedWords = prepareWords(sourceWords);
        if (preparedWords.isEmpty()) {
            throw new EmptyDictionaryException(
                    "В словаре нет подходящих слов из пяти букв");
        }

        // Неизменяемые коллекции защищают словарь от случайной порчи извне.
        words = Collections.unmodifiableList(new ArrayList<>(preparedWords));
        wordSet = Collections.unmodifiableSet(preparedWords);
    }

    private Set<String> prepareWords(List<String> sourceWords) {
        // LinkedHashSet одновременно убирает повторы и сохраняет порядок слов.
        Set<String> preparedWords = new LinkedHashSet<>();

        for (String sourceWord : sourceWords) {
            if (sourceWord == null) {
                continue;
            }

            String normalizedWord = normalize(sourceWord.trim());
            if (isValidFormat(normalizedWord)) {
                preparedWords.add(normalizedWord);
            }
        }

        return preparedWords;
    }

    /** Приводит слово к единому виду, используемому во всей игре. */
    public static String normalize(String word) {
        if (word == null) {
            return null;
        }

        return word.toLowerCase(Locale.ROOT).replace('ё', 'е');
    }

    /** Проверяет, что строка состоит ровно из пяти русских букв. */
    public static boolean isValidFormat(String word) {
        if (word == null) {
            return false;
        }

        return RUSSIAN_WORD_PATTERN.matcher(word).matches();
    }

    public boolean contains(String word) {
        if (word == null) {
            return false;
        }

        String normalizedWord = normalize(word);
        return wordSet.contains(normalizedWord);
    }

    public int size() {
        return words.size();
    }

    public List<String> getWords() {
        return words;
    }

    public String getRandomWord() {
        return getRandomWord(new Random());
    }

    public String getRandomWord(Random random) {
        if (random == null) {
            throw new IllegalArgumentException(
                    "Генератор случайных чисел не должен быть null");
        }

        int randomIndex = random.nextInt(words.size());
        return words.get(randomIndex);
    }

    /**
     * Сравнивает слово игрока с ответом.
     *
     * Сначала отмечаются точные совпадения знаком '+'. Затем среди ещё не
     * использованных букв ответа ищутся буквы не на своём месте для знака '^'.
     * Такой порядок нужен для правильной обработки повторяющихся букв.
     */
    public static String compare(String guess, String answer) {
        String normalizedGuess = normalize(guess);
        String normalizedAnswer = normalize(answer);

        checkWordsBeforeComparison(normalizedGuess, normalizedAnswer);

        char[] feedback = {'-', '-', '-', '-', '-'};
        boolean[] answerLetterWasUsed = new boolean[WORD_LENGTH];

        markExactMatches(
                normalizedGuess,
                normalizedAnswer,
                feedback,
                answerLetterWasUsed);
        markMisplacedLetters(
                normalizedGuess,
                normalizedAnswer,
                feedback,
                answerLetterWasUsed);

        return new String(feedback);
    }

    private static void markExactMatches(
            String guess,
            String answer,
            char[] feedback,
            boolean[] answerLetterWasUsed
    ) {
        for (int position = 0; position < WORD_LENGTH; position++) {
            boolean lettersAreEqual =
                    guess.charAt(position) == answer.charAt(position);

            if (lettersAreEqual) {
                feedback[position] = '+';
                answerLetterWasUsed[position] = true;
            }
        }
    }

    private static void markMisplacedLetters(
            String guess,
            String answer,
            char[] feedback,
            boolean[] answerLetterWasUsed
    ) {
        for (int guessPosition = 0;
             guessPosition < WORD_LENGTH;
             guessPosition++) {

            // Буква уже получила знак '+', повторно искать её не нужно.
            if (feedback[guessPosition] == '+') {
                continue;
            }

            int answerPosition = findUnusedLetter(
                    guess.charAt(guessPosition),
                    answer,
                    answerLetterWasUsed);

            if (answerPosition >= 0) {
                feedback[guessPosition] = '^';
                answerLetterWasUsed[answerPosition] = true;
            }
        }
    }

    /** Возвращает позицию буквы или -1, если свободной такой буквы нет. */
    private static int findUnusedLetter(
            char letter,
            String answer,
            boolean[] answerLetterWasUsed
    ) {
        for (int position = 0; position < WORD_LENGTH; position++) {
            boolean positionIsFree = !answerLetterWasUsed[position];
            boolean letterMatches = answer.charAt(position) == letter;

            if (positionIsFree && letterMatches) {
                return position;
            }
        }

        return -1;
    }

    private static void checkWordsBeforeComparison(
            String guess,
            String answer
    ) {
        if (!isValidFormat(guess) || !isValidFormat(answer)) {
            throw new IllegalArgumentException(
                    "Оба слова должны состоять из пяти русских букв");
        }
    }

    public String compareWords(String guess, String answer) {
        return compare(guess, answer);
    }

    /** Возвращает слова, которые могли бы дать указанный результат проверки. */
    public List<String> compatibleWords(String guess, String feedback) {
        if (feedback == null || !feedback.matches("[+\\^-]{5}")) {
            throw new IllegalArgumentException("Некорректный результат проверки");
        }

        List<String> compatibleWords = new ArrayList<>();
        for (String candidate : words) {
            String candidateFeedback = compare(guess, candidate);
            if (feedback.equals(candidateFeedback)) {
                compatibleWords.add(candidate);
            }
        }

        return compatibleWords;
    }
}
