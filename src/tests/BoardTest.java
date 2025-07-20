package tests;

import log320.entities.Player;
import log320.game.Board;

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

        System.out.println(board.isRowCovered(Player.RED, 1)); // true
        System.out.println(board.isRowCovered(Player.RED, 2)); // true
        System.out.println(board.isRowCovered(Player.RED, 3)); // false

        System.out.println(board.isRowCovered(Player.BLACK, 6)); // true
        System.out.println(board.isRowCovered(Player.BLACK, 5)); // true
        System.out.println(board.isRowCovered(Player.BLACK, 4));  // false

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
        Board tes = new Board(
                "2 2 0 0 2 2 0 2 " +
                        "1 1 2 1 2 2 0 0 " +
                        "0 0 0 0 1 1 1 1 " +
                        "0 0 1 0 0 0 0 0 " +
                        "0 0 0 3 0 0 0 0 " +
                        "3 3 0 4 0 0 0 3 " +
                        "0 4 3 0 3 3 3 4 " +
                        "0 4 4 4 0 4 4 0");
        tes.print();

        System.out.println("RED possible moves:" + tes.getPossibleMoves(Player.RED));
        System.out.println("BLACK possible moves:" + tes.getPossibleMoves(Player.BLACK));
    }
}
