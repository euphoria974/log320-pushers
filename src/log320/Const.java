package log320;

import java.util.Random;

public final class Const {
    // Heuristiques
    static final int MAX_DEPTH = 5;
    static final long MAX_TIME_MILLIS = 4800;
    static final int MAX_TIME_SCORE = -100;

    static final int WIN_SCORE = 10000;
    static final int LOSS_SCORE = -10000;
    static final Random RANDOM = new Random();
    static final int CHAR_TO_ROW = 65;

    // Constantes du jeu
    static final int EMPTY = 0;
    static final int BLACK_PAWN = 1;
    static final int BLACK_PUSHER = 2;
    static final int RED_PAWN = 3;
    static final int RED_PUSHER = 4;

    static final int RED_WINNING_COL = 7;
    static final int BLACK_WINNING_COL = 0;
}
