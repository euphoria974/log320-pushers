package log320.game;

import log320.entities.Move;
import log320.entities.Player;
import log320.transposition.NodeType;
import log320.transposition.TranspositionTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static log320.Const.*;

public class CPUPlayer {
    private final Board BOARD;
    private final Player PLAYER;
    private final ArrayList<Move> BEST_MOVES = new ArrayList<>(20);
    private final ArrayList<Move> CURRENT_BEST_MOVES = new ArrayList<>(20);
    private final TranspositionTable TRANSPOSITION_TABLE = new TranspositionTable();

    private final int[][] historyTable = new int[64][64];

    public CPUPlayer(Board board, Player player) {
        this.BOARD = board;
        this.PLAYER = player;
    }

    // Retourne la liste des coups possibles. Cette liste contient
    // plusieurs coups possibles si et seulement si plusieurs coups
    // ont le même score.
    public Move getNextMove() {
        long startTime = System.currentTimeMillis();
        BEST_MOVES.clear();
        int maxDepth = 1;
        int bestScore;
        int finalBestScore = Integer.MIN_VALUE;
        List<Move> possibleMoves = BOARD.getSortedPossibleMoves(PLAYER);
        ExecutorService executor;
        List<Future<int[]>> futures;

        TRANSPOSITION_TABLE.incrementAge();

        timeLoop:
        while (!isTimeExceeded(startTime)) {
            CURRENT_BEST_MOVES.clear();
            bestScore = Integer.MIN_VALUE;
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            futures = new ArrayList<>();

            if (!BEST_MOVES.isEmpty()) {
                for (int i = BEST_MOVES.size() - 1; i >= 0; i--) {
                    Move best = BEST_MOVES.get(i);
                    int idx = possibleMoves.indexOf(best);
                    if (idx > 0) {
                        possibleMoves.remove(idx);
                        possibleMoves.addFirst(best);
                    }
                }
            }

            for (int moveIndex = 0; moveIndex < possibleMoves.size(); moveIndex++) {
                Move move = possibleMoves.get(moveIndex);
                Board boardCopy = BOARD.clone(move);

                final int idx = moveIndex;
                int finalMaxDepth = maxDepth;
                futures.add(executor.submit(() -> {
                    int score = alphaBeta(
                            boardCopy,
                            false,
                            Integer.MIN_VALUE,
                            Integer.MAX_VALUE,
                            1,
                            finalMaxDepth,
                            startTime
                    );
                    return new int[]{score, idx};
                }));
            }

            executor.shutdown();

            for (Future<int[]> future : futures) {
                try {
                    int[] result = future.get();
                    int score = result[0];
                    int moveIndex = result[1];
                    Move move = possibleMoves.get(moveIndex);

                    if (score == Integer.MIN_VALUE) {
                        // temps maximum écoulé
                        break timeLoop;
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        finalBestScore = bestScore;
                        CURRENT_BEST_MOVES.clear();
                        CURRENT_BEST_MOVES.add(move);
                    } else if (score == bestScore) {
                        CURRENT_BEST_MOVES.add(move);
                    }
                } catch (Exception e) {
                    System.out.println("\033[91;40m" + e.getMessage());
                    break;
                }
            }

            if (!CURRENT_BEST_MOVES.isEmpty()) {
                BEST_MOVES.clear();
                BEST_MOVES.addAll(CURRENT_BEST_MOVES);
            }

            maxDepth++;
        }

        if (BEST_MOVES.isEmpty()) {
            System.out.println("\033[91;40m!!! CRITICAL ERROR: No best moves found !!!");
            BEST_MOVES.addAll(possibleMoves);
        }

        System.out.println("\033[32;40mBest moves found: " + BEST_MOVES + " with score: " + finalBestScore + " at depth: " + maxDepth);

        return BEST_MOVES.get(RANDOM.nextInt(BEST_MOVES.size()));
    }

