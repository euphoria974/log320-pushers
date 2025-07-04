package log320.entities;

public class UndoMoveState {
    public int movedPiece, capturedPiece;
    public Move move;

    public UndoMoveState set(Move m, int mp, int cp) {
        move = m;
        movedPiece = mp;
        capturedPiece = cp;
        return this;
    }
}
