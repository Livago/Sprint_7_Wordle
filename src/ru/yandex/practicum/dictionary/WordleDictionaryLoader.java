package ru.yandex.practicum.dictionary;

import ru.yandex.practicum.exception.DictionaryException;
import ru.yandex.practicum.exception.DictionaryNotFoundException;
import ru.yandex.practicum.exception.DictionaryReadException;
import ru.yandex.practicum.exception.EmptyDictionaryException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Читает исходные строки из файла и создаёт игровой словарь. */
public final class WordleDictionaryLoader {
    private final PrintWriter log;

    public WordleDictionaryLoader() {
        this(null);
    }

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary load(String fileName) throws DictionaryException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new DictionaryNotFoundException("Путь к словарю не указан");
        }

        Path path = Path.of(fileName);
        return load(path);
    }

    public WordleDictionary load(Path path) throws DictionaryException {
        if (path == null) {
            throw new DictionaryNotFoundException("Путь к словарю не указан");
        }

        List<String> fileLines = readAllLines(path);

        try {
            WordleDictionary dictionary = new WordleDictionary(fileLines);
            writeToLog("Загружено слов: " + dictionary.size(), null);
            return dictionary;
        } catch (EmptyDictionaryException exception) {
            writeToLog(exception.getMessage(), exception);
            throw exception;
        }
    }

    private List<String> readAllLines(Path path) throws DictionaryException {
        List<String> lines = new ArrayList<>();

        // Оба файловых ресурса автоматически закроются после чтения.
        try (FileReader fileReader = new FileReader(
                    path.toFile(), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fileReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (FileNotFoundException exception) {
            String message = "Файл словаря не найден: " + path;
            writeToLog(message, exception);
            throw new DictionaryNotFoundException(message, exception);
        } catch (IOException exception) {
            String message = "Не удалось прочитать словарь: " + path;
            writeToLog(message, exception);
            throw new DictionaryReadException(message, exception);
        }

        return lines;
    }

    public WordleDictionary loadDictionary(String fileName)
            throws DictionaryException {
        return load(fileName);
    }

    private void writeToLog(String message, Throwable error) {
        if (log == null) {
            return;
        }

        log.println(message);
        if (error != null) {
            error.printStackTrace(log);
        }
        log.flush();
    }
}
