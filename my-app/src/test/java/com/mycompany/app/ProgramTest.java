package com.mycompany.app;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProgramTest {

    @BeforeAll
    static void useHeadlessSwing() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void constructorCreatesEmptyGame() {
        Game game = new Game();

        assertEquals(State.PLAYING, game.state);
        assertEquals('X', game.player1.symbol);
        assertEquals('O', game.player2.symbol);
        assertArrayEquals(new char[] {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, game.board);
    }

    @Test
    void checkStateDetectsWinsDrawAndPlayingPositions() {
        Game game = new Game();

        game.symbol = 'X';
        assertEquals(State.XWIN, game.checkState(new char[] {'X', 'X', 'X', ' ', ' ', ' ', ' ', ' ', ' '}));
        assertEquals(State.XWIN, game.checkState(new char[] {'X', ' ', ' ', 'X', ' ', ' ', 'X', ' ', ' '}));
        assertEquals(State.XWIN, game.checkState(new char[] {'X', ' ', ' ', ' ', 'X', ' ', ' ', ' ', 'X'}));

        game.symbol = 'O';
        assertEquals(State.OWIN, game.checkState(new char[] {' ', ' ', 'O', ' ', 'O', ' ', 'O', ' ', ' '}));
        assertEquals(State.OWIN, game.checkState(new char[] {'O', ' ', ' ', ' ', 'O', ' ', ' ', ' ', 'O'}));

        game.symbol = 'X';
        assertEquals(State.DRAW, game.checkState(new char[] {'X', 'O', 'X', 'X', 'O', 'O', 'O', 'X', 'X'}));
        assertEquals(State.PLAYING, game.checkState(new char[] {'X', 'O', 'X', ' ', 'O', 'O', 'O', 'X', 'X'}));
    }

    @Test
    void generateMovesAddsOnlyEmptyCells() {
        Game game = new Game();
        ArrayList<Integer> moves = new ArrayList<>();

        game.generateMoves(new char[] {'X', ' ', 'O', ' ', 'X', 'O', ' ', 'X', ' '}, moves);

        assertEquals(Arrays.asList(1, 3, 6, 8), moves);
    }

    @Test
    void evaluatePositionScoresTerminalAndNonTerminalBoards() {
        Game game = new Game();
        char[] xWin = {'X', 'X', 'X', 'O', 'O', ' ', ' ', ' ', ' '};
        char[] draw = {'X', 'O', 'X', 'X', 'O', 'O', 'O', 'X', 'X'};
        char[] playing = {'X', 'O', 'X', ' ', 'O', 'O', 'O', 'X', 'X'};

        game.symbol = 'X';
        assertEquals(Game.INF, game.evaluatePosition(xWin, game.player1));
        assertEquals(-Game.INF, game.evaluatePosition(xWin, game.player2));
        assertEquals(0, game.evaluatePosition(draw, game.player1));
        assertEquals(-1, game.evaluatePosition(playing, game.player1));
    }

    @Test
    void minimaxChoosesImmediateWinningMoveAndRestoresBoard() {
        Game game = new Game();
        char[] board = {'O', 'O', ' ', 'X', 'X', ' ', ' ', ' ', ' '};
        char[] original = board.clone();

        int move = quietly(() -> game.MiniMax(board, game.player2));

        assertEquals(3, move);
        assertArrayEquals(original, board);
        assertEquals(0, game.q);
    }

    @Test
    void minAndMaxReturnTerminalScores() {
        Game game = new Game();
        char[] xWin = {'X', 'X', 'X', 'O', 'O', ' ', ' ', ' ', ' '};
        char[] oWin = {'O', 'O', 'O', 'X', 'X', ' ', ' ', ' ', ' '};

        game.symbol = 'X';
        assertEquals(Game.INF, game.MinMove(xWin, game.player1));
        assertEquals(Game.INF, game.MaxMove(xWin, game.player1));

        game.symbol = 'O';
        assertEquals(-Game.INF, game.MinMove(oWin, game.player1));
        assertEquals(-Game.INF, game.MaxMove(oWin, game.player1));
    }

    @Test
    void cellStoresCoordinatesMarkerAndDisabledState() {
        TicTacToeCell cell = new TicTacToeCell(5, 2, 1);

        assertEquals(5, cell.getNum());
        assertEquals(1, cell.getRow());
        assertEquals(2, cell.getCol());
        assertEquals(' ', cell.getMarker());
        assertTrue(cell.isEnabled());

        cell.setMarker("X");

        assertEquals('X', cell.getMarker());
        assertEquals("X", cell.getText());
        assertFalse(cell.isEnabled());
    }

    @Test
    void utilityPrintsBoardsAndMoveLists() {
        String output = captureOutput(() -> {
            Utility.print(new char[] {'X', 'O', ' ', ' ', 'X', ' ', 'O', ' ', 'X'});
            Utility.print(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9});
            Utility.print(new ArrayList<>(Arrays.asList(0, 4, 8)));
        });

        assertTrue(output.contains("X-O-"));
        assertTrue(output.contains("1-2-3-4-5-6-7-8-9-"));
        assertTrue(output.contains("0-4-8-"));
    }

    @Test
    void panelCreatesCellsAndHandlesFirstClick() {
        TicTacToePanel panel = new TicTacToePanel(new GridLayout(3, 3));

        assertEquals(9, panel.getComponentCount());
        TicTacToeCell first = (TicTacToeCell) panel.getComponent(0);

        quietly(() -> {
            first.doClick();
            return 0;
        });

        int filled = 0;
        for (int i = 0; i < panel.getComponentCount(); i++) {
            TicTacToeCell cell = (TicTacToeCell) panel.getComponent(i);
            if (cell.getMarker() != ' ') {
                filled++;
            }
        }
        assertEquals('X', first.getMarker());
        assertTrue(filled >= 2);
    }

    private static String captureOutput(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return output.toString();
    }

    private static int quietly(IntAction action) {
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        try {
            return action.run();
        } finally {
            System.setOut(original);
        }
    }

    @FunctionalInterface
    private interface IntAction {
        int run();
    }
}
