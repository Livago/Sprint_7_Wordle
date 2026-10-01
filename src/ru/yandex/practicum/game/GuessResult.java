package ru.yandex.practicum.game;

/** Результат одного успешно выполненного хода. */
public final class GuessResult {
    private final String word;
    private final String feedback;
    private final int attemptsLeft;
    private final WordleGame.GameState state;

    public GuessResult(
            String word,
            String feedback,
            int attemptsLeft,
            WordleGame.GameState state
    ) {
        this.word = word;
        this.feedback = feedback;
        this.attemptsLeft = attemptsLeft;
        this.state = state;
    }

    public String getWord() {
        return word;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getResult() {
        return feedback;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public WordleGame.GameState getState() {
        return state;
    }
}
