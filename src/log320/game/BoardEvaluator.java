package log320.game;

import log320.entities.Move;
import log320.entities.Player;

import java.util.List;

import static log320.Const.*;

public class BoardEvaluator {
    private final Board BOARD;

    public BoardEvaluator(Board board) {
        this.BOARD = board;
    }

    public int evaluate(Player player) {
        // Peux manger safement un pusher: +120
        // Peux manger safement un pion: +60
        // Peux manger mais perte d'un pion: +50
        // Peux manger mais perte d'un pusher: -60
        // Pion en danger mais peux manger: +130 (sacrifice)
        // Bloque un pusher adverse: +200

        int score = 0;
        int playerPushers = 0;
        int opponentPushers = 0;

        for (int col = 0; col < 8; col++) {
            if (BOARD.get(player.getWinningRow(), col) == player.getPawn() || BOARD.get(player.getWinningRow(), col) == player.getPusher())
                return WIN_SCORE;
            if (BOARD.get(player.getOpponent().getWinningRow(), col) == player.getOpponent().getPawn() || BOARD.get(player.getOpponent().getWinningRow(), col) == player.getOpponent().getPusher())
                return LOSS_SCORE;

            for (int row = 0; row < 8; row++) {
                if (BOARD.get(row, col) == player.getPusher()) {
                    playerPushers++;
                    score += 25;

                    /* TODO
                    if (BOARD.canEat(getBoard(), player, row, col)) {
                        score += 55;
                    } */

                    if (row > 1 && row < 6 && BOARD.get(row + player.getDirection() + player.getDirection(), col) == player.getOpponent().getPusher() && BOARD.get(row + player.getDirection(), col) == EMPTY) {
                        score += 200;
                    }

                    if (BOARD.isExposed(player, row, col)) {
                        score -= 130;
                    }

                    int distanceToWinningRow = 10 * Math.abs(row - player.getWinningRow());
                    score += distanceToWinningRow;
                } else if (BOARD.get(row, col) == player.getOpponent().getPusher()) {
                    opponentPushers++;
                    int distanceToWinningRow = 10 * Math.abs(row - player.getOpponent().getWinningRow());
                    score -= distanceToWinningRow;
                } else if (BOARD.get(row, col) == player.getPawn()) {
                    score += 10;

                    /* TODO
                    if (BOARD.canEat(player, row, col)) {
                        score += 100;
                    }
                     */

                    if (BOARD.isExposed(player, row, col)) {
                        score -= 25;
                    }

                    if (BOARD.isPawnActivated(player, row, col)) {
                        int distanceToWinningRow = 8 * Math.abs(row - player.getWinningRow());
                        score += distanceToWinningRow;
                    }
                } else if (BOARD.get(row, col) == player.getOpponent().getPawn()) {
                    if (BOARD.isPawnActivated(player, row, col)) {
                        int distanceToWinningRow = 8 * Math.abs(row - player.getOpponent().getWinningRow());
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

        score += 100 * (playerPushers - opponentPushers);

        List<Move> myMoves = BOARD.getPossibleMoves(player);
        List<Move> opponentMoves = BOARD.getPossibleMoves(player.getOpponent());

        score += (myMoves.size() - opponentMoves.size()) * 3;

        if (BOARD.isRowCovered(player, player.getOpponent().getWinningRow() + player.getDirection())) {
            score += 900;
        }

        if (BOARD.isRowCovered(player, player.getOpponent().getWinningRow() + player.getDirection() + player.getDirection())) {
            score += 700;
        }

        if (BOARD.isRowCovered(player, player.getOpponent().getWinningRow() + player.getDirection() + player.getDirection() + player.getDirection())) {
            score += 300;
        }

        return score;
    }
}
