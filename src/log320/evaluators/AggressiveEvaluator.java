package log320.evaluators;

import log320.Board;
import log320.Move;
import log320.Player;

import java.util.List;

import static log320.Const.LOSS_SCORE;
import static log320.Const.WIN_SCORE;
import static log320.Helper.countPotentialPushes;

public class AggressiveEvaluator implements IEvaluator {
    public int evaluate(Board board, Player player) {
        // TODO: retirer des points si des pions sont exposés
        
        int score = 0;
        int playerPushers = 0;
        int playerPawns = 0;
        int opponentPushers = 0;
        int opponentPawns = 0;

        for (int row = 0; row < 8; row++) {
            if (board.getBoard()[row][player.getWinningCol()] == player.getPawn() || board.getBoard()[row][player.getWinningCol()] == player.getPusher())
                return WIN_SCORE;
            if (board.getBoard()[row][player.getOpponent().getWinningCol()] == player.getOpponent().getPawn() || board.getBoard()[row][player.getOpponent().getWinningCol()] == player.getOpponent().getPusher())
                return LOSS_SCORE;

            for (int col = 0; col < 8; col++) {
                int bonusScore = player == Player.RED ? (col * col) / 10 : ((7 - col) * (7 - col)) / 10;
                if (board.getBoard()[row][col] == player.getPusher()) {
                    playerPushers++;
                    score += bonusScore;

                    if (col == player.getWinningCol() - 1) {
                        score += 5000;
                    }
                } else if (board.getBoard()[row][col] == player.getOpponent().getPusher()) {
                    opponentPushers++;
                    score -= bonusScore;
                } else if (board.getBoard()[row][col] == player.getPawn()) {
                    playerPawns++;
                    score += bonusScore;

                    if (col == player.getWinningCol() - 1) {
                        score += 5000;
                    }
                } else if (board.getBoard()[row][col] == player.getOpponent().getPawn()) {
                    opponentPawns++;
                    score -= bonusScore;
                }

                if ((board.getBoard()[row][col] == player.getPawn() || board.getBoard()[row][col] == player.getPusher()) && row >= 2 && row <= 5) {
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

        score += 20 * countPotentialPushes(board.getBoard(), player);

        return score;
    }
}
