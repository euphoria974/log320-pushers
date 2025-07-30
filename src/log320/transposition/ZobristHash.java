package log320.transposition;

import log320.entities.Player;
import log320.game.Board;

import java.util.Random;

import static log320.Const.*;

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

        hash = addPiecesHash(hash, board.getRedPushers(), RED_PUSHER);
        hash = addPiecesHash(hash, board.getRedPawns(), RED_PAWN);
        hash = addPiecesHash(hash, board.getBlackPushers(), BLACK_PUSHER);
        hash = addPiecesHash(hash, board.getBlackPawns(), BLACK_PAWN);

        return hash;
    }

    private static long addPiecesHash(long hash, long piecesBoard, int piece) {
        while (piecesBoard != 0) {
            int index = Long.numberOfTrailingZeros(piecesBoard);
            int row = index >> 3; // same as index / 8
            int col = index & 7;  // same as index % 8
            hash ^= PIECES_TABLE[row][col][piece];
            piecesBoard &= piecesBoard - 1;
        }
        return hash;
    }

    public static long getHashForPosition(int row, int col, int piece) {
        return PIECES_TABLE[row][col][piece];
    }

    public static long getHashForPlayer(Player player) {
        return player == Player.RED ? 0x1234567890ABCDEFL : 0xFEDCBA0987654321L;
    }

    public static long updateHash(long currentHash, int fromRow, int fromCol, int fromPiece, int toRow, int toCol, int toPiece) {
        if (fromPiece != EMPTY) {
            currentHash ^= PIECES_TABLE[fromRow][fromCol][fromPiece];
        }

        if (toPiece != EMPTY) {
            currentHash ^= PIECES_TABLE[toRow][toCol][toPiece];
        }

        if (fromPiece != EMPTY) {
            currentHash ^= PIECES_TABLE[toRow][toCol][fromPiece];
        }

        return currentHash;
    }
}
