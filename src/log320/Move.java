package log320;

import static log320.Const.BLACK_WINNING_ROW;
import static log320.Const.COL_CHAR_OFFSET;
import static log320.Const.RED_WINNING_ROW;

public class Move implements Comparable<Move> {
    private final int FROM_ROW, TO_ROW, FROM_COL, TO_COL;
    private final boolean IS_WINNING;

    // score currently associated to this move by the minmax algorithm
    private int score;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.FROM_ROW = fromRow;
        this.TO_ROW = toRow;
        this.FROM_COL = fromCol;
        this.TO_COL = toCol;
        this.IS_WINNING = toRow == BLACK_WINNING_ROW || toRow == RED_WINNING_ROW;
    }

    public int getFromRow() {
        return FROM_ROW;
    }

    public int getToRow() {
        return TO_ROW;
    }

    public int getFromCol() {
        return FROM_COL;
    }

    public int getToCol() {
        return TO_COL;
    }

    public boolean isWinning() {
        return IS_WINNING;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(Move m) {
        return Integer.compare(this.score, m.score);
    }

    @Override
    public String toString() {
        return "" + ((char) (FROM_COL + COL_CHAR_OFFSET)) + (FROM_ROW + 1)
                + ((char) (TO_COL + COL_CHAR_OFFSET)) + (TO_ROW + 1);
    }

    public static Move fromString(String moveStr) {
        return new Move(
            moveStr.charAt(1) - '1', // fromRow
            moveStr.charAt(0) - COL_CHAR_OFFSET, // fromCol
            moveStr.charAt(3) - '1', // toRow
            moveStr.charAt(2) - COL_CHAR_OFFSET // toCol
        );
    }
}
