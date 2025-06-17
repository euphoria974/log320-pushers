package log320;

import static log320.Const.CHAR_TO_ROW;

public class Move {
    private final int FROM_ROW, TO_ROW, FROM_COL, TO_COL;
    private final String STRING_MOVE;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.FROM_ROW = fromRow;
        this.TO_ROW = toRow;
        this.FROM_COL = fromCol;
        this.TO_COL = toCol;
        this.STRING_MOVE = "" + ((char) (fromRow + CHAR_TO_ROW)) + (fromCol + 1) + ((char) (toRow + CHAR_TO_ROW)) + (toCol + 1);
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

    @Override
    public String toString() {
        return STRING_MOVE;
    }
}
