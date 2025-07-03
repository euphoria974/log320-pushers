package log320;

import java.util.ArrayList;
import java.util.Collections;
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

    public CPUPlayer(Board board, Player player) {
        this.BOARD = board;
        this.PLAYER = player;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public Move getNextMove() {
        long startTime = System.currentTimeMillis();
        BEST_MOVES.clear();
        int maxDepth = 1;
        int bestScore;
        int finalBestScore = Integer.MIN_VALUE;
        List<Move> possibleMoves = BOARD.getPossibleMoves(PLAYER);
        ExecutorService executor;
        List<Future<int[]>> futures;

        Move winningMove = possibleMoves.stream().filter(Move::isWinning).findAny().orElse(null);
        if (winningMove != null) {
            return winningMove;
        }

        timeLoop:
        while (!isTimeExceeded(startTime)) {
            CURRENT_BEST_MOVES.clear();
            bestScore = Integer.MIN_VALUE;
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            futures = new ArrayList<>();

            for (int moveIndex = 0; moveIndex < possibleMoves.size(); moveIndex++) {
                Move move = possibleMoves.get(moveIndex);
                Board boardCopy = BOARD.clone();
                boardCopy.play(move);

                final int idx = moveIndex;
                int finalMaxDepth = maxDepth;
                futures.add(executor.submit(() -> {
                    int score = alphaBeta(
                            PLAYER.getOpponent(),
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

                    // TODO
                    // System.out.println("\033[94;40mMove: " + move + ", Score: " + score + ", Depth: " + maxDepth);

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

        Collections.shuffle(BEST_MOVES);
        return BEST_MOVES.getFirst();
    }

    private int alphaBeta(Player player, Board board, boolean isMax, int alpha, int beta, int currentDepth, int maxDepth, long startTime) {
        if (isTimeExceeded(startTime)) {
            return Integer.MIN_VALUE;
        }

        int boardScore = board.evaluate(PLAYER);
        if (boardScore >= WIN_SCORE || boardScore <= LOSS_SCORE || currentDepth >= maxDepth) {
            return boardScore;
        }

        List<Move> possibleMoves = board.getPossibleMoves(player);

        if (isMax) {
            int maxScore = Integer.MIN_VALUE;

            for (Move move : possibleMoves) {
                board.play(move);
                int value = alphaBeta(player.getOpponent(), board, false, alpha, beta, currentDepth + 1, maxDepth, startTime);
                board.undo();

                maxScore = Math.max(maxScore, value);
                alpha = Math.max(alpha, maxScore);

                if (alpha >= beta) {
                    break;
                }
            }

            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;

            for (Move move : possibleMoves) {
                board.play(move);
                int value = alphaBeta(player.getOpponent(), board, true, alpha, beta, currentDepth + 1, maxDepth, startTime);
                board.undo();

                minScore = Math.min(minScore, value);
                beta = Math.min(beta, minScore);

                if (beta <= alpha) {
                    break;
                }
            }
            return minScore;
        }
    }

    private boolean isTimeExceeded(long startTime) {
        return System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS;
    }
}
