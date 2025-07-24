package tests;

import log320.entities.Move;
import log320.entities.Player;
import log320.game.Board;
import log320.game.CPUPlayer;
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

        System.out.println(ZobristHash.computeHash(new Board("0 0 2 0 0 0 0 0 " +
                "1 2 1 2 0 2 1 2 " +
                "1 0 1 1 0 1 2 0 " +
                "0 0 0 0 0 0 0 1 " +
                "0 4 0 3 3 2 0 3 " +
                "0 3 0 4 0 3 4 0 " +
                "2 0 0 0 4 4 0 3 " +
                "4 0 0 0 0 0 0 4 ")));

        Board test = new Board("0 0 0 2 0 0 0 2 " +
                "0 1 0 0 2 0 1 2 " +
                "2 1 0 2 2 0 0 1 " +
                "1 0 0 0 1 1 0 0 " +
                "4 0 1 3 4 0 3 0 " +
                "3 0 3 0 3 0 2 3 " +
                "3 4 0 4 0 4 0 4 " +
                "0 0 0 0 0 0 0 0 ");
        System.out.println(test.evaluate(Player.RED));

        Move move = new CPUPlayer(test, Player.RED).getNextMove();
        board.play(move);
        System.out.println(test.evaluate(Player.RED));
    }
}
