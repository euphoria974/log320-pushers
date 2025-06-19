package log320;

import java.util.Comparator;

import static log320.Helper.isExposed;

public class MoveComparator implements Comparator<Move> {
    final int[][] BOARD;
    final Player PLAYER;

    public MoveComparator(Board board, Player player) {
        this.BOARD = board.getBoard();
        this.PLAYER = player;
    }

    @Override
    public int compare(Move m1, Move m2) {
        return Integer.compare(definePriority(m1), definePriority(m2));
    }

    private int definePriority(Move move) {
        int movedPiece = BOARD[move.getFromRow()][move.getFromCol()];
        int destPiece = BOARD[move.getToRow()][move.getToCol()];

        int score = 0;

        // Si tu peux manger un pusher adverse +20
        // Si tu peux manger un pion adverse +8
        // Si t'es pas safe après le coup -95
        // Si t'es proche de la victoire +6/colonne

        // Capture
        if (destPiece == PLAYER.getOpponent().getPusher()) {
            if (movedPiece == PLAYER.getPusher()) {
                score += 20;
            } else if (BOARD[move.getFromRow() - (move.getToRow() - move.getFromRow())][move.getFromCol() - PLAYER.getForwardColumn()] == PLAYER.getPusher()) {
                score += 20;
            }
        } else if (destPiece == PLAYER.getOpponent().getPawn()) {
            if (movedPiece == PLAYER.getPusher()) {
                score += 8;
            } else if (BOARD[move.getFromRow() - (move.getToRow() - move.getFromRow())][move.getFromCol() - PLAYER.getForwardColumn()] == PLAYER.getPusher()) {
                score += 8;
            }
        }

        // Exposé
        if (isExposed(BOARD, PLAYER, move.getToRow(), move.getToCol())) {
            score -= 95;
        }

        // Près de la victoire
        score += 6 * Math.abs(move.getToCol() - PLAYER.getWinningCol());

        return score;
    }
}
