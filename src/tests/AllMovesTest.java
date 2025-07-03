package tests;

import log320.Board;

import static log320.Const.ALL_MOVES;

public class AllMovesTest {
    public static void main(String[] args) {
        System.out.println(ALL_MOVES.values());

        Board board = new Board();
        board.init();

        board.print();
        board.play(ALL_MOVES.get("B2C3"));
        board.print();
        board.play(ALL_MOVES.get("C7B6"));
        board.play(ALL_MOVES.get("C8C7"));
        board.print();
    }
}
