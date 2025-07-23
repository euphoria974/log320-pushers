package log320.transposition;

import log320.entities.Move;

// https://adamberent.com/transposition-table-and-zobrist-hashing/
public class TranspositionTable {
    private final Entry[] ENTRIES = new Entry[1 << 20];

    public static class Entry {
        public int depth;
        public int score;
        public NodeType type;
        public Move bestMove;

        public Entry(int depth, int score, NodeType type, Move bestMove) {
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.bestMove = bestMove;
        }
    }

    public Entry get(long hash) {
        return ENTRIES[getIndex(hash)];
    }

    public void put(long hash, int depth, int score, NodeType type, Move bestMove) {
        ENTRIES[getIndex(hash)] = new Entry(depth, score, type, bestMove);
    }

    private int getIndex(long hash) {
        return Math.floorMod(hash, ENTRIES.length);
    }
}
