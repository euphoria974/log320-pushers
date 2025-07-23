package log320.transposition;

import log320.game.Board;

import java.util.Random;

import static log320.Const.EMPTY;

public class ZobristHash {
    private static final long[][][] PIECES_TABLE = new long[8][8][5];

    static {
        Random random = new Random(0); // fixed seed for "pseudorandom"

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                for (int piece = 0; piece < 5; piece++) {
                    PIECES_TABLE[row][col][piece] = random.nextLong();
                }
            }
        }
    }

    public static long computeHash(Board board) {
        long hash = 0L;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board.get(row, col);

                if (piece != EMPTY) {
                    hash ^= PIECES_TABLE[row][col][piece];
                }
            }
        }

        return hash;
    }

    public static long[][][] getPiecesTable() {
        return PIECES_TABLE;
    }
}
