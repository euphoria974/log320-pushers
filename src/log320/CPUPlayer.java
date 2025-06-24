package log320;

import java.util.List;
import java.util.Collections;

import static log320.Const.*;

public class CPUPlayer {
    private final Board BOARD;
    private final Player PLAYER;

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
        int maxDepth = 1;
        List<Move> possibleMoves = BOARD.getPossibleMoves(PLAYER);

        // look for immediate winning moves
        Move winningMove = possibleMoves.stream().filter(Move::isWinning).findAny().orElse(null);
        if (winningMove != null) {
            return winningMove;
        }

        while(System.currentTimeMillis() - startTime < MAX_TIME_MILLIS) {
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;
            for(Move move : possibleMoves) {
                BOARD.play(move);

                int score = alphaBeta(
                    PLAYER.getOpponent(),
                    BOARD,
                    false, // maximizing for PLAYER, so the opponent will be minimizing
                    alpha,
                    beta,
                    1,
                    maxDepth,
                    startTime
                );

                BOARD.undo();

                System.out.println("Move: " + move + ", Score: " + score + ", Depth: " + maxDepth);

                if(score == MAX_TIME_SCORE) {
                    break;
                }

                // set the score for sorting
                move.setScore(score);

                // update alpha for the root node
                alpha = Math.max(alpha, score);
            }

            // sort by the best move to improve the chances of pruning branches
            // in the next iteration with alpha beta pruning
            Collections.sort(possibleMoves, Collections.reverseOrder());

            maxDepth += 1;
        }

        // since we're sorting the list, the best move will always be first
        return possibleMoves.get(0);
    }

    private int alphaBeta(Player player, Board board, boolean isMax, int alpha, int beta, int currentDepth, int maxDepth, long startTime) {
        if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
            return MAX_TIME_SCORE;
        }

        List<Move> possibleMoves = board.getPossibleMoves(player);
        int score = board.evaluate(PLAYER);

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
