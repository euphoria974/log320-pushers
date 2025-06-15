package log320;

import java.util.Comparator;

public class MoveComparator implements Comparator<String> {
    final int[][] BOARD;
    final int PLAYER;

    public MoveComparator(Board board, int player) {
        this.BOARD = board.getBoard();
        this.PLAYER = player;
    }

    @Override
    public int compare(String m1, String m2) {
        return Integer.compare(definePriority(m1), definePriority(m2));
    }

    // Centre
    // Coins
    // Le reste
    private int definePriority(String move) { // TODO : à peaufiner
        int fromRow = move.charAt(0) - 65;
        int fromCol = Character.getNumericValue(move.charAt(1)) - 1;
        int toRow = move.charAt(2) - 65;
        int toCol = Character.getNumericValue(move.charAt(3)) - 1;
        int movedPiece = BOARD[fromRow][fromCol];
        int dest = BOARD[toRow][toCol];

        int score = 0;

        if ((PLAYER == 3 && toCol == 7) || (PLAYER == 1 && toCol == 0)) {
            score += 10000;
        }

        score += (PLAYER == 3) ? (toCol - fromCol) * 10 : (fromCol - toCol) * 10;

        if (((PLAYER == 3 && (dest == 1 || dest == 2)) || (PLAYER == 1 && (dest == 3 || dest == 4)))) {
            score += 500;
        }

        if (movedPiece == PLAYER) {
            score += 30;
        }

        return score;
    }
}
