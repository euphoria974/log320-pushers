package log320;

public class Helper {
    public static int countPotentialPushes(int[][] board, Player player) {
        int count = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = player == Player.RED ? 0 : 1; col < (player == Player.RED ? 7 : 8); col++) {
                if (board[row][col] == player.getPusher()) {
                    int piece = board[row][col + player.getForwardColumn()];
                    if (piece == player.getPawn()) {
                        count++;
                    }

                    if (row > 0) {
                        piece = board[row - 1][col + player.getForwardColumn()];
                        if (piece == player.getPawn()) {
                            count++;
                        }
                    }

                    if (row < 7) {
                        piece = board[row + 1][col + player.getForwardColumn()];
                        if (piece == player.getPawn()) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    public static boolean isPawnActivated(int[][] board, Player player, int row, int col) {
        for (int d = -1; d <= 1; d++) {
            if (row > 0 && row < 7 && board[row + d][col - player.getForwardColumn()] == player.getPusher()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isExposed(int[][] board, Player player, int toRow, int toCol) {
        int opponentForward = player.getOpponent().getForwardColumn();
        int playerForward = player.getForwardColumn();

        int[][] threatDiagonals = {
                {-1, opponentForward},
                {1, opponentForward}
        };
        boolean threatened = false;
        for (int[] dir : threatDiagonals) {
            int row = toRow + dir[0];
            int col = toCol + dir[1];
            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                if (board[row][col] == player.getOpponent().getPusher()) {
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
                if (board[row][col] == player.getPusher()) {
                    protectedByPusher = true;
                    break;
                }
            }
        }

        return threatened && !protectedByPusher;
    }

    public static boolean canEat(int[][] board, Player player, int r, int c) {
        int playerForward = player.getForwardColumn();

        int[][] threatDiagonals = {
                {-1, playerForward},
                {1, playerForward}
        };

        int[][] protectDiagonals = {
                {-1, -playerForward},
                {1, -playerForward}
        };

        for (int[] direction : threatDiagonals) {
            int row = r + direction[0];
            int col = c + direction[1];

            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                if (board[row][col] == player.getOpponent().getPawn() || board[row][col] == player.getOpponent().getPusher()) {
                    if (board[r][c] == player.getPusher()) {
                        return true;
                    }

                    for (int[] dir : protectDiagonals) {
                        int ro = r + dir[0];
                        int co = c + dir[1];

                        if (ro >= 0 && ro < 8 && co >= 0 && co < 8) {
                            if (board[ro][co] == player.getPusher()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
