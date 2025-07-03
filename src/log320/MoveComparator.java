package log320;

import java.util.Comparator;

public class MoveComparator implements Comparator<Move> {
    private final Board BOARD;
    private final Player PLAYER;

    public MoveComparator(Board board, Player player) {
        this.BOARD = board;
        this.PLAYER = player;
    }

    @Override
    public int compare(Move m1, Move m2) {
        return Integer.compare(getMoveScore(m2), getMoveScore(m1));
    }

    public int getMoveScore(Move move) {
        int destPiece = BOARD.get(move.getToRow(), move.getToCol());

        int score = 0;

        if (move.isWinning()) {
            return Integer.MAX_VALUE;
        }

        // Si tu peux manger un pusher adverse +20
        // Si tu peux manger un pion adverse +8
        // Si t'es pas safe après le coup -95
        // Si t'es proche de la victoire +6/colonne

        // Capture
        if (destPiece == PLAYER.getOpponent().getPusher()) {
            score += 30;
        } else if (destPiece == PLAYER.getOpponent().getPawn()) {
            score += 8;
        }

        // Exposé
        BOARD.play(move);
        if (BOARD.isExposed(PLAYER, move.getToRow(), move.getToCol())) {
            score -= 95;
        }
        BOARD.undo();

        // Près de la victoire
        int distanceToWinningRow = Math.abs(move.getToRow() - PLAYER.getWinningRow());
        score += 3 * (7 - distanceToWinningRow);

        return score;
    }
}
