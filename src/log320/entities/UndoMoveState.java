package log320.entities;

public class UndoMoveState {
    int movedPiece, capturedPiece;
    Move move;

    public UndoMoveState set(Move m, int mp, int cp) {
        move = m;
        movedPiece = mp;
        capturedPiece = cp;
        return this;
    }
}