    private int alphaBeta(Board board, boolean isMax, int alpha, int beta, int currentDepth, int maxDepth, long startTime) {
        if (isTimeExceeded(startTime)) {
            return Integer.MIN_VALUE;
        }

        int boardScore = board.evaluate(PLAYER);
        // pour favoriser les coups gagnants, on soustrait le depth
        if (boardScore >= WIN_SCORE) {
            return WIN_SCORE - currentDepth;
        }

        if (boardScore <= LOSS_SCORE) {
            return LOSS_SCORE + currentDepth;
        }

        Player player = isMax ? PLAYER : PLAYER.getOpponent();
        if (currentDepth >= maxDepth) {
            int quiescenceScore = quiescenceSearch(board, alpha, beta, player, startTime);

            if (quiescenceScore >= WIN_SCORE) {
                return WIN_SCORE - currentDepth;
            }

            if (quiescenceScore <= LOSS_SCORE) {
                return LOSS_SCORE + currentDepth;
            }

            return quiescenceScore;
        }

        int remainingDepth = maxDepth - currentDepth;
        int originalAlpha = alpha;

        TranspositionTable.Entry entry = TRANSPOSITION_TABLE.get(board.getHash(), player);
        if (entry != null && entry.depth >= remainingDepth) {
            switch (entry.type) {
                case EXACT:
                    return entry.score;
                case ALPHA:
                    if (entry.score <= alpha) return entry.score;
                    break;
                case BETA:
                    if (entry.score >= beta) return entry.score;
                    break;
            }
        }

        List<Move> possibleMoves = board.getSortedPossibleMoves(player, historyTable);

        Move bestMove = null;
        if (entry != null && entry.bestMove != null) {
            for (int i = 0; i < possibleMoves.size(); i++) {
                if (possibleMoves.get(i).equals(entry.bestMove)) {
                    bestMove = possibleMoves.remove(i);
                    possibleMoves.addFirst(bestMove);
                    break;
                }
            }
        }

        int score = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        if (isMax) {
            for (Move move : possibleMoves) {
                board.play(move);
                int value = alphaBeta(board, false, alpha, beta, currentDepth + 1, maxDepth, startTime);
                board.undo();

                if (value > score) {
                    score = value;
                    bestMove = move;
                }

                alpha = Math.max(alpha, score);

                if (alpha >= beta) {
                    if (bestMove != null) {
                        historyTable[bestMove.getFrom()][bestMove.getTo()] += remainingDepth * remainingDepth;
                    }

                    break;
                }
            }
        } else {
            for (Move move : possibleMoves) {
                board.play(move);
                int value = alphaBeta(board, true, alpha, beta, currentDepth + 1, maxDepth, startTime);
                board.undo();

                if (value < score) {
                    score = value;
                    bestMove = move;
                }

                beta = Math.min(beta, score);

                if (beta <= alpha) {
                    break;
                }
            }
        }

        NodeType nodeType;
        if (score <= originalAlpha) {
            nodeType = NodeType.ALPHA;
        } else if (score >= beta) {
            nodeType = NodeType.BETA;
        } else {
            nodeType = NodeType.EXACT;
        }

        TRANSPOSITION_TABLE.put(board.getHash(), player, remainingDepth, score, nodeType, bestMove);

        return score;
    }

    public int quiescenceSearch(Board board, int alpha, int beta, Player player, long startTime) {
        if (isTimeExceeded(startTime)) {
            return Integer.MIN_VALUE;
        }

        int bestValue = board.evaluate(player);

        alpha = Math.max(alpha, bestValue);

        if (alpha >= beta) {
            return bestValue;
        }

        for (Move move : board.getNoisyMoves(player)) {
            board.play(move);
            int value = -quiescenceSearch(board, -beta, -alpha, player.getOpponent(), startTime);
            board.undo();

            bestValue = Math.max(bestValue, value);

            alpha = Math.max(alpha, bestValue);

            if (alpha >= beta) {
                break;
            }
        }

        return bestValue;
    }

    private boolean isTimeExceeded(long startTime) {
        return System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS;
    }
}
