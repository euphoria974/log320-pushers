package log320;

import java.util.HashMap;
import java.util.Map;

public final class Const {
    // Heuristiques
    public static final long MAX_TIME_MILLIS = 4940;
    public static final int FIRST_MAX_DEPTH = 4;

    public static final int WIN_SCORE = 1000000000;
    public static final int LOSS_SCORE = -1000000000;
    public static final int COL_CHAR_OFFSET = 'A';
    public static final Map<String, Move> ALL_MOVES = new HashMap<>(308);

    // Constantes du jeu
    public static final int EMPTY = 0;
    public static final int BLACK_PAWN = 1;
    public static final int BLACK_PUSHER = 2;
    public static final int RED_PAWN = 3;
    public static final int RED_PUSHER = 4;
    public static final int RED_WINNING_ROW = 7;
    public static final int BLACK_WINNING_ROW = 0;

    static {
        // Initialisation de tous les coups possibles
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                for (int dir = -1; dir <= 1; dir++) {
                    int redToRow = row + Player.RED.getDirection();
                    int redToCol = col + dir;
                    if (redToCol >= 0 && redToCol < 8 && redToRow < 8 && redToRow >= 0) {
                        Move move = new Move(row, col, redToRow, redToCol);
                        ALL_MOVES.put(move.toString(), move);
                    }

                    int blackToRow = row + Player.BLACK.getDirection();
                    int blackToCol = col - dir;
                    if (blackToCol >= 0 && blackToCol < 8 && blackToRow < 8 && blackToRow >= 0) {
                        Move move = new Move(row, col, blackToRow, blackToCol);
                        ALL_MOVES.put(move.toString(), move);
                    }
                }
            }
        }
    }
}
