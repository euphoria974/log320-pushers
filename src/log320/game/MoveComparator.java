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
        // Victoire
        if (move.isWinning()) {
            return Integer.MAX_VALUE;
        }

        int toIndex = move.getTo();
        int toRow = toIndex >> 3; // div 8
        int toCol = toIndex & 7;  // mod 8

        int destPiece = BOARD.get(toIndex);
        int score = 0;

        // Capture
        if (destPiece == PLAYER.getOpponent().getPusher()) {
            score += 1000;
        } else if (destPiece == PLAYER.getOpponent().getPawn()) {
            score += 300;
        }

        // Exposé
        if (BOARD.isExposed(PLAYER, toRow, toCol)) {
            score -= 10000;
        }

        // Près de la victoire
        int distanceToWinningRow = Math.abs(toRow - PLAYER.getWinningRow());
        score += 10 * (7 - distanceToWinningRow);

        return score;
    }
}
