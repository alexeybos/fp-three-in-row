package org.skillsmart.game;

import java.util.Arrays;

public final class Board {

    private final int size;
    private final Element[][] cells;   // наружу не отдаём

    public Board(int size) {
        this.size = size;
        this.cells = new Element[size][size];
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                cells[x][y] = new Element(Element.EMPTY);
    }

    private Board(int size, Element[][] cells) {
        this.size = size;
        this.cells = cells;
    }

    public int size() { return size; }

    public Element get(int x, int y) { return cells[x][y]; }

    public Board copy() {
        Element[][] copy = deepCopy(cells);   // тот же deepCopy, что в with
        return new Board(size, copy);
    }

    private Element[][] deepCopy(Element[][] cells) {
        Element[][] copy = new Element[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(cells[i], 0, copy[i], 0, size);
        }
        return copy;
    }

    public Board swap(int x1, int y1, int x2, int y2) {
        Element[][] copy = deepCopy(cells);
        Element tmp = copy[x1][y1];
        copy[x1][y1] = copy[x2][y2];
        copy[x2][y2] = tmp;
        return new Board(size, copy);
    }

    public static Board of(int size, Element[][] cells) {
        Element[][] copy = new Element[size][];
        for (int i = 0; i < size; i++) {
            copy[i] = Arrays.copyOf(cells[i], cells[i].length);
        }
        return new Board(size, copy);
    }

    // deep copy для иммутабельности...
    public Element[][] toArray() {
        Element[][] copy = new Element[size][];
        for (int i = 0; i < size; i++) {
            copy[i] = Arrays.copyOf(cells[i], cells[i].length);
        }
        return copy;
    }

    //очень сильно под вопросом
    public Board with(int x, int y, Element e) {
        Element[][] copy = deepCopy(cells);
        copy[x][y] = e;
        return new Board(size, copy);
    }
}
