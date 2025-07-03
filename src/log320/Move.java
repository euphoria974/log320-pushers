package log320;

import static log320.Const.COL_CHAR_OFFSET;

public class Move {
    private final int FROM_ROW, TO_ROW, FROM_COL, TO_COL;
    private final String STRING_MOVE;
    private final boolean IS_WINNING;
    private final boolean IS_DIAGONAL;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.FROM_ROW = fromRow;
        this.TO_ROW = toRow;
        this.FROM_COL = fromCol;
        this.TO_COL = toCol;
        this.STRING_MOVE = "" + ((char) (fromCol + COL_CHAR_OFFSET)) + (fromRow + 1) + ((char) (toCol + COL_CHAR_OFFSET)) + (toRow + 1);
        this.IS_WINNING = toRow == Player.BLACK.getWinningRow() || toRow == Player.RED.getWinningRow();
        this.IS_DIAGONAL = Math.abs(fromRow - toRow) == 1 && Math.abs(fromCol - toCol) == 1;
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

    public boolean isDiagonal() {
        return IS_DIAGONAL;
    }

    @Override
    public String toString() {
        return STRING_MOVE;
    }
}
