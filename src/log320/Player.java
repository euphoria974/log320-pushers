package log320;

import static log320.Const.*;

public enum Player {
    RED(RED_PAWN, RED_PUSHER, RED_WINNING_COL),
    BLACK(BLACK_PAWN, BLACK_PUSHER, BLACK_WINNING_COL);

    private final int PAWN;
    private final int PUSHER;
    private final int WINNING_COL;

    private Player OPPONENT_PLAYER;

    static {
        RED.OPPONENT_PLAYER = BLACK;
        BLACK.OPPONENT_PLAYER = RED;
    }

    Player(int pawn, int pusher, int winningCol) {
        this.PAWN = pawn;
        this.PUSHER = pusher;
        this.WINNING_COL = winningCol;
    }

    public int getPawn() {
        return PAWN;
    }

    public int getPusher() {
        return PUSHER;
    }

    public Player getOpponent() {
        return OPPONENT_PLAYER;
    }

    public int getWinningCol() {
        return WINNING_COL;
    }
}
