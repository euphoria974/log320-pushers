public class MoveState {
    int fromRow, fromCol, toRow, toCol, movedPiece, capturedPiece;

    MoveState(int fr, int fc, int tr, int tc, int mp, int cp) {
        fromRow = fr;
        fromCol = fc;
        toRow = tr;
        toCol = tc;
        movedPiece = mp;
        capturedPiece = cp;
    }
}
