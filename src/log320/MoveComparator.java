package log320;

import java.util.Comparator;

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

        // Près de la victoire
        score += 120 * (7 - Math.abs(move.getToCol() - PLAYER.getWinningCol()));

        // Capture
        if (destPiece == PLAYER.getOpponent().getPusher()) score += 100;
        else if (destPiece == PLAYER.getOpponent().getPawn()) score += 25;

        // Petit jouable
        if (movedPiece == PLAYER.getPawn()) {
            score += 30;
            int backCol = move.getFromCol() - PLAYER.getForwardColumn();
            if (backCol >= 0 && backCol < 8 && BOARD[move.getFromRow()][backCol] == PLAYER.getPusher()) {
                score += 50;
            }
        }

        // Safe après le coup
        if (isExposedAfterMove(move.getToRow(), move.getToCol())) {
            score -= 95;
        } else {
            score += 50;
        }

        return score;
    }

    private boolean isExposedAfterMove(int toRow, int toCol) {
        int opponentForward = PLAYER.getOpponent().getForwardColumn();
        int playerForward = PLAYER.getForwardColumn();

        int[][] threatDiagonals = {
                {-1, opponentForward},
                {1, opponentForward}
        };
        boolean threatened = false;
        for (int[] dir : threatDiagonals) {
            int row = toRow + dir[0];
            int col = toCol + dir[1];
            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                if (BOARD[row][col] == PLAYER.getOpponent().getPusher()) {
                    threatened = true;
                    break;
                }
            }
        }

        int[][] protectDiagonals = {
                {-1, -playerForward},
                {1, -playerForward}
        };
        boolean protectedByPusher = false;

        for (int[] dir : protectDiagonals) {
            int row = toRow + dir[0];
            int col = toCol + dir[1];
            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                if (BOARD[row][col] == PLAYER.getPusher()) {
                    protectedByPusher = true;
                    break;
                }
            }
        }

        return threatened && !protectedByPusher;
    }
}
