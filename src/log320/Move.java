package log320;

import static log320.Const.BLACK_WINNING_COL;
import static log320.Const.CHAR_TO_ROW;

public class Move {
    private final int FROM_ROW, TO_ROW, FROM_COL, TO_COL;
    private final String STRING_MOVE;
    private final boolean IS_WINNING;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.FROM_ROW = fromRow;
        this.TO_ROW = toRow;
        this.FROM_COL = fromCol;
        this.TO_COL = toCol;
        this.STRING_MOVE = "" + ((char) (fromRow + CHAR_TO_ROW)) + (fromCol + 1) + ((char) (toRow + CHAR_TO_ROW)) + (toCol + 1);
        this.IS_WINNING = toCol == BLACK_WINNING_COL || toCol == Player.RED.getWinningCol();
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

    @Override
    public String toString() {
        return STRING_MOVE;
    }
}
