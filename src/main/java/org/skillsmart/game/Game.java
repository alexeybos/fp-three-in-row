package org.skillsmart.game;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class Game {

    private Game() {}

    private static final Random RANDOM = new Random();
    private static final char[] SYMBOLS = { 'A', 'B', 'C', 'D', 'E', 'F' };
    private static final Scanner SCANNER = new Scanner(System.in);

    public static BoardState draw(BoardState bs) {
        return draw(bs, false);
    }

    public static BoardState draw(BoardState bs, boolean ask) {
        int size = bs.board().size();

        // шапка с номерами столбцов
        System.out.print("   ");
        for (int j = 0; j < size; j++) {
            System.out.print(j + " ");
        }
        System.out.println();

        for (int i = 0; i < size; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < size; j++) {
                System.out.print(bs.board().get(i, j).symbol() + " ");
            }
            System.out.println();
        }
        System.out.println();

        if (ask) {
            SCANNER.nextLine();
        }
        return bs;
    }

    public static Board cloneBoard(Board board) {
        return board.copy();
    }

    public static char randomSymbol() {
        return SYMBOLS[RANDOM.nextInt(SYMBOLS.length)];
    }

    public static BoardState readMove(BoardState bs) {
        System.out.println(">");
        String input = SCANNER.nextLine().trim();
        if (input.equals("q")) {
            System.exit(0);
        }

        String[] coords = input.split("\\s+");

        Board board = cloneBoard(bs.board());
        int y  = Integer.parseInt(coords[0]);
        int x  = Integer.parseInt(coords[1]);
        int y1 = Integer.parseInt(coords[2]);
        int x1 = Integer.parseInt(coords[3]);

        Board newBoard = board.swap(x, y, x1, y1);

        //BoardState bb = new BoardState(newBoard, bs.score());
        BoardState bb = processCascade(new BoardState(newBoard, bs.score()));

        return bb;
    }

    public static List<Match> findMatches(Board board) {
        List<Match> matches = new ArrayList<>();
        int size = board.size();

        // Горизонтальные комбинации
        for (int row = 0; row < size; row++) {
            int startCol = 0;
            for (int col = 1; col < size; col++) {
                if (board.get(row, startCol).symbol() == Element.EMPTY) {
                    startCol = col;
                    continue;
                }
                if (board.get(row, col).symbol() == Element.EMPTY) {
                    addMatchIfValid(matches, row, startCol, col - startCol, MatchDirection.HORIZONTAL);
                    startCol = col + 1;
                    continue;
                }
                if (board.get(row, col).symbol() != board.get(row, startCol).symbol()) {
                    addMatchIfValid(matches, row, startCol, col - startCol, MatchDirection.HORIZONTAL);
                    startCol = col;
                } else if (col == size - 1) {
                    addMatchIfValid(matches, row, startCol, col - startCol + 1, MatchDirection.HORIZONTAL);
                }
            }
        }

        // Вертикальные комбинации
        for (int col = 0; col < size; col++) {
            int startRow = 0;
            for (int row = 1; row < size; row++) {
                if (board.get(startRow, col).symbol() == Element.EMPTY) {
                    startRow = row;
                    continue;
                }
                if (board.get(row, col).symbol() == Element.EMPTY) {
                    addMatchIfValid(matches, startRow, col, row - startRow, MatchDirection.VERTICAL);
                    startRow = row + 1;
                    continue;
                }
                if (board.get(row, col).symbol() != board.get(startRow, col).symbol()) {
                    addMatchIfValid(matches, startRow, col, row - startRow, MatchDirection.VERTICAL);
                    startRow = row;
                } else if (row == size - 1) {
                    addMatchIfValid(matches, startRow, col, row - startRow + 1, MatchDirection.VERTICAL);
                }
            }
        }

        return matches;
    }

    private static void addMatchIfValid(List<Match> matches, int row, int col,
                                        int length, MatchDirection direction) {
        if (length >= 3) {
            matches.add(new Match(direction, row, col, length));
        }
    }

    public static BoardState removeMatches(BoardState currentState, List<Match> matches) {
        if (matches == null || matches.isEmpty()) {
            return currentState;
        }

        Element[][] markedCells = markCellsForRemoval(currentState.board(), matches);
        Element[][] gravityAppliedCells = applyGravity(markedCells, currentState.board().size());

        int removedCount = matches.stream().mapToInt(Match::length).sum();
        int newScore = currentState.score() + calculateScore(removedCount);

        return new BoardState(
                Board.of(currentState.board().size(), gravityAppliedCells),
                newScore);
    }

    private static Element[][] markCellsForRemoval(Board board, List<Match> matches) {
        Element[][] newCells = board.toArray();   // защитная копия — аналог .Clone()
        Element empty = new Element(Element.EMPTY);

        for (Match match : matches) {
            for (int i = 0; i < match.length(); i++) {
                int row = match.direction() == MatchDirection.HORIZONTAL
                        ? match.row() : match.row() + i;
                int col = match.direction() == MatchDirection.HORIZONTAL
                        ? match.col() + i : match.col();

                newCells[row][col] = empty;
            }
        }

        return newCells;
    }

    private static Element[][] applyGravity(Element[][] cells, int size) {
        Element[][] newCells = new Element[size][size];
        Element empty = new Element(Element.EMPTY);

        for (int row = 0; row < size; row++) {
            Arrays.fill(newCells[row], empty);
        }

        for (int col = 0; col < size; col++) {
            int newRow = size - 1;
            for (int row = size - 1; row >= 0; row--) {
                if (cells[row][col].symbol() != Element.EMPTY) {
                    newCells[newRow][col] = cells[row][col];
                    newRow--;
                }
            }
        }

        return newCells;
    }

    private static int calculateScore(int removedCount) {
        return removedCount * 10;
    }

    public static BoardState fillEmptySpaces(BoardState currentState) {
        Board board = currentState.board();
        int size = board.size();

        Element[][] newCells = board.toArray();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (newCells[row][col].symbol() == Element.EMPTY) {
                    newCells[row][col] = new Element(randomSymbol());
                }
            }
        }

        return new BoardState(Board.of(size, newCells), currentState.score());
    }

    public static BoardState initializeGame() {
        return pipeln(new BoardState(new Board(8), 0),
                Game::fillEmptySpaces,
                Game::processCascade
        );
    }

    //все хорошо, вот только нельзя разные типы в таком пайплайне использовать
    @SafeVarargs
    private static <T> T pipeln(T value, Function<T, T>... steps) {
        for (Function<T, T> step : steps) {
            value = step.apply(value);
        }
        return value;
    }

    public static <TIn, TOut> TOut pipe(TIn input, Function<TIn, TOut> func) {
        return func.apply(input);
    }

    // компактнее не получается.
    public static BoardState processCascade_old(BoardState state) {
        List<Match> matches = findMatches(state.board());
        if (matches.isEmpty()) {
            return state;
        }

        BoardState bs = pipeln(
                removeMatches(state, matches),
                Game::fillEmptySpaces
        );

        return processCascade_old(bs);
    }

    public static BoardState processCascade(BoardState state) {
        return findMatches(state.board()).size() == 0 ? state : cascadePipeline(state);
    }

    public static BoardState cascadePipeline(BoardState state) {
        boolean debug = true;
        return pipeln(
                state,
                bs -> removeMatches(bs, findMatches(bs.board())),
                bs -> draw(bs, debug),
                Game::fillEmptySpaces,
                bs -> draw(bs, debug),
                Game::processCascade
        );
    }

}
