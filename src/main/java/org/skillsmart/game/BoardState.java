package org.skillsmart.game;

public record BoardState(Board board, int score) {
    public BoardState(Board board, int score) {
        this.board = board;
        this.score = score;
    }
}
