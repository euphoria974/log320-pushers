package log320.game;

import log320.entities.Move;
import log320.entities.Player;

import java.util.Comparator;

import static log320.Const.*;

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
        int toRow = toIndex / 8;
        int toCol = toIndex % 8;

        int destPiece = BOARD.getPieceFromIndex(toIndex);
        int score = 0;

        // Capture
        if (destPiece == PLAYER.getOpponent().getPusher()) {
            score += PUSHER_WEIGHT;
        } else if (destPiece == PLAYER.getOpponent().getPawn()) {
            score += PAWN_WEIGHT;
        }

        // Exposé
        if (BOARD.isExposed(PLAYER, toRow, toCol)) {
            score -= EXPOSED_PUSHER_WEIGHT;
        }

        // Près de la victoire
        int distanceToWinningRow = Math.abs(toRow - PLAYER.getWinningRow());
        score += DISTANCE_TO_WIN_WEIGHT * (7 - distanceToWinningRow);

        return score;
    }
}
