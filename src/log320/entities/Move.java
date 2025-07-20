package log320.entities;

import static log320.Const.*;

public class Move {
    private final int FROM;
    private final int TO;
    private final String STRING_MOVE;
    private final boolean IS_WINNING;

    public Move(int from, int to) {
        this.FROM = from;
        this.TO = to;
        this.STRING_MOVE = toString(from, to);
        this.IS_WINNING = TO / 8 == RED_WINNING_ROW || TO / 8 == BLACK_WINNING_ROW;
    }

    @Override
    public String toString() {
        return STRING_MOVE;
    }

    public boolean isWinning() {
        return IS_WINNING;
    }

    public int getFrom() {
        return FROM;
    }

    public int getTo() {
        return TO;
    }

    public static String indexToCoord(int index) {
        return "" + (char) (COL_CHAR_OFFSET + (index % 8)) + (index / 8 + 1);
    }

    public static String toString(int from, int to) {
        return indexToCoord(from) + indexToCoord(to);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Move) {
            Move other = (Move) obj;
            return FROM == other.FROM && TO == other.TO;
        }
        
        return false;
    }
}
