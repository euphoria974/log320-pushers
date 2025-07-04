package tests;

import log320.entities.Player;
import log320.game.Board;
import log320.transposition.ZobristHash;

public class ZobristTest {
    public static void main(String[] args) {
        Board board = new Board();
        board.init();

        long hash = ZobristHash.computeHash(board);

        System.out.println("Zobrist Hash: " + hash);

        hash ^= ZobristHash.getTable()[0][0][Player.BLACK.getPusher()];

        System.out.println("Zobrist Hash: " + hash);

        hash ^= ZobristHash.getTable()[0][0][Player.BLACK.getPusher()];

        System.out.println("Zobrist Hash: " + hash);
    }
}
