package org.skillsmart.game;

public record Element(char symbol) {
    public static final char EMPTY = '0';

    public Element {
        if (symbol != EMPTY && (symbol < 'A' || symbol > 'F')) {
            throw new IllegalArgumentException("Invalid symbol: " + symbol);
        }
    }
}
