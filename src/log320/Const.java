package log320;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class Const {
    // Heuristiques
    static final int MAX_DEPTH = 6;
    static final long MAX_TIME_MILLIS = 4900;
    static final int MAX_TIME_SCORE = -100;

    static final int WIN_SCORE = 10000;
    static final int LOSS_SCORE = -10000;
    static final Random RANDOM = new Random();
    static final int CHAR_TO_ROW = 65;
    static final Map<String, Move> ALL_MOVES = new HashMap<>(308);

    // Constantes du jeu
    static final int EMPTY = 0;
    static final int BLACK_PAWN = 1;
    static final int BLACK_PUSHER = 2;
    static final int RED_PAWN = 3;
    static final int RED_PUSHER = 4;

    static final int RED_WINNING_COL = 7;
    static final int BLACK_WINNING_COL = 0;

    static {
        Board board = new Board();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                for (int dir = -1; dir <= 1; dir++) {
                    int toRow = row + dir;
                    int toCol = col + 1;
                    if (toRow >= 0 && toRow < 8 && toCol < 8) {
                        Move move = new Move(row, col, toRow, toCol);
                        ALL_MOVES.put(move.toString(), move);
                    }
                }
            }
        }

        for (int row = 7; row >= 0; row--) {
            for (int col = 7; col > 0; col--) {
                for (int dir = -1; dir <= 1; dir++) {
                    int toRow = row - dir;
                    int toCol = col - 1;
                    if (toRow >= 0 && toRow < 8 && toCol < 8) {
                        Move move = new Move(row, col, toRow, toCol);
                        ALL_MOVES.put(move.toString(), move);
                    }
                }
            }
        }
    }
}
