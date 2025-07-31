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

    public CPUPlayer(Board board, Player player) {
        this.BOARD = board;
        this.PLAYER = player;
    }

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
                Board boardCopy = BOARD.clone();
                boardCopy.play(move);

                final int idx = moveIndex;
                int finalMaxDepth = maxDepth;

                futures.add(executor.submit(() -> {
                    int score = -negamax(
                            boardCopy,
                            PLAYER.getOpponent(),
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
                        // temps maximum écoulé, résultat biaisé
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

        // retourne un coup aléatoire parmi les meilleurs coups trouvés
        return BEST_MOVES.get(RANDOM.nextInt(BEST_MOVES.size()));
    }

    private int negamax(Board board, Player player, int alpha, int beta, int currentDepth, int maxDepth, long startTime) {
        if (isTimeExceeded(startTime)) {
            return Integer.MIN_VALUE;
        }

        // pour favoriser les coups gagnants, on soustrait le depth
        if (board.hasPlayerWon(player)) {
            return WIN_SCORE - currentDepth;
        }

        if (board.hasPlayerWon(player.getOpponent())) {
            return LOSS_SCORE + currentDepth;
        }

        if (currentDepth >= maxDepth) {
            return quiescenceSearch(board, alpha, beta, player, currentDepth, startTime);
        }

        int remainingDepth = maxDepth - currentDepth;
        int originalAlpha = alpha;

        TranspositionTable.Entry entry = TRANSPOSITION_TABLE.get(board.getHash());
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

        List<Move> possibleMoves = board.getSortedPossibleMoves(player);

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

        int score = Integer.MIN_VALUE;

        for (Move move : possibleMoves) {
            board.play(move);
            int value = -negamax(board, player.getOpponent(), -beta, -alpha, currentDepth + 1, maxDepth, startTime);
            board.undo();

            if (value > score) {
                score = value;
                bestMove = move;
            }

            alpha = Math.max(alpha, score);

            if (alpha >= beta) {
                break;
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

        TRANSPOSITION_TABLE.put(board.getHash(), remainingDepth, score, nodeType, bestMove);

        return score;
    }

    public int quiescenceSearch(Board board, int alpha, int beta, Player player, int currentDepth, long startTime) {
        if (isTimeExceeded(startTime)) {
            return Integer.MIN_VALUE;
        }

        if (board.hasPlayerWon(player)) {
            return WIN_SCORE - currentDepth;
        }

        if (board.hasPlayerWon(player.getOpponent())) {
            return LOSS_SCORE + currentDepth;
        }

        int bestValue = board.evaluate(player);

        alpha = Math.max(alpha, bestValue);

        if (alpha >= beta) {
            return bestValue;
        }

        for (Move move : board.getCaptureMoves(player)) {
            board.play(move);
            int value = -quiescenceSearch(board, -beta, -alpha, player.getOpponent(), currentDepth, startTime);
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
