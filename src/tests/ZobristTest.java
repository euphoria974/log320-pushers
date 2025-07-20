package tests;

import log320.game.Board;
import log320.transposition.ZobristHash;

import static log320.Const.BLACK_PUSHER;

public class ZobristTest {
    public static void main(String[] args) {
        Board board = new Board();
        board.init();

        long hash = ZobristHash.computeHash(board);

        System.out.println("Zobrist Hash: " + hash);

        hash ^= ZobristHash.getHashForPosition(0, 0, BLACK_PUSHER);

        System.out.println("Zobrist Hash: " + hash);

        hash ^= ZobristHash.getHashForPosition(0, 0, BLACK_PUSHER);

        System.out.println("Zobrist Hash: " + hash);
    }
}
