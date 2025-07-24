package log320.transposition;

import log320.entities.Move;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// https://adamberent.com/transposition-table-and-zobrist-hashing/
public class TranspositionTable {
    private final Map<Long, Entry> table = new ConcurrentHashMap<>();

    public static class Entry {
        public long hash;
        public int depth;
        public int score;
        public NodeType type;
        public Move bestMove;

        public Entry(long hash, int depth, int score, NodeType type, Move bestMove) {
            this.hash = hash;
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.bestMove = bestMove;
        }
    }

    public Entry get(long hash) {
        Entry entry = table.get(hash);
        if (entry == null) return null;
        return entry.hash == hash ? entry : null;
    }

    public void put(long hash, int depth, int score, NodeType type, Move bestMove) {
        table.put(hash, new Entry(hash, depth, score, type, bestMove));
    }
}
