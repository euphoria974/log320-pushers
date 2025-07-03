package log320;

import static log320.Const.*;

public enum Player {
    RED(RED_PAWN, RED_PUSHER, RED_WINNING_ROW, 1),
    BLACK(BLACK_PAWN, BLACK_PUSHER, BLACK_WINNING_ROW, -1);

    private final int PAWN;
    private final int PUSHER;
    private final int WINNING_ROW;
    private final int DIRECTION;

    private Player OPPONENT_PLAYER;

    static {
        RED.OPPONENT_PLAYER = BLACK;
        BLACK.OPPONENT_PLAYER = RED;
    }

    Player(int pawn, int pusher, int winningRow, int forwardDirection) {
        this.PAWN = pawn;
        this.PUSHER = pusher;
        this.WINNING_ROW = winningRow;
        this.DIRECTION = forwardDirection;
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

    public int getWinningRow() {
        return WINNING_ROW;
    }

    public int getDirection() {
        return DIRECTION;
    }
}
