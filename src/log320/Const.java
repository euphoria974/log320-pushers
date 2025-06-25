package log320;

import java.util.Random;

public final class Const {
    // Heuristiques
    public static final long MAX_TIME_MILLIS = 4900;
    public static final int FIRST_MAX_DEPTH = 7;

    public static final int WIN_SCORE = 100000;
    public static final int LOSS_SCORE = -100000;
    public static final Random RANDOM = new Random();
    public static final int COL_CHAR_OFFSET = (int)'A';

    // Constantes du jeu
    public static final int EMPTY = 0;
    public static final int BLACK_PAWN = 1;
    public static final int BLACK_PUSHER = 2;
    public static final int RED_PAWN = 3;
    public static final int RED_PUSHER = 4;
    public static final int RED_WINNING_ROW = 7;
    public static final int BLACK_WINNING_ROW = 0;
}
