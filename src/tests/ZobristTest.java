package tests;

import log320.game.Board;

import static log320.Const.ALL_MOVES;

public class ZobristTest {
    public static void main(String[] args) {
        Board board = new Board();
        board.init();

        System.out.println("Zobrist Hash: " + board.getHash());

        board.play(ALL_MOVES.get("A7A6"));

        System.out.println("Zobrist Hash: " + board.getHash());

        board.undo();

        System.out.println("Zobrist Hash: " + board.getHash());


        board.play(ALL_MOVES.get("A7A6"));
        System.out.println("Zobrist Hash: " + board.getHash());
        board.play(ALL_MOVES.get("B7B6"));
        System.out.println("Zobrist Hash: " + board.getHash());
        board.play(ALL_MOVES.get("B7B6"));
        System.out.println("Zobrist Hash: " + board.getHash());

        board.undo();

        System.out.println("Zobrist Hash: " + board.getHash());

        board.undo();

        System.out.println("Zobrist Hash: " + board.getHash());

        board.undo();

        System.out.println("Zobrist Hash: " + board.getHash());
    }
}
