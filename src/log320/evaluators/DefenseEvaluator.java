package log320.evaluators;

import log320.Board;
import log320.Move;
import log320.Player;

import java.util.List;

import static log320.Const.*;

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

        // vérifie toutes les positions gagnantes d'abord
        for (int col = 0; col < 8; col++) {
            if (board.get(player.getWinningRow(), col) == player.getPawn()
                    || board.get(player.getWinningRow(), col) == player.getPusher())
                return WIN_SCORE;
            if (board.get(player.getOpponent().getWinningRow(), col) == player.getOpponent().getPawn()
                    || board.get(player.getOpponent().getWinningRow(), col) == player.getOpponent().getPusher())
                return LOSS_SCORE;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board.get(row, col) == player.getPusher()) {
                    playerPushers++;
                    score += 25;

                    if (board.canCapture(player, row, col)) {
                        score += 55;
                    }

                    if (row > 1 && row < 6 && board.get(row + player.getDirection() + 1, col) == player
                            .getOpponent().getPusher() && board.get(row + player.getDirection(), col) == EMPTY) {
                        score += 200;
                    }

                    if (board.isExposed(player, row, col)) {
                        score -= 130;
                    }

                    int distanceToWinningRow = 10 * (7 - Math.abs(row - player.getWinningRow()));
                    score += distanceToWinningRow;
                } else if (board.get(row, col) == player.getOpponent().getPusher()) {
                    opponentPushers++;
                    int distanceToWinningRow = 10 * (7 - Math.abs(row - player.getOpponent().getWinningRow()));
                    score -= distanceToWinningRow;
                } else if (board.get(row, col) == player.getPawn()) {
                    score += 10;

                    if (board.canCapture(player, row, col)) {
                        score += 100;
                    }

                    if (board.isExposed(player, row, col)) {
                        score -= 25;
                    }

                    if (board.isPawnActivated(player, row, col)) {
                        if (row > 1 && row < 6 && board.get(row + player.getDirection() + 1, col) == player.getOpponent().getPusher()
                                && board.get(row + player.getDirection(), col) == EMPTY) {
                            score += 200;
                        }

                        int distanceToWinningRow = 10 * (7 - Math.abs(row - player.getWinningRow()));
                        score += distanceToWinningRow;
                    }
                } else if (board.get(row, col) == player.getOpponent().getPawn()) {
                    if (board.isPawnActivated(player, row, col)) {
                        int distanceToWinningRow = 10 * (7 - Math.abs(row - player.getOpponent().getWinningRow()));
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
