package log320.game;

import log320.entities.Player;

import static log320.Const.*;

public class BoardEvaluator {
    private final Board BOARD;

    private final int pusherScore = 10000;
    private final int pawnScore = 100;
    private final int capturePusherScore = 350;
    private final int capturePawnScore = 30;
    private final int exposedPusherScore = -100000;
    private final int exposedPawnScore = -200;
    private final int faceToFacePusherScore = 200;
    private final int pusherDistanceToWinningRowScore = 10;
    private final int pawnDistanceToWinningRowScore = 8;
    private final int movesDifferenceScore = 5;
    private final int firstRowProtectionScore = 750;
    private final int secondRowProtectionScore = 550;
    private final int thirdRowProtectionScore = 275;

    public BoardEvaluator(Board board) {
        this.BOARD = board;
    }

    // TODO différence de pushers dans la moitié du plateau haut/bas

    public int evaluate(Player player) {
        if (BOARD.hasPlayerWon(player)) {
            return WIN_SCORE;
        }

        if (BOARD.hasPlayerWon(player.getOpponent())) {
            return LOSS_SCORE;
        }

        int score = 0;

        score += evaluateFeatures(player);

        // défense des 3 premières rangées
        score += evaluateRowProtection(player);

        return score;
    }

    private int evaluateFeatures(Player player) {
        int score = 0;
        long pushers = player == Player.RED ? BOARD.getRedPushers() : BOARD.getBlackPushers();

        score += pusherScore * Long.bitCount(pushers);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = BOARD.get(row, col);

                if (piece == player.getPusher()) {
                    if (BOARD.canEat(player, player.getOpponent().getPusher(), row, col)) score += capturePusherScore;
                    if (BOARD.canEat(player, player.getOpponent().getPawn(), row, col)) score += capturePawnScore;

                    if (row > 1 && row < 6 && BOARD.get(row + player.getDirection() + player.getDirection(), col) == player.getOpponent().getPusher() && BOARD.get(row + player.getDirection(), col) == EMPTY) {
                        score += faceToFacePusherScore;
                    }

                    if (BOARD.isExposed(player, row, col)) score += exposedPusherScore;

                    int distanceToWinningRow = pusherDistanceToWinningRowScore * Math.abs(row - player.getWinningRow());
                    score += distanceToWinningRow;
                } else if (piece == player.getPawn()) {
                    score += pawnScore;

                    if (BOARD.isExposed(player, row, col)) score += exposedPawnScore;

                    if (BOARD.isPawnActivated(player, row, col)) {
                        if (BOARD.canEat(player, player.getOpponent().getPusher(), row, col))
                            score += capturePusherScore;
                        if (BOARD.canEat(player, player.getOpponent().getPawn(), row, col)) score += capturePawnScore;

                        int distanceToWinningRow = pawnDistanceToWinningRowScore * Math.abs(row - player.getWinningRow());
                        score += distanceToWinningRow;
                    }
                }
            }
        }

        return score;
    }

    private int evaluateRowProtection(Player player) {
        int score = 0;
        int firstRow = player.getOpponent().getWinningRow() + player.getDirection();
        int secondRow = firstRow + player.getDirection();
        int thirdRow = secondRow + player.getDirection();

        if (BOARD.isRowCovered(player, firstRow)) score += firstRowProtectionScore;
        if (BOARD.isRowCovered(player, secondRow)) score += secondRowProtectionScore;
        if (BOARD.isRowCovered(player, thirdRow)) score += thirdRowProtectionScore;

        return score;
    }
}