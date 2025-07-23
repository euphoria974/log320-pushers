package log320.entities;

public class UndoMoveState {
    public int movedPiece, capturedPiece;
    public Move move;
    public long zobristHash;

    public UndoMoveState set(Move m, int mp, int cp, long z) {
        move = m;
        movedPiece = mp;
        capturedPiece = cp;
        zobristHash = z;
        return this;
    }
}
