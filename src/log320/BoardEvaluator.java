package log320;

import java.util.List;

public class BoardEvaluator {
    private final Board BOARD;

    public BoardEvaluator(Board board) {
        this.BOARD = board;
    }

    public int evaluate(Player player) {
        // Pour chaque pusher: +25
        // Pour chaque pusher adverse: -25
        // Pour chaque pion: +10
        // Pour chaque pion adverse: -10
        // Pusher en danger: -250
        // Pion en danger: -50
        // Pusher à 2 cases en avant: +1000
        // Distance au rang de victoire: +5 * distance pour les pushers

        int score = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (BOARD.get(row, col) == player.getPusher()) {
                    score += 25;

                    int checkRow = row + player.getDirection() * 2;
                    if (checkRow >= 0 && checkRow < 8 && BOARD.get(checkRow, col) == player.getOpponent().getPusher()) {
                        score += 1000;
                    }

                    score += 5 * Math.abs(row - player.getWinningRow());

                    if (BOARD.isExposed(player, row, col)) {
                        score -= 250;
                    }

                    if (BOARD.canCapturePusher(player, row, col)) {
                        score += 120;
                    }
                } else if (BOARD.get(row, col) == player.getOpponent().getPusher()) {
                    score -= 25;

                    int checkRow = row + player.getOpponent().getDirection() * 2;
                    if (checkRow >= 0 && checkRow < 8 && BOARD.get(checkRow, col) == player.getPusher()) {
                        score -= 1000;
                    }

                    score += 5 * Math.abs(row - player.getOpponent().getWinningRow());
                } else if (BOARD.get(row, col) == player.getPawn()) {
                    score += 10;

                    if (BOARD.isExposed(player, row, col)) {
                        score -= 50;
                    }

                    if (BOARD.canCapturePusher(player, row, col)) {
                        score += 120;
                    }
                } else if (BOARD.get(row, col) == player.getOpponent().getPawn()) {
                    score -= 10;
                }
            }
        }

        List<Move> myMoves = BOARD.getPossibleMoves(player);
        List<Move> opponentMoves = BOARD.getPossibleMoves(player.getOpponent());

        score += (myMoves.size() - opponentMoves.size()) * 4;

        return score;
    }
}
