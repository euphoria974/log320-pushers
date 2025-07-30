package log320;

import java.util.List;

import static log320.Const.*;

public class BoardEvaluator {
    private final Board BOARD;

    // Bonus topologique : favorise les bords (A-B et G-H)
    private static final int[] COLUMN_BONUS = { 4, 2, 0, 0, 0, 0, 2, 4 };

    public BoardEvaluator(Board board) {
        this.BOARD = board;
    }

    public int evaluate(Player player) {
        int score = 0;
        int playerPushers = 0;
        int opponentPushers = 0;

        for (int col = 0; col < 8; col++) {
            // Victoire immédiate ?
            if (BOARD.get(player.getWinningRow(), col) == player.getPawn()
                    || BOARD.get(player.getWinningRow(), col) == player.getPusher())
                return WIN_SCORE;

            if (BOARD.get(player.getOpponent().getWinningRow(), col) == player.getOpponent().getPawn()
                    || BOARD.get(player.getOpponent().getWinningRow(), col) == player.getOpponent().getPusher())
                return LOSS_SCORE;

            for (int row = 0; row < 8; row++) {
                int cell = BOARD.get(row, col);

                // Pousseur du joueur
                if (cell == player.getPusher()) {
                    playerPushers++;
                    score += 25;
                    score += COLUMN_BONUS[col];  // Bonus bord

                    if (row > 1 && row < 6
                            && BOARD.get(row + player.getDirection() + player.getDirection(), col) == player.getOpponent().getPusher()
                            && BOARD.get(row + player.getDirection(), col) == EMPTY) {
                        score += 200; // blocage utile
                    }

                    if (BOARD.isExposed(player, row, col)) {
                        score -= 130;
                    }

                    int distanceToWinningRow = 10 * Math.abs(row - player.getWinningRow());
                    score += distanceToWinningRow;
                }

                // Pousseur adverse
                else if (cell == player.getOpponent().getPusher()) {
                    opponentPushers++;
                    int distanceToWinningRow = 10 * Math.abs(row - player.getOpponent().getWinningRow());
                    score -= distanceToWinningRow;
                }

                // Pion du joueur
                else if (cell == player.getPawn()) {
                    score += 10;
                    score += COLUMN_BONUS[col];

                    if (BOARD.isExposed(player, row, col)) {
                        score -= 25;
                    }

                    if (BOARD.isPawnActivated(player, row, col)) {
                        boolean weakColumn = true;
                        for (int r = row + player.getDirection(); r != row + 4 * player.getDirection(); r += player.getDirection()) {
                            if (r < 0 || r >= 8) break;
                            int enemy = BOARD.get(r, col);
                            if (enemy == player.getOpponent().getPawn() || enemy == player.getOpponent().getPusher()) {
                                weakColumn = false;
                                break;
                            }
                        }
                        if (weakColumn) {
                            score += 100; // Percée possible ! exploiter la faiblesse
                        }
                    }


                    // Pénalité souple pour pion isolé (plus doux qu'avant)
                    boolean hasNearbyPusher = false;
                    for (int dRow = 1; dRow <= 2; dRow++) {
                        int checkRow = row - player.getDirection() * dRow;
                        for (int dCol = -1; dCol <= 1; dCol++) {
                            int checkCol = col + dCol;
                            if (checkRow >= 0 && checkRow < 8 && checkCol >= 0 && checkCol < 8) {
                                if (BOARD.get(checkRow, checkCol) == player.getPusher()) {
                                    hasNearbyPusher = true;
                                    break;
                                }
                            }
                        }
                        if (hasNearbyPusher) break;
                    }
                    if (!hasNearbyPusher) {
                        score -= 5; // Poussé légèrement isolé
                    }
                }

                // Pion adverse activé
                else if (cell == player.getOpponent().getPawn()) {
                    if (BOARD.isPawnActivated(player, row, col)) {
                        int distanceToWinningRow = 8 * Math.abs(row - player.getOpponent().getWinningRow());
                        score -= distanceToWinningRow;
                    }
                }
            }
        }

        if (opponentPushers == 0) return WIN_SCORE;
        if (playerPushers == 0) return LOSS_SCORE;

        score += 100 * (playerPushers - opponentPushers);

        List<Move> myMoves = BOARD.getPossibleMoves(player);
        List<Move> opponentMoves = BOARD.getPossibleMoves(player.getOpponent());
        score += (myMoves.size() - opponentMoves.size()) * 3;

        if (BOARD.isRowCovered(player, player.getOpponent().getWinningRow() + player.getDirection())) {
            score += 900;
        }

        if (BOARD.isRowCovered(player, player.getOpponent().getWinningRow() + 2 * player.getDirection())) {
            score += 700;
        }

        if (BOARD.isRowCovered(player, player.getOpponent().getWinningRow() + 3 * player.getDirection())) {
            score += 300;
        }

        return score;
    }
}
