package log320;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static log320.Const.WIN_SCORE;

public class Board {
    private final MoveComparator MOVE_COMPARATOR;
    private final int[][] BOARD = new int[8][8];
    private final Stack<MoveState> MOVE_STACK = new Stack<>();

    private String lastMove = "";

    public Board(String s, int player) {
        build(s);
        MOVE_COMPARATOR = new MoveComparator(this, player);
    }

    public int[][] getBoard() {
        return BOARD;
    }

    public String getLastMove() {
        return lastMove;
    }

    public void build(String s) {
        List<String> boardValues = Arrays.asList(s.split(" ")).reversed();

        int row = 0, col = 0;
        for (String boardValue : boardValues) {
            BOARD[row][col] = Integer.parseInt(boardValue);
            row++;
            if (row == 8) {
                col++;
                row = 0;
            }
        }
    }

    public void reset() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (j == 0) {
                    BOARD[i][j] = 4;
                } else if (j == 1) {
                    BOARD[i][j] = 3;
                } else if (j == 6) {
                    BOARD[i][j] = 1;
                } else if (j == 7) {
                    BOARD[i][j] = 2;
                }
            }
        }
    }

    public void print() {
        for (int[] ints : BOARD) {
            System.out.println(Arrays.toString(ints).replaceAll("[\\[\\],]", "").replaceAll(",", " "));
        }
    }

    public void play(String move) {
        lastMove = move;

        int fromRow = move.charAt(0) - 65;
        int fromCol = Character.getNumericValue(move.charAt(1)) - 1;
        int toRow = move.charAt(2) - 65;
        int toCol = Character.getNumericValue(move.charAt(3)) - 1;
        int movedPiece = BOARD[fromRow][fromCol];
        int capturedPiece = BOARD[toRow][toCol];

        MOVE_STACK.push(new MoveState(fromRow, fromCol, toRow, toCol, movedPiece, capturedPiece));

        BOARD[fromRow][fromCol] = 0;
        BOARD[toRow][toCol] = movedPiece;
    }

    public void undo() {
        if (MOVE_STACK.isEmpty()) return;
        MoveState ms = MOVE_STACK.pop();
        BOARD[ms.toRow][ms.toCol] = ms.capturedPiece;
        BOARD[ms.fromRow][ms.fromCol] = ms.movedPiece;
    }

    public boolean hasWon(int player) {
        return evaluate(player) == WIN_SCORE;
    }

    public int evaluate(int player) {
        int opponent = player == 3 ? 1 : 3;
        int playerWinningCol = player == 3 ? 7 : 0;
        int opponentWinningCol = opponent == 3 ? 7 : 0;
        int playerPusher = player + 1;
        int opponentPusher = opponent + 1;

        int playerScore = 0, opponentScore = 0;
        int playerCount = 0, opponentCount = 0;
        boolean hasPlayerPusher = false, hasOpponentPusher = false;

        for (int row = 0; row < 8; row++) {
            if (BOARD[row][playerWinningCol] == player) return Const.WIN_SCORE;
            if (BOARD[row][opponentWinningCol] == opponent) return Const.LOSS_SCORE;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = BOARD[row][col];
                boolean isCenter = (row >= 2 && row <= 5) && (col >= 2 && col <= 5);

                if (piece == player) {
                    playerScore += 100 + ((player == 3) ? col * 10 : (7 - col) * 10);
                    if (isCenter) playerScore += 20;
                    if (col == 0 || col == 7) playerScore -= 10;
                    playerCount++;
                } else if (piece == playerPusher) {
                    playerScore += 150 + ((player == 3) ? col * 15 : (7 - col) * 15);
                    if (isCenter) playerScore += 30;
                    if (col == 0 || col == 7) playerScore -= 15;
                    if ((player == 3 && col < 7 && BOARD[row][col + 1] == opponentPusher) ||
                            (player == 1 && col > 0 && BOARD[row][col - 1] == opponentPusher)) {
                        playerScore += 40;
                    }
                    hasPlayerPusher = true;
                    playerCount++;
                } else if (piece == opponent) {
                    opponentScore += 100 + ((opponent == 3) ? col * 10 : (7 - col) * 10);
                    if (isCenter) opponentScore += 20;
                    if (col == 0 || col == 7) opponentScore -= 10;
                    opponentCount++;
                } else if (piece == opponentPusher) {
                    opponentScore += 150 + ((opponent == 3) ? col * 15 : (7 - col) * 15);
                    if (isCenter) opponentScore += 30;
                    if (col == 0 || col == 7) opponentScore -= 15;
                    if ((opponent == 3 && col < 7 && BOARD[row][col + 1] == playerPusher) ||
                            (opponent == 1 && col > 0 && BOARD[row][col - 1] == playerPusher)) {
                        opponentScore += 40;
                    }
                    hasOpponentPusher = true;
                    opponentCount++;
                }
            }
        }

        if (!hasPlayerPusher) return Const.LOSS_SCORE;
        if (!hasOpponentPusher) return Const.WIN_SCORE;

        playerScore += (playerCount - opponentCount) * 50;
        return playerScore - opponentScore;
    }

    public ArrayList<String> getPossibleMoves(int player) {
        ArrayList<String> possibleMoves = new ArrayList<>(32);
        int enemyPlayer = player == 3 ? 1 : 3;
        StringBuilder sb = new StringBuilder(4);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (BOARD[row][col] == player) {
                    if (player == 3) { // PETIT ROUGE
                        if (BOARD[row][col + 1] == 0 && BOARD[row][col - 1] == player + 1) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 65)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && ((BOARD[row - 1][col + 1] == 0 || BOARD[row - 1][col + 1] == enemyPlayer || BOARD[row - 1][col + 1] == enemyPlayer + 1) && BOARD[row + 1][col - 1] == player + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 64)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && ((BOARD[row + 1][col + 1] == 0 || BOARD[row + 1][col + 1] == enemyPlayer || BOARD[row + 1][col + 1] == enemyPlayer + 1) && BOARD[row - 1][col - 1] == player + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 66)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }
                    } else { // PETIT NOIR
                        if (BOARD[row][col - 1] == 0 && BOARD[row][col + 1] == player + 1) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 65)).append(col);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && ((BOARD[row - 1][col - 1] == 0 || BOARD[row - 1][col - 1] == enemyPlayer || BOARD[row - 1][col - 1] == enemyPlayer + 1) && BOARD[row + 1][col + 1] == player + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 64)).append(col);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && (BOARD[row + 1][col - 1] == 0 || BOARD[row + 1][col - 1] == enemyPlayer || BOARD[row + 1][col - 1] == enemyPlayer + 1) && BOARD[row - 1][col + 1] == player + 1) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 66)).append(col);
                            possibleMoves.add(sb.toString());
                        }
                    }
                } else if (BOARD[row][col] == player + 1) {
                    if (player == 3 && col < 7) { // PUSHER ROUGE
                        if (BOARD[row][col + 1] == 0) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 65)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && (BOARD[row - 1][col + 1] == 0 || BOARD[row - 1][col + 1] == enemyPlayer || BOARD[row - 1][col + 1] == enemyPlayer + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 64)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }

                        if (row < 7 && (BOARD[row + 1][col + 1] == 0 || BOARD[row + 1][col + 1] == enemyPlayer || BOARD[row + 1][col + 1] == enemyPlayer + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 66)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }
                    } else if (col > 0) { // PUSHER NOIR
                        if (BOARD[row][col - 1] == 0) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 65)).append(col);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && (BOARD[row - 1][col - 1] == 0 || BOARD[row - 1][col - 1] == enemyPlayer || BOARD[row - 1][col - 1] == enemyPlayer + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 64)).append(col);
                            possibleMoves.add(sb.toString());
                        }

                        if (row < 7 && (BOARD[row + 1][col - 1] == 0 || BOARD[row + 1][col - 1] == enemyPlayer || BOARD[row + 1][col - 1] == enemyPlayer + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65)).append(col + 1).append((char) (row + 66)).append(col);
                            possibleMoves.add(sb.toString());
                        }
                    }
                }
            }
        }

        possibleMoves.sort(MOVE_COMPARATOR);
        return possibleMoves;
    }
}
