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

        if (toCol == PLAYER.getWinningCol()) {
            score += 10000;
        }

        score += (PLAYER == Player.RED) ? (toCol - fromCol) * 10 : (fromCol - toCol) * 10;

        if (dest == PLAYER.getOpponent().getPawn() || dest == PLAYER.getOpponent().getPusher()) {
            score += 500;
        }

        if (movedPiece == PLAYER.getPawn()) {
            score += 30;
        }

        return score;
    }
}
