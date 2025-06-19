package log320.evaluators;

import log320.Board;
import log320.Move;
import log320.Player;

import java.util.List;

import static log320.Const.*;
import static log320.Helper.*;

public class DefenseEvaluator implements IEvaluator {
    public int evaluate(Board board, Player player) {
        // Peux manger safement un pusher: +120
        // Peux manger safement un pion: +60
        // Peux manger mais perte d'un pion: +50
        // Peux manger mais perte d'un pusher: -60
        // Pion en danger mais peux manger: +130 (sacrifice)
        // Bloque un pusher adverse: +200

        int score = 0;
        int playerPushers = 0;
        int opponentPushers = 0;

        for (int row = 0; row < 8; row++) {
            if (board.getBoard()[row][player.getWinningCol()] == player.getPawn() || board.getBoard()[row][player.getWinningCol()] == player.getPusher())
                return WIN_SCORE;
            if (board.getBoard()[row][player.getOpponent().getWinningCol()] == player.getOpponent().getPawn() || board.getBoard()[row][player.getOpponent().getWinningCol()] == player.getOpponent().getPusher())
                return LOSS_SCORE;

            for (int col = 0; col < 8; col++) {
                if (board.getBoard()[row][col] == player.getPusher()) {
                    playerPushers++;
                    score += 25;

                    if (canEat(board.getBoard(), player, row, col)) {
                        score += 55;
                    }

                    if (col > 1 && col < 6 && board.getBoard()[row][col + player.getForwardColumn() + 1] == player.getOpponent().getPusher() && board.getBoard()[row][col + player.getForwardColumn()] == EMPTY) {
                        score += 200;
                    }

                    if (isExposed(board.getBoard(), player, row, col)) {
                        score -= 130;
                    }

                    int distanceToWinningRow = 10 * (7 - Math.abs(col - player.getWinningCol()));
                    score += distanceToWinningRow;
                } else if (board.getBoard()[row][col] == player.getOpponent().getPusher()) {
                    opponentPushers++;
                    int distanceToWinningRow = 10 * (7 - Math.abs(col - player.getOpponent().getWinningCol()));
                    score -= distanceToWinningRow;
                } else if (board.getBoard()[row][col] == player.getPawn()) {
                    score += 10;

                    if (canEat(board.getBoard(), player, row, col)) {
                        score += 100;
                    }

                    if (isExposed(board.getBoard(), player, row, col)) {
                        score -= 25;
                    }

                    if (isPawnActivated(board.getBoard(), player, row, col)) {
                        if (col > 1 && col < 6 && board.getBoard()[row][col + player.getForwardColumn() + 1] == player.getOpponent().getPusher() && board.getBoard()[row][col + player.getForwardColumn()] == EMPTY) {
                            score += 200;
                        }

                        int distanceToWinningRow = 10 * (7 - Math.abs(col - player.getWinningCol()));
                        score += distanceToWinningRow;
                    }
                } else if (board.getBoard()[row][col] == player.getOpponent().getPawn()) {
                    if (isPawnActivated(board.getBoard(), player, row, col)) {
                        int distanceToWinningRow = 10 * (7 - Math.abs(col - player.getOpponent().getWinningCol()));
                        score -= distanceToWinningRow;
                    }
                }
            }
        }

        if (opponentPushers == 0) {
            return WIN_SCORE;
        }

        if (playerPushers == 0) {
            return LOSS_SCORE;
        }

        score += 25 * (playerPushers - opponentPushers);

        List<Move> myMoves = board.getPossibleMoves(player);
        List<Move> opponentMoves = board.getPossibleMoves(player.getOpponent());

        score += Math.abs(myMoves.size() - opponentMoves.size()) * 3;

        return score;
    }
}
