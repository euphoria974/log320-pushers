package log320;

import java.util.Comparator;

import static log320.Const.CHAR_TO_ROW;

public class MoveComparator implements Comparator<String> {
    final int[][] BOARD;
    final Player PLAYER;

    public MoveComparator(Board board, Player player) {
        this.BOARD = board.getBoard();
        this.PLAYER = player;
    }

    @Override
    public int compare(String m1, String m2) {
        return Integer.compare(definePriority(m1), definePriority(m2));
    }

    private int definePriority(String move) { // TODO : à peaufiner
        int fromRow = move.charAt(0) - CHAR_TO_ROW;
        int fromCol = Character.getNumericValue(move.charAt(1)) - 1;
        int toRow = move.charAt(2) - CHAR_TO_ROW;
        int toCol = Character.getNumericValue(move.charAt(3)) - 1;
        int movedPiece = BOARD[fromRow][fromCol];
        int dest = BOARD[toRow][toCol];

        int score = 0;

        // Près de la victoire
        if (toCol == PLAYER.getWinningCol()) score += 10000;
        else if (toCol == PLAYER.getWinningCol() - PLAYER.getForwardColumn()) score += 800;
        else if (toCol == PLAYER.getWinningCol() - 2 * PLAYER.getForwardColumn()) score += 500;

        // Capture
        if (dest == PLAYER.getOpponent().getPusher()) score += 1000;
        else if (dest == PLAYER.getOpponent().getPawn()) score += 200;

        // Se met devant un pusher
        int forwardCol = toCol + PLAYER.getForwardColumn();
        if (forwardCol < 8 && forwardCol > 0 && BOARD[toRow][forwardCol] == PLAYER.getOpponent().getPusher()) {
            score += 500;
        }

        // Contrôle le centre
        if (toRow >= 2 && toRow <= 5) score += 100;

        // Petit jouable
        if (movedPiece == PLAYER.getPawn()) {
            score += 30;
            int backCol = fromCol - PLAYER.getForwardColumn();
            if (backCol >= 0 && backCol < 8 && BOARD[fromRow][backCol] == PLAYER.getPusher()) {
                score += 50;
            }
        }

        // Safe après le coup
        if (isExposedAfterMove(toRow, toCol)) {
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
