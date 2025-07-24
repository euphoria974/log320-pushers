package log320.entities;

public class UndoMoveState {
    public long redPushers, redPawns, blackPushers, blackPawns, zobristHash;

    public UndoMoveState set(long r, long rp, long b, long bp, long z) {
        redPushers = r;
        redPawns = rp;
        blackPushers = b;
        blackPawns = bp;
        zobristHash = z;
        return this;
    }
}
