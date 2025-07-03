package log320.transposition;

import log320.entities.Move;

// https://adamberent.com/transposition-table-and-zobrist-hashing/
public class TranspositionTable {
    private static final int TABLE_SIZE = 16_777_216;

    private final Entry[] table = new Entry[TABLE_SIZE];

    private int generation = 0;

    public static class Entry {
        public long hash;
        public int depth;
        public int score;
        public NodeType type;
        public int generation;
        public Move bestMove;

        public Entry(long hash, int depth, int score, NodeType type, int generation, Move bestMove) {
            this.hash = hash;
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.generation = generation;
            this.bestMove = bestMove;
        }
    }

    public Entry get(long hash) {
        int idx = index(hash);
        Entry entry = table[idx];
        return (entry != null && entry.hash == hash) ? entry : null;
    }

    public void put(long hash, int depth, int score, NodeType type, Move bestMove) {
        int idx = index(hash);
        Entry existing = table[idx];

        if (existing == null ||
                existing.generation < generation ||
                (existing.generation == generation && depth >= existing.depth) ||
                existing.hash != hash) {
            table[idx] = new Entry(hash, depth, score, type, generation, bestMove);
        }
    }

    public void newGeneration() {
        generation++;
    }

    private int index(long hash) {
        return Math.floorMod(hash, TABLE_SIZE);
    }
}
