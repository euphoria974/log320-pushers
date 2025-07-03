package tests;

import log320.Board;
import log320.Player;

public class BoardTest {
    public static void main(String[] args) {
        Board board = new Board();

        board.init();
        board.print();

        System.out.println(board.getPossibleMoves(Player.RED));
        System.out.println(board.getPossibleMoves(Player.BLACK));

        System.out.println(board.isRowCovered(Player.RED, 1)); // true
        System.out.println(board.isRowCovered(Player.RED, 2)); // true
        System.out.println(board.isRowCovered(Player.RED, 3)); // false

        System.out.println(board.isRowCovered(Player.BLACK, 6)); // true
        System.out.println(board.isRowCovered(Player.BLACK, 5)); // true
        System.out.println(board.isRowCovered(Player.BLACK, 4));  // false

        board.clear();
        board.set(0, 0, Player.RED.getPusher());
        board.set(1, 1, Player.BLACK.getPawn());
        System.out.println(board.isExposed(Player.RED, 0, 0)); // false
        board.set(2, 2, Player.BLACK.getPusher());
        System.out.println(board.isExposed(Player.RED, 0, 0)); // true

        board.clear();

        for (int col = 0; col < 8; col++) {
            board.set(0, col, Player.RED.getPusher());
            board.set(7, col, Player.BLACK.getPusher());
        }

        board.print();

        System.out.println(board.getPossibleMoves(Player.RED));
        System.out.println(board.getPossibleMoves(Player.BLACK));
    }
}