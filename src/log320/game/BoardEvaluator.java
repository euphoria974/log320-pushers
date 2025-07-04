package log320.game;

import log320.entities.Move;
import log320.entities.Player;

import java.util.List;

import static log320.Const.*;

public class BoardEvaluator {
    private final Board BOARD;

    private final int pusherScore = 25;
    private final int pawnScore = 10;
    private final int capturePusherScore = 150;
    private final int capturePawnScore = 30;
    private final int exposedPusherScore = -500;
    private final int exposedPawnScore = -50;
    private final int faceToFacePusherScore = 200;
    private final int pusherDistanceToWinningRowScore = 10;
    private final int pawnDistanceToWinningRowScore = 8;
    private final int pushersDifferenceScore = 100;
    private final int movesDifferenceScore = 3;
    private final int firstRowProtectionScore = 900;
    private final int secondRowProtectionScore = 700;
    private final int thirdRowProtectionScore = 350;

    public BoardEvaluator(Board board) {
        this.BOARD = board;
    }

    public int evaluate(Player player) {

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
                    score += pusherScore;

                    if (BOARD.canEat(player, player.getOpponent().getPusher(), row, col)) {
                        score += capturePusherScore;
                    }

                    if (BOARD.canEat(player, player.getOpponent().getPawn(), row, col)) {
                        score += capturePawnScore;
                    }

                    if (row > 1 && row < 6 && BOARD.get(row + player.getDirection() + player.getDirection(), col) == player.getOpponent().getPusher() && BOARD.get(row + player.getDirection(), col) == EMPTY) {
                        score += faceToFacePusherScore;
                    }

                    if (BOARD.isExposed(player, row, col)) {
                        score += exposedPusherScore;
                    }

                    int distanceToWinningRow = pusherDistanceToWinningRowScore * Math.abs(row - player.getWinningRow());
                    score += distanceToWinningRow;
                } else if (BOARD.get(row, col) == player.getOpponent().getPusher()) {
                    opponentPushers++;
                    int distanceToWinningRow = pusherDistanceToWinningRowScore * Math.abs(row - player.getOpponent().getWinningRow());
                    score -= distanceToWinningRow;
                } else if (BOARD.get(row, col) == player.getPawn()) {
                    score += pawnScore;

                    if (BOARD.isExposed(player, row, col)) {
                        score += exposedPawnScore;
                    }

                    if (BOARD.isPawnActivated(player, row, col)) {
                        if (BOARD.canEat(player, player.getOpponent().getPusher(), row, col)) {
                            score += capturePusherScore;
                        }

                        if (BOARD.canEat(player, player.getOpponent().getPawn(), row, col)) {
                            score += capturePawnScore;
                        }

                        int distanceToWinningRow = pawnDistanceToWinningRowScore * Math.abs(row - player.getWinningRow());
                        score += distanceToWinningRow;
                    }
                } else if (BOARD.get(row, col) == player.getOpponent().getPawn()) {
                    if (BOARD.isPawnActivated(player, row, col)) {
                        int distanceToWinningRow = pawnDistanceToWinningRowScore * Math.abs(row - player.getOpponent().getWinningRow());
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

        score += pushersDifferenceScore * (playerPushers - opponentPushers);

        List<Move> myMoves = BOARD.getPossibleMoves(player);
        List<Move> opponentMoves = BOARD.getPossibleMoves(player.getOpponent());

        score += (myMoves.size() - opponentMoves.size()) * movesDifferenceScore;

        if (BOARD.isRowCovered(player, player.getOpponent().getWinningRow() + player.getDirection())) {
            score += firstRowProtectionScore;
        }

        if (BOARD.isRowCovered(player, player.getOpponent().getWinningRow() + player.getDirection() + player.getDirection())) {
            score += secondRowProtectionScore;
        }

        if (BOARD.isRowCovered(player, player.getOpponent().getWinningRow() + player.getDirection() + player.getDirection() + player.getDirection())) {
            score += thirdRowProtectionScore;
        }

        return score;
    }
}
