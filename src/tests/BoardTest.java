package tests;

import log320.entities.Player;
import log320.game.Board;
import log320.game.Game;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static log320.Const.*;

public class BoardTest {
    public static void main(String[] args) {
        Board board = new Board();

        board.init();

        board.print();

        System.out.println("possible moves:");
        System.out.println(board.getPossibleMoves(Player.RED));
        System.out.println(board.getPossibleMoves(Player.BLACK));
        board.play(ALL_MOVES.get("C2B3"));
        board.print();

        System.out.println("possible moves:");
        System.out.println(board.getPossibleMoves(Player.RED));
        System.out.println(board.getPossibleMoves(Player.BLACK));
        board.play(ALL_MOVES.get("C1C2"));
        board.print();

        System.out.println("possible moves:");
        System.out.println(board.getPossibleMoves(Player.RED));
        System.out.println(board.getPossibleMoves(Player.BLACK));
        Board clone = board.clone(ALL_MOVES.get("C2D3"));
        clone.print();

        board.undo();
        board.print();

        board.undo();
        board.print();

        board.init();
        System.out.println("possible moves:");
        System.out.println(board.getPossibleMoves(Player.RED));
        System.out.println(board.getPossibleMoves(Player.BLACK));

        System.out.println("isRowCovered: >>");
        board.init();

        // ---------------

        Path path = Paths.get("boardStates", "test7.brd");
        String content;
        try {
            List<String> lines = Files.readAllLines(path);
            content = String.join("", lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Board pawnActivatedBoard = new Board(content.substring(1));

        pawnActivatedBoard.print();

        System.out.println("red score: " + pawnActivatedBoard.evaluate(Player.RED));
        System.out.println("black score: " + pawnActivatedBoard.evaluate(Player.BLACK));

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                pawnActivatedBoard.isExposed(Player.RED, i, j);
                pawnActivatedBoard.isExposed(Player.BLACK, i, j);

                pawnActivatedBoard.set(i, j, RED_PAWN);
                pawnActivatedBoard.set(i, j, BLACK_PAWN);
                pawnActivatedBoard.isPawnActivated(Player.RED, i, j);
                pawnActivatedBoard.isPawnActivated(Player.BLACK, i, j);
            }
        }

        // ---
        Game newGame = new Game();
        newGame.start("0 0 0 0 2 0 2 0 " +
                "2 4 0 0 0 0 0 1 " +
                "0 0 1 1 0 1 0 2 " +
                "0 0 0 0 0 1 2 3 " +
                "0 4 0 3 0 1 0 3 " +
                "3 0 0 0 4 4 0 3 " +
                "0 0 0 0 4 0 4 0 " +
                "4 0 0 0 4 0 0 0 ", Player.RED);

        String s = "B7 - B8";
        String m = s.replaceAll("[^A-Za-z0-9]", "");
    }
}
