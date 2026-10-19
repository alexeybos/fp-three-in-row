package org.skillsmart.game;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class Game {

    private Game() {}

    private static final Random RANDOM = new Random();
    private static final char[] SYMBOLS = { 'A', 'B', 'C', 'D', 'E', 'F' };
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void draw(Board board) {
        int size = board.size();

        // шапка с номерами столбцов
        System.out.print("   ");
        for (int j = 0; j < size; j++) {
            System.out.print(j + " ");
        }
        System.out.println();

        for (int i = 0; i < size; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < size; j++) {
                System.out.print(board.get(i, j).symbol() + " ");
            }
            System.out.println();
        }
        System.out.println();
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

    public static BoardState processCascade_old(BoardState state) {
        List<Match> matches = findMatches(state.board());
        if (matches.isEmpty()) {
            return state;
        }

        BoardState afterRemoval = removeMatches(state, matches);
        BoardState afterFill = fillEmptySpaces(afterRemoval);

        return processCascade(afterFill);
    }

    // компактнее не получается.
    public static BoardState processCascade(BoardState state) {
        List<Match> matches = findMatches(state.board());
        if (matches.isEmpty()) {
            return state;
        }

        BoardState bs = pipeln(
                removeMatches(state, matches),
                Game::fillEmptySpaces
        );

        return processCascade(bs);
    }

    /*public static BoardState initializeGame() {
        return processCascade(fillEmptySpaces(new BoardState(new Board(8), 0)));
    }*/

    public static BoardState initializeGame() {
        // честно говоря, немного "подзавис" над мыслью как сделать пайплайн в Java:
        // 1. Builder здесь вообще никаким боком - это паттерн создания объектов, так что fillEmptySpaces и processCascade сюда ну никак не зайдут.
        // 2. Через перенос fillEmptySpaces и processCascade в BoardState - ну тут как бы мы вроде от всего этого уходим. никакого ооп...
        // Да и вообще, эти методы никак к классу BoardState не относятся.
        // Операций типа |> тут нет. Сделал нижеследующий в комментах вариант (чисто чтобы было видно, что читал материал,
        // т.к. вариант с промежуточными переменными, все же лучше, чем со вложенными скобками)

        /*
        BoardState bs = new BoardState(new Board(8), 0);
        bs = Game.fillEmptySpaces(bs);
        return processCascade(bs);*/

        //ну а рабочий вариант сделал с эмуляцией пайплайна:
        return pipeln(new BoardState(new Board(8), 0),
                Game::fillEmptySpaces,
                Game::processCascade
        );


        //return processCascade(fillEmptySpaces();
    }

    @SafeVarargs
    private static <T> T pipeln(T value, UnaryOperator<T>... steps) {
        T result = value;
        for (UnaryOperator<T> step : steps) {
            result = step.apply(result);
        }
        return result;
    }

    // попробовал реализовать pipe как в примере в курсе, но дальнейшая реализация initializeGame2
    // получается не очень красивой. Опять куча скобок и никакого праздника...
    // Хотя у предыдущей реализации через pipeln тоже серьезное ограничение, я могу обработать только один тип для всего
    // набора функций, менять его нельзя...
    public static <TIn, TOut> TOut pipe(TIn input, Function<TIn, TOut> func) {
        return func.apply(input);
    }

    public static BoardState initializeGame2() {

        //ну а рабочий вариант сделал с эмуляцией пайплайна:
        return pipe(new BoardState(new Board(8), 0),
                state -> pipe(fillEmptySpaces(state),
                Game::processCascade)
        );


        //return processCascade(fillEmptySpaces();
    }

}
