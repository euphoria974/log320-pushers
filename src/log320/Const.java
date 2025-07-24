package log320;

import log320.entities.Move;
import log320.entities.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class Const {
    // Heuristiques
    public static final long MAX_TIME_MILLIS = 4775;
    public static final int WIN_SCORE = 1000000000;
    public static final int LOSS_SCORE = -1000000000;
    public static final int COL_CHAR_OFFSET = 'A';
    public static final Map<String, Move> ALL_MOVES = new HashMap<>(308);
    public static final Random RANDOM = new Random();

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
                int fromIndex = row * 8 + col;
                for (Player player : new Player[]{Player.RED, Player.BLACK}) {
                    for (int dir = -1; dir <= 1; dir++) {
                        int toRow = row + player.getDirection();
                        int toCol = col + dir;
                        if (toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8) {
                            int toIndex = toRow * 8 + toCol;
                            Move move = new Move(fromIndex, toIndex);
                            ALL_MOVES.put(move.toString(), move);
                        }
                    }
                }
            }
        }
    }
}
