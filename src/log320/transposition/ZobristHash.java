package log320.transposition;

import log320.game.Board;

import java.util.Random;

import static log320.Const.*;

public class ZobristHash {
    private static final long[][][] TABLE = new long[8][8][5];

    static {
        Random random = new Random(0); // fixed seed for "pseudorandom"

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                for (int piece = 0; piece < 5; piece++) {
                    TABLE[row][col][piece] = random.nextLong();
                }
            }
        }
    }

    public static long computeHash(Board board) {
        long hash = getHash(0L, board.getRedPushers(), RED_PUSHER);
        hash = getHash(hash, board.getRedPawns(), RED_PAWN);
        hash = getHash(hash, board.getBlackPushers(), BLACK_PUSHER);
        hash = getHash(hash, board.getBlackPawns(), BLACK_PAWN);

        return hash;
    }

    private static long getHash(long hash, long piecesBoard, int piece) {
        while (piecesBoard != 0) {
            int index = Long.numberOfTrailingZeros(piecesBoard);
            hash ^= TABLE[index / 8][index % 8][piece];
            piecesBoard &= piecesBoard - 1;
        }

        return hash;
    }

    public static long getHashForPosition(int row, int col, int piece) {
        return TABLE[row][col][piece];
    }

    public static long updateHash(long currentHash, int fromRow, int fromCol, int fromPiece, int toRow, int toCol, int toPiece) {
        if (fromPiece != EMPTY) {
            currentHash ^= TABLE[fromRow][fromCol][fromPiece];
        }

        if (toPiece != EMPTY) {
            currentHash ^= TABLE[toRow][toCol][toPiece];
        }

        if (fromPiece != EMPTY) {
            currentHash ^= TABLE[toRow][toCol][fromPiece];
        }

        return currentHash;
    }
}
