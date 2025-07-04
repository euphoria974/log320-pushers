package log320.game;

import log320.entities.Move;
import log320.entities.Player;

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

        // Victoire
        if (move.isWinning()) {
            return Integer.MAX_VALUE;
        }

        // Capture
        if (destPiece == PLAYER.getOpponent().getPusher()) {
            score += 1000;
        } else if (destPiece == PLAYER.getOpponent().getPawn()) {
            score += 300;
        }

        // Exposé
        BOARD.play(move);
        if (BOARD.isExposed(PLAYER, move.getToRow(), move.getToCol())) {
            score -= 10000;
        }
        BOARD.undo();

        // Près de la victoire
        int distanceToWinningRow = Math.abs(move.getToRow() - PLAYER.getWinningRow());
        score += 10 * (7 - distanceToWinningRow);

        // pushers alignés
        if (move.getToRow() < 7 && move.getToRow() > 0) {
            if (BOARD.get(move.getToRow() + PLAYER.getDirection(), move.getToCol()) == PLAYER.getPusher()) {
                score += 100;
            }

            if (BOARD.get(move.getToRow() + PLAYER.getDirection(), move.getToCol()) == PLAYER.getOpponent().getPusher()) {
                score += 150;
            }
        }

        return score;
    }
}
