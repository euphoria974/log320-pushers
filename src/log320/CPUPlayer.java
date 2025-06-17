package log320;

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

    public CPUPlayer(Board board, Player player) {
        this.BOARD = board;
        this.PLAYER = player;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<Move> getNextMove() {
        long startTime = System.currentTimeMillis();
        BEST_MOVES.clear();
        int maxDepth = 1;

        while (System.currentTimeMillis() - startTime < MAX_TIME_MILLIS) {
            CURRENT_BEST_MOVES.clear();
            int bestScore = Integer.MIN_VALUE;
            List<Move> possibleMoves = BOARD.getPossibleMoves(PLAYER);
            System.out.println("Possible moves: " + possibleMoves);
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<int[]>> futures = new ArrayList<>();

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

            boolean completed = true;
            for (Future<int[]> future : futures) {
                try {
                    int[] result = future.get();
                    int score = result[0];
                    int moveIndex = result[1];
                    Move move = possibleMoves.get(moveIndex);

                    System.out.println("Move: " + move + ", Score: " + score + ", Depth: " + maxDepth);

                    if (score == MAX_TIME_SCORE) {
                        completed = false;
                        break;
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        CURRENT_BEST_MOVES.clear();
                        CURRENT_BEST_MOVES.add(move);
                    } else if (score == bestScore) {
                        CURRENT_BEST_MOVES.add(move);
                    }
                } catch (Exception e) {
                    completed = false;
                    break;
                }
            }

            if (completed && !CURRENT_BEST_MOVES.isEmpty()) {
                BEST_MOVES.clear();
                BEST_MOVES.addAll(CURRENT_BEST_MOVES);
            } else {
                break;
            }

            maxDepth++;
        }

        if (BEST_MOVES.isEmpty()) {
            List<Move> possibleMoves = BOARD.getPossibleMoves(PLAYER);
            BEST_MOVES.add(possibleMoves.get(RANDOM.nextInt(possibleMoves.size())));
        }

        return BEST_MOVES;
    }

    private int alphaBeta(Player player, Board board, boolean isMax, int alpha, int beta, int currentDepth, int maxDepth, long startTime) {
        if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
            return MAX_TIME_SCORE;
        }

        List<Move> possibleMoves = board.getPossibleMoves(player);
        int score = board.evaluate(PLAYER, currentDepth);

        if (score >= WIN_SCORE || score <= LOSS_SCORE || possibleMoves.isEmpty() || currentDepth >= maxDepth) {
            return score;
        }

        if (isMax) {
            int maxScore = Integer.MIN_VALUE;

            for (Move move : possibleMoves) {
                if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
                    return MAX_TIME_SCORE;
                }

                board.play(move);
                int value = alphaBeta(player.getOpponent(), board, false, alpha, beta, currentDepth + 1, maxDepth, startTime);
                board.undo();

                if (value == MAX_TIME_SCORE) {
                    return MAX_TIME_SCORE;
                }

                // Optimisation: early cutoff
                if (value >= WIN_SCORE) {
                    return value;
                }

                maxScore = Math.max(maxScore, value);

                if (maxScore >= beta) {
                    break;
                }

                alpha = Math.max(alpha, maxScore);
            }

            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;

            for (Move move : possibleMoves) {
                if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
                    return MAX_TIME_SCORE;
                }

                board.play(move);
                int value = alphaBeta(player.getOpponent(), board, true, alpha, beta, currentDepth + 1, maxDepth, startTime);
                board.undo();

                if (value == MAX_TIME_SCORE) {
                    return MAX_TIME_SCORE;
                }

                // Optimisation: early cutoff
                if (value <= LOSS_SCORE) {
                    return value;
                }

                minScore = Math.min(minScore, value);

                if (minScore <= alpha) {
                    break;
                }

                beta = Math.min(beta, minScore);
            }
            return minScore;
        }
    }
}
