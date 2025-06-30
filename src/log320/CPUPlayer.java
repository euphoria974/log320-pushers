package log320;

import java.util.Collections;
import java.util.List;

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
        Move bestMove = null;

        // look for immediate winning moves
        Move winningMove = possibleMoves.stream().filter(Move::isWinning).findAny().orElse(null);
        if (winningMove != null) {
            return winningMove;
        }

        while (System.currentTimeMillis() - startTime < MAX_TIME_MILLIS) {
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
            Collections.sort(possibleMoves, Collections.reverseOrder());

            maxDepth += 1;
        }

        return bestMove;
    }

    private int negamax(Player player, int alpha, int beta, int currentDepth, int maxDepth, long startTime) {
        if (isTimeLimitExceeded(startTime) || currentDepth >= maxDepth || BOARD.isGameOver()) {
            return BOARD.evaluate(player);
        }

        List<Move> possibleMoves = BOARD.getPossibleMoves(player);
        if (possibleMoves.isEmpty()) {
            return BOARD.evaluate(player);
        }

        Move winningMove = possibleMoves.stream().filter(Move::isWinning).findAny().orElse(null);
        if (winningMove != null) {
            return player == PLAYER ? WIN_SCORE : LOSS_SCORE;
        }

        int value = Integer.MIN_VALUE;

        for (Move move : possibleMoves) {
            BOARD.play(move);
            // https://en.wikipedia.org/wiki/Negamax
            int score = -negamax(player.getOpponent(), -beta, -alpha, currentDepth + 1, maxDepth, startTime);
            BOARD.undo();

            value = Math.max(value, score);
            alpha = Math.max(alpha, value);

            if (value >= beta) {
                break;
            }
        }

        return value;
    }

    private boolean isTimeLimitExceeded(long startTime) {
        return System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS;
    }
}
