package log320;

import java.util.ArrayList;
import java.util.List;

import static log320.Const.*;

public class CPUPlayer {
    private final Board BOARD;
    private final Player PLAYER;

    public CPUPlayer(Board board, Player player) {
        this.BOARD = board;
        this.PLAYER = player;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public ArrayList<String> getNextMove() {
        long startTime = System.currentTimeMillis();
        ArrayList<String> bestMoves = new ArrayList<>();
        ArrayList<String> currentBest = new ArrayList<>();
        int depth = 0;

        while (System.currentTimeMillis() - startTime < MAX_TIME_MILLIS) {
            currentBest.clear();
            int bestScore = Integer.MIN_VALUE;

            for (String move : BOARD.getPossibleMoves(PLAYER)) {
                BOARD.play(move);
                int score = alphaBeta(PLAYER.getOpponent(), false, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, startTime);
                BOARD.undo();

                if (score > bestScore) {
                    bestScore = score;
                    currentBest.clear();
                    currentBest.add(move);
                } else if (score == bestScore) {
                    currentBest.add(move);
                }
            }

            if (System.currentTimeMillis() - startTime < MAX_TIME_MILLIS) {
                bestMoves.clear();
                bestMoves.addAll(currentBest);
            }

            depth++;
        }

        return bestMoves;
    }

    private int alphaBeta(Player player, boolean isMax, int alpha, int beta, int dept, long startTime) {
        if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
            return MAX_TIME_SCORE;
        }

        List<String> possibleMoves = BOARD.getPossibleMoves(player);

        int score = BOARD.evaluate(PLAYER);

        if (score == WIN_SCORE || score == LOSS_SCORE || possibleMoves.isEmpty() || dept >= MAX_DEPTH) {
            return score;
        }

        if (isMax) {
            int maxScore = Integer.MIN_VALUE;

            for (String move : possibleMoves) {
                if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
                    return MAX_TIME_SCORE;
                }

                BOARD.play(move);
                int value = alphaBeta(player.getOpponent(), false, alpha, beta, dept + 1, startTime);
                BOARD.undo();

                // Optimisation: early cutoff
                // TODO voir si cette optimisation est pertinente
                /*if (value == WIN_SCORE) {
                    return value;
                }*/

                maxScore = Math.max(maxScore, value);

                if (maxScore >= beta) {
                    break;
                }

                alpha = Math.max(alpha, maxScore);
            }

            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;

            for (String move : possibleMoves) {
                if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
                    return MAX_TIME_SCORE;
                }

                BOARD.play(move);
                int value = alphaBeta(player.getOpponent(), true, alpha, beta, dept + 1, startTime);
                BOARD.undo();

                // Optimisation: early cutoff
                // TODO voir si cette optimisation est pertinente
                /*if (value == LOSS_SCORE) {
                    return value;
                }*/

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
