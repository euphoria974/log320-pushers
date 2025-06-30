package log320.transposition;

// https://adamberent.com/transposition-table-and-zobrist-hashing/
public class TranspositionTable {
    private static final int TABLE_SIZE = 1_000_000;

    private final Entry[] table = new Entry[TABLE_SIZE];

    public static class Entry {
        public long hash;
        public int depth;
        public int score;
        public NodeType type;
        public boolean ancient;

        public Entry(long hash, int depth, int score, NodeType type) {
            this.hash = hash;
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.ancient = false;
        }
    }

    public Entry get(long hash) {
        int idx = Math.floorMod(hash, TABLE_SIZE);
        Entry entry = table[idx];
        return (entry != null && entry.hash == hash) ? entry : null;
    }

    public void put(long hash, int depth, int score, NodeType type) {
        int idx = Math.floorMod(hash, TABLE_SIZE);
        Entry entry = table[idx];

        if (entry == null || entry.ancient || depth >= entry.depth) {
            table[idx] = new Entry(hash, depth, score, type);
        }
    }

    public void markAllAncient() {
        for (Entry e : table) {
            if (e != null) {
                e.ancient = true;
            }
        }
    }
}
