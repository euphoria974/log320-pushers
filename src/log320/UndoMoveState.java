package log320;

public class UndoMoveState {
    int fromRow, fromCol, toRow, toCol, movedPiece, capturedPiece;

    public UndoMoveState update(int fr, int fc, int tr, int tc, int mp, int cp) {
        fromRow = fr;
        fromCol = fc;
        toRow = tr;
        toCol = tc;
        movedPiece = mp;
        capturedPiece = cp;
        return this;
    }
}
