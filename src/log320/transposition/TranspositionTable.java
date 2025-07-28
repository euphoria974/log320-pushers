package log320.transposition;

import log320.entities.Move;
import log320.entities.Player;

// https://adamberent.com/transposition-table-and-zobrist-hashing/
public class TranspositionTable {
    private final Entry[] ENTRIES = new Entry[8194311];

    private int age = 0;

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
        public int age;

        public void set(long hash, int depth, int score, NodeType type, Move bestMove, int age) {
            this.hash = hash;
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.bestMove = bestMove;
            this.age = age;
        }
    }

    public Entry get(long hash, Player player) {
        hash ^= ZobristHash.getHashForPlayer(player);
        Entry entry = ENTRIES[indexOf(hash)];
        if (entry == null || entry.type == null) return null;
        return entry.hash == hash && entry.age == age ? entry : null;
    }

    public void put(long hash, Player player, int depth, int score, NodeType type, Move bestMove) {
        hash ^= ZobristHash.getHashForPlayer(player);
        Entry entry = ENTRIES[indexOf(hash)];

        if (entry.depth < depth) {
            entry.set(hash, depth, score, type, bestMove, age);
        }
    }

    public void incrementAge() {
        // TODO Not sure this is needed
        // age++;
    }

    private int indexOf(long hash) {
        return Math.floorMod(hash, ENTRIES.length);
    }
}
