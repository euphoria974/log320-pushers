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
            for(Move move : possibleMoves) {
                BOARD.play(move);

                int score = alphaBeta(
                    PLAYER.getOpponent(),
                    BOARD,
                    false, // maximizing for PLAYER, so the opponent will be minimizing
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    1,
                    maxDepth,
                    startTime
                );

                BOARD.undo();

                System.out.println("Move: " + move + ", Score: " + score + ", Depth: " + maxDepth);

                // set the score for sorting
                move.setScore(score);
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
        if (isTimeLimitExceeded(startTime) || currentDepth >= maxDepth || board.isGameOver()) {
            return board.evaluate(PLAYER);
        }

        List<Move> possibleMoves = board.getPossibleMoves(player);
        if(possibleMoves.isEmpty()) {
            return board.evaluate(PLAYER);
        }
        
        if (isMax) {
            int maxScore = Integer.MIN_VALUE;

            for (Move move : possibleMoves) {
                board.play(move);
                int value = alphaBeta(player.getOpponent(), board, false, alpha, beta, currentDepth + 1, maxDepth, startTime);
                board.undo();

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
                board.play(move);
                int value = alphaBeta(player.getOpponent(), board, true, alpha, beta, currentDepth + 1, maxDepth, startTime);
                board.undo();

                minScore = Math.min(minScore, value);

                if (minScore <= alpha) {
                    break;
                }

                beta = Math.min(beta, minScore);
            }
            return minScore;
        }
    }

    private boolean isTimeLimitExceeded(long startTime) {
        return System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS;
    }
}
