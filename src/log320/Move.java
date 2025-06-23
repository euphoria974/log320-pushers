package log320;

import static log320.Const.BLACK_WINNING_ROW;
import static log320.Const.COL_CHAR_OFFSET;
import static log320.Const.RED_WINNING_ROW;

public class Move {
    private final int FROM_ROW, TO_ROW, FROM_COL, TO_COL;
    private final String STRING_MOVE;
    private final boolean IS_WINNING;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.FROM_ROW = fromRow;
        this.TO_ROW = toRow;
        this.FROM_COL = fromCol;
        this.TO_COL = toCol;
        this.STRING_MOVE = getMoveString(fromRow, fromCol, toRow, toCol);
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

    @Override
    public String toString() {
        return STRING_MOVE;
    }

    public static String getMoveString(int fromRow, int fromCol, int toRow, int toCol) {
        return "" + ((char) (fromCol + COL_CHAR_OFFSET)) + (fromRow + 1)
                + ((char) (toCol + COL_CHAR_OFFSET)) + (toRow + 1);
    }
}
