package log320;

import log320.transposition.NodeType;
import log320.transposition.TranspositionTable;
import log320.transposition.ZobristHash;

import java.util.Collections;
import java.util.List;

import static log320.Const.FIRST_MAX_DEPTH;
import static log320.Const.MAX_TIME_MILLIS;

public class CPUPlayer {
    private final Board BOARD;
    private final Player PLAYER;
    private final TranspositionTable TRANSPOSITION_TABLE = new TranspositionTable();

    public CPUPlayer(Board board, Player player) {
        this.BOARD = board;
        this.PLAYER = player;
    }

    // Retourne la liste des coups possibles. Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public Move getNextMove() {
        long startTime = System.currentTimeMillis();
        // iterative deepening : start with a depth of 1 and adapt subsequent searches
        // using the previous results to optimize alpha beta pruning
        // https://www.chessprogramming.org/Iterative_Deepening
        int maxDepth = FIRST_MAX_DEPTH;
        List<Move> possibleMoves = BOARD.getPossibleMoves(PLAYER);
        Move bestMove = null;

        // look for immediate winning moves
        Move winningMove = possibleMoves.stream().filter(Move::isWinning).findAny().orElse(null);
        if (winningMove != null) {
            return winningMove;
        }

        while (System.currentTimeMillis() - startTime < MAX_TIME_MILLIS) {
            TRANSPOSITION_TABLE.markAllAncient();

            int currentBestScore = Integer.MIN_VALUE;
            Move currentBestMove = null;

            for (Move move : possibleMoves) {
                BOARD.play(move);

                int score = negamax(
                        PLAYER.getOpponent(),
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE,
                        0,
                        maxDepth,
                        startTime
                );

                BOARD.undo();

                System.out.println("Move: " + move + ", Score: " + score + ", Depth: " + maxDepth);

                // set the score for sorting
                move.setScore(score);
                if (score > currentBestScore) {
                    currentBestScore = score;
                    currentBestMove = move;
                }
            }

            bestMove = currentBestMove;

            // sort by the best move to improve the chances of pruning branches
            // in the next iteration with alpha beta pruning
            possibleMoves.sort(Collections.reverseOrder());

            maxDepth += 1;
        }

        return bestMove;
    }

    private int negamax(Player player, int alpha, int beta, int currentDepth, int maxDepth, long startTime) {
        long hash = ZobristHash.computeHash(BOARD);

        TranspositionTable.Entry entry = TRANSPOSITION_TABLE.get(hash);
        if (entry != null && entry.depth >= maxDepth - currentDepth) {
            if (entry.type == NodeType.EXACT) {
                return entry.score;
            } else if (entry.type == NodeType.ALPHA && entry.score <= alpha) {
                return alpha;
            } else if (entry.type == NodeType.BETA && entry.score >= beta) {
                return beta;
            }
        }

        if (isTimeLimitExceeded(startTime) || currentDepth >= maxDepth || BOARD.isGameOver()) {
            return BOARD.evaluate(player);
        }

        List<Move> possibleMoves = BOARD.getPossibleMoves(player);
        if (possibleMoves.isEmpty()) {
            return BOARD.evaluate(player);
        }

        Move winningMove = possibleMoves.stream().filter(Move::isWinning).findAny().orElse(null);
        if (winningMove != null) {
            return player == PLAYER ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }

        int value = Integer.MIN_VALUE;
        NodeType nodeType = NodeType.ALPHA;

        for (Move move : possibleMoves) {
            BOARD.play(move);
            // https://en.wikipedia.org/wiki/Negamax
            int score = -negamax(player.getOpponent(), -beta, -alpha, currentDepth + 1, maxDepth, startTime);
            BOARD.undo();

            value = Math.max(value, score);

            if (value > alpha) {
                alpha = value;
                nodeType = NodeType.EXACT;
            }

            if (value >= beta) {
                nodeType = NodeType.BETA;
                break;
            }
        }

        TRANSPOSITION_TABLE.put(hash, maxDepth - currentDepth, value, nodeType);

        return value;
    }

    private boolean isTimeLimitExceeded(long startTime) {
        return System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS;
    }
}
