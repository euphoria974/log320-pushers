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
        int movedPiece = BOARD.get(move.getFromRow(), move.getFromCol());
        int destPiece = BOARD.get(move.getToRow(), move.getToCol());

        int score = 0;

        if (move.getToRow() == PLAYER.getWinningRow()) {
            if (movedPiece == PLAYER.getPusher()) {
                return Integer.MAX_VALUE;
            } else if (isPawnActive(move)) {
                return Integer.MAX_VALUE;
            }
        }

        // Si tu peux manger un pusher adverse +20
        // Si tu peux manger un pion adverse +8
        // Si t'es pas safe après le coup -95
        // Si t'es proche de la victoire +6/colonne

        // Capture
        if (destPiece == PLAYER.getOpponent().getPusher()) {
            if (movedPiece == PLAYER.getPusher()) {
                score += 20;
            } else if (BOARD.get(move.getFromRow() - PLAYER.getDirection(), move.getFromCol()
                    - (move.getToCol() - move.getFromCol())) == PLAYER.getPusher()) {
                score += 20;
            }
        } else if (destPiece == PLAYER.getOpponent().getPawn()) {
            if (movedPiece == PLAYER.getPusher()) {
                score += 8;
            } else if (isPawnActive(move)) {
                score += 8;
            }
        }

        // Exposé
        if (BOARD.isExposed(PLAYER, move.getToRow(), move.getToCol())) {
            score -= 95;
        }

        // Près de la victoire
        score += 6 * Math.abs(move.getToRow() - PLAYER.getWinningRow());

        return score;
    }

    private boolean isPawnActive(Move move) {
        int direction = move.getToCol() - move.getFromCol();
        return BOARD.get(move.getFromRow() - PLAYER.getDirection(), move.getFromCol() - direction) == PLAYER.getPusher();
    }
}
