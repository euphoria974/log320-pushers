package log320.transposition;

import log320.entities.Move;

// https://adamberent.com/transposition-table-and-zobrist-hashing/
public class TranspositionTable {
    private final Entry[] ENTRIES = new Entry[4194311];

    public TranspositionTable() {
        for (int i = 0; i < ENTRIES.length; i++) {
            ENTRIES[i] = new Entry();
        }
    }

    public static class Entry {
        public long hash;
        public int depth;
        public int score;
        public NodeType type;
        public Move bestMove;

        public void set(long hash, int depth, int score, NodeType type, Move bestMove) {
            this.hash = hash;
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.bestMove = bestMove;
        }
    }

    public Entry get(long hash) {
        Entry entry = ENTRIES[indexOf(hash)];
        if (entry == null || entry.type == null) return null;
        return entry.hash == hash ? entry : null;
    }

    public void put(long hash, int depth, int score, NodeType type, Move bestMove) {
        Entry entry = ENTRIES[indexOf(hash)];

        if (entry.depth < depth) {
            entry.set(hash, depth, score, type, bestMove);
        }
    }

    private int indexOf(long hash) {
        return Math.floorMod(hash, ENTRIES.length);
    }
}
