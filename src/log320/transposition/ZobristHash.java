package log320.transposition;

import log320.Board;

import static log320.Const.EMPTY;
import static log320.Const.RANDOM;

public class ZobristHash {
    private static final long[][][] TABLE = new long[8][8][5];

    static {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                for (int piece = 0; piece < 5; piece++) {
                    TABLE[row][col][piece] = RANDOM.nextLong();
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
                    hash ^= TABLE[row][col][piece];
                }
            }
        }

        return hash;
    }

    public long[][][] getTable() {
        return TABLE;
    }
}
