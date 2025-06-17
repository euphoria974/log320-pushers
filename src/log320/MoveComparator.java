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

    private int definePriority(Move move) { // TODO : à peaufiner
        int movedPiece = BOARD[move.getFromRow()][move.getFromCol()];
        int dest = BOARD[move.getToRow()][move.getToCol()];

        int score = 0;

        // Près de la victoire
        if (move.getToCol() == PLAYER.getWinningCol()) score += 10000;
        else if (move.getToCol() == PLAYER.getWinningCol() - PLAYER.getForwardColumn()) score += 800;
        else if (move.getToCol() == PLAYER.getWinningCol() - 2 * PLAYER.getForwardColumn()) score += 500;

        // Capture
        if (dest == PLAYER.getOpponent().getPusher()) score += 1000;
        else if (dest == PLAYER.getOpponent().getPawn()) score += 200;

        // Se met devant un pusher
        int forwardCol = move.getToCol() + PLAYER.getForwardColumn();
        if (forwardCol < 8 && forwardCol > 0 && BOARD[move.getToRow()][forwardCol] == PLAYER.getOpponent().getPusher()) {
            score += 500;
        }

        // Contrôle le centre
        if (move.getToRow() >= 2 && move.getToRow() <= 5) score += 100;

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
            score -= 300;
        } else {
            score += 200;
        }

        int playerPawnCount = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (BOARD[r][c] == PLAYER.getPawn()) playerPawnCount++;
            }
        }

        if (movedPiece == PLAYER.getPawn() && playerPawnCount > 4) {
            score += 300;
        }

        if (movedPiece == PLAYER.getPusher() && playerPawnCount > 4) {
            score -= 300;
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
