package log320.evaluators;

import log320.Board;
import log320.Move;
import log320.Player;

import java.util.List;

import static log320.Const.LOSS_SCORE;
import static log320.Const.WIN_SCORE;

public class AggressiveEvaluator implements IEvaluator {
    public int evaluate(Board board, Player player) {
        // TODO: retirer des points si des pions sont exposés
        
        int score = 0;
        int playerPushers = 0;
        int playerPawns = 0;
        int opponentPushers = 0;
        int opponentPawns = 0;

        // vérifie toutes les positions gagnantes d'abord
        for (int col = 0; col < 8; col++) {
            if (board.get(player.getWinningRow(), col) == player.getPawn() || board.get(player.getWinningRow(), col) == player.getPusher())
                return WIN_SCORE;
            if (board.get(player.getOpponent().getWinningRow(), col) == player.getOpponent().getPawn() || board.get(player.getOpponent().getWinningRow(), col) == player.getOpponent().getPusher())
                return LOSS_SCORE;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int bonusScore = player == Player.RED ? (row * row) / 10 : ((7 - row) * (7 - row)) / 10;
                if (board.get(row, col) == player.getPusher()) {
                    playerPushers++;
                    score += bonusScore;

                    if (row == player.getWinningRow() - 1) {
                        score += 5000;
                    }
                } else if (board.get(row, col) == player.getOpponent().getPusher()) {
                    opponentPushers++;
                    score -= bonusScore;
                } else if (board.get(row, col) == player.getPawn()) {
                    playerPawns++;
                    score += bonusScore;

                    if (row == player.getWinningRow() - 1) {
                        score += 5000;
                    }
                } else if (board.get(row, col) == player.getOpponent().getPawn()) {
                    opponentPawns++;
                    score -= bonusScore;
                }

                if ((board.get(row, col) == player.getPawn() || board.get(row, col) == player.getPusher()) && col >= 2 && col <= 5) {
                    score += 15;
                }
            }
        }

        if (opponentPushers == 0) {
            return WIN_SCORE;
        }

        if (playerPushers == 0) {
            return LOSS_SCORE;
        }

        score += (playerPushers * 300 + playerPawns * 100) - (opponentPushers * 300 + opponentPawns * 100);

        List<Move> myMoves = board.getPossibleMoves(player);
        List<Move> oppMoves = board.getPossibleMoves(player.getOpponent());
        score += (myMoves.size() - oppMoves.size()) * 10;

        score += 20 * board.countPotentialPushes(player);

        return score;
    }
}
