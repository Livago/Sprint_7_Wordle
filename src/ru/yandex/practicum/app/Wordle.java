package ru.yandex.practicum.app;

import ru.yandex.practicum.dictionary.WordleDictionary;
import ru.yandex.practicum.dictionary.WordleDictionaryLoader;
import ru.yandex.practicum.exception.DictionaryException;
import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.game.GuessResult;
import ru.yandex.practicum.game.WordleGame;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Главный класс приложения.
 *
 * Только этот класс общается с игроком через консоль. Вся игровая логика
 * находится в WordleGame, а работа с файлом словаря — в WordleDictionaryLoader.
 */
public final class Wordle {
    private static final String DEFAULT_DICTIONARY = "words_ru.txt";
    private static final String LOG_FILE = "wordle.log";

    private Wordle() {
        // У приложения нет объектов Wordle: запуск начинается с метода main.
    }

    public static void main(String[] args) {
        String dictionaryFile = DEFAULT_DICTIONARY;
        if (args.length > 0) {
            dictionaryFile = args[0];
        }

        // Лог автоматически закроется после завершения блока try.
        try (PrintWriter log = new PrintWriter(
                new FileWriter(LOG_FILE, StandardCharsets.UTF_8))) {
            run(dictionaryFile, log);
        } catch (IOException exception) {
            System.err.println("Не удалось создать технический лог. Игра не запущена.");
        }
    }

    static void run(String dictionaryFile, PrintWriter log) {
        try {
            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary dictionary = loader.load(dictionaryFile);
            WordleGame game = new WordleGame(dictionary, log);

            play(game, log);
        } catch (DictionaryException exception) {
            logError(log, "Не удалось запустить игру", exception);
            System.out.println("Не удалось загрузить игровой словарь. Подробности записаны в лог.");
        } catch (Exception exception) {
            logError(log, "Непредвиденная ошибка приложения", exception);
            System.out.println("Произошла системная ошибка. Подробности записаны в лог.");
        }
    }

    private static void play(WordleGame game, PrintWriter log) {
        printRules(game.getAttemptsLeft());

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            while (!game.isFinished() && scanner.hasNextLine()) {
                playOneTurn(game, scanner);
            }

            if (game.isFinished()) {
                printFinalResult(game);
            } else {
                log.println("Поток ввода закрылся до завершения игры");
                log.flush();
                System.out.println("Ввод завершён. Игра прервана.");
            }
        } catch (RuntimeException exception) {
            logError(log, "Критическая ошибка игры", exception);
            System.out.println("Игра остановлена из-за системной ошибки. Подробности записаны в лог.");
        }
    }

    /** Проводит один ход. Ошибочный ввод не завершает игру и не тратит попытку. */
    private static void playOneTurn(WordleGame game, Scanner scanner) {
        System.out.print("\nВаш ход: ");
        String input = scanner.nextLine();

        try {
            String word = input;

            // Пустая строка означает просьбу компьютеру подобрать слово.
            if (input.isEmpty()) {
                word = game.getHint();
                System.out.println("Подсказка: " + word);
            }

            GuessResult result = game.makeMove(word);
            System.out.println(result.getFeedback());

            if (!game.isFinished()) {
                System.out.println(formatAttemptsLeft(result.getAttemptsLeft()));
            }
        } catch (GameException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void printRules(int attempts) {
        System.out.println("Wordle: угадайте русское слово из пяти букв.");
        System.out.println("Нажмите Enter без слова, чтобы получить автоматическую подсказку.");
        System.out.println();
        System.out.println("У вас " + attempts + " попыток:");
    }

    private static void printFinalResult(WordleGame game) {
        if (game.isWon()) {
            System.out.println("Победа!");
        } else {
            System.out.println("Попытки закончились.");
        }
        System.out.println("Загаданное слово: " + game.getAnswer());
    }

    private static String formatAttemptsLeft(int attempts) {
        if (attempts == 1) {
            return "Осталась 1 попытка";
        }
        if (attempts >= 2 && attempts <= 4) {
            return "Осталось " + attempts + " попытки";
        }
        return "Осталось " + attempts + " попыток";
    }

    private static void logError(PrintWriter log, String message, Throwable error) {
        log.println(message + ": " + error.getMessage());
        error.printStackTrace(log);
        log.flush();
    }
}
