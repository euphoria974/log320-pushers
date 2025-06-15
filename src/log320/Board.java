package log320;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static log320.Const.*;

public class Board {
    private final MoveComparator MOVE_COMPARATOR;
    private final int[][] BOARD = new int[8][8];
    private final Stack<UndoMoveState> MOVE_STACK = new Stack<>();
    private final List<UndoMoveState> MOVE_STATE_POOL = new ArrayList<>();

    private String lastMove = "";
    private int moveStatePoolIndex = 0;

    public Board(String s, Player player) {
        build(s);
        MOVE_COMPARATOR = new MoveComparator(this, player);

        for (int i = 0; i < 1000; i++) {
            MOVE_STATE_POOL.add(new UndoMoveState());
        }
    }

    public int[][] getBoard() {
        return BOARD;
    }

    public String getLastMove() {
        return lastMove;
    }

    public void build(String s) {
        String[] boardValues = s.split(" ");

        int row = 0, col = 7;
        for (String boardValue : boardValues) {
            BOARD[row][col] = Integer.parseInt(boardValue);

            row++;
            if (row > 7) {
                row = 0;
                col--;
            }

            if (col < 0) {
                break;
            }
        }
    }

    public void reset() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (j == 0) {
                    BOARD[i][j] = RED_PUSHER;
                } else if (j == 1) {
                    BOARD[i][j] = RED_PAWN;
                } else if (j == 6) {
                    BOARD[i][j] = BLACK_PAWN;
                } else if (j == 7) {
                    BOARD[i][j] = BLACK_PUSHER;
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

        int fromRow = move.charAt(0) - CHAR_TO_ROW;
        int fromCol = Character.getNumericValue(move.charAt(1)) - 1;
        int toRow = move.charAt(2) - CHAR_TO_ROW;
        int toCol = Character.getNumericValue(move.charAt(3)) - 1;
        int movedPiece = BOARD[fromRow][fromCol];
        int capturedPiece = BOARD[toRow][toCol];

        UndoMoveState ms = MOVE_STATE_POOL.get(moveStatePoolIndex++);
        MOVE_STACK.push(ms.update(fromRow, fromCol, toRow, toCol, movedPiece, capturedPiece));

        BOARD[fromRow][fromCol] = EMPTY;
        BOARD[toRow][toCol] = movedPiece;
    }

    public void undo() {
        if (MOVE_STACK.isEmpty()) return;
        UndoMoveState ms = MOVE_STACK.pop();
        BOARD[ms.toRow][ms.toCol] = ms.capturedPiece;
        BOARD[ms.fromRow][ms.fromCol] = ms.movedPiece;
        moveStatePoolIndex--;
    }

    public boolean hasWon(Player player) {
        return evaluate(player) == WIN_SCORE;
    }

    public int evaluate(Player player) {
        int playerScore = 0, opponentScore = 0;
        int playerCount = 0, opponentCount = 0;
        boolean hasPlayerPusher = false, hasOpponentPusher = false;

        for (int row = 0; row < 8; row++) {
            if (BOARD[row][player.getWinningCol()] == player.getPawn() || BOARD[row][player.getWinningCol()] == player.getPusher())
                return WIN_SCORE;
            if (BOARD[row][player.getOpponent().getWinningCol()] == player.getOpponent().getPawn() || BOARD[row][player.getOpponent().getWinningCol()] == player.getOpponent().getPusher())
                return LOSS_SCORE;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = BOARD[row][col];
                boolean isCenter = (row >= 2 && row <= 5) && (col >= 2 && col <= 5);

                if (piece == player.getPawn()) {
                    playerScore += 100 + ((player == Player.RED) ? col * 10 : (RED_WINNING_COL - col) * 10);
                    if (isCenter) playerScore += 20;
                    if (col == BLACK_WINNING_COL || col == RED_WINNING_COL) playerScore -= 10;
                    playerCount++;
                } else if (piece == player.getPusher()) {
                    playerScore += 150 + ((player == Player.RED) ? col * 15 : (RED_WINNING_COL - col) * 15);
                    if (isCenter) playerScore += 30;
                    if (col == BLACK_WINNING_COL || col == RED_WINNING_COL) playerScore -= 15;
                    if ((player == Player.RED && col < 7 && BOARD[row][col + 1] == player.getOpponent().getPusher()) ||
                            (player == Player.BLACK && col > 0 && BOARD[row][col - 1] == player.getOpponent().getPusher())) {
                        playerScore += 40;
                    }
                    hasPlayerPusher = true;
                    playerCount++;
                } else if (piece == player.getOpponent().getPawn()) {
                    opponentScore += 100 + ((player.getOpponent() == Player.RED) ? col * 10 : (RED_WINNING_COL - col) * 10);
                    if (isCenter) opponentScore += 20;
                    if (col == BLACK_WINNING_COL || col == RED_WINNING_COL) opponentScore -= 10;
                    opponentCount++;
                } else if (piece == player.getOpponent().getPusher()) {
                    opponentScore += 150 + ((player.getOpponent() == Player.RED) ? col * 15 : (RED_WINNING_COL - col) * 15);
                    if (isCenter) opponentScore += 30;
                    if (col == BLACK_WINNING_COL || col == RED_WINNING_COL) opponentScore -= 15;
                    if ((player.getOpponent() == Player.RED && col < 7 && BOARD[row][col + 1] == player.getPusher()) ||
                            (player.getOpponent() == Player.BLACK && col > 0 && BOARD[row][col - 1] == player.getPusher())) {
                        opponentScore += 40;
                    }
                    hasOpponentPusher = true;
                    opponentCount++;
                }
            }
        }

        if (!hasPlayerPusher) return LOSS_SCORE;
        if (!hasOpponentPusher) return WIN_SCORE;

        playerScore += (playerCount - opponentCount) * 50;
        return playerScore - opponentScore;
    }

    public ArrayList<String> getPossibleMoves(Player player) {
        ArrayList<String> possibleMoves = new ArrayList<>(32);
        StringBuilder sb = new StringBuilder(4);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (BOARD[row][col] == player.getPawn()) {
                    if (player == Player.RED) {
                        if (BOARD[row][col + 1] == EMPTY && BOARD[row][col - 1] == player.getPusher()) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && BOARD[row + 1][col - 1] == player.getPusher() && ((BOARD[row - 1][col + 1] == EMPTY || BOARD[row - 1][col + 1] == player.getOpponent().getPawn() || BOARD[row - 1][col + 1] == player.getOpponent().getPusher()))) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW - 1)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && BOARD[row - 1][col - 1] == player.getPusher() && ((BOARD[row + 1][col + 1] == EMPTY || BOARD[row + 1][col + 1] == player.getOpponent().getPawn() || BOARD[row + 1][col + 1] == player.getOpponent().getPusher()))) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW + 1)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }
                    } else { // PETIT NOIR
                        if (BOARD[row][col - 1] == EMPTY && BOARD[row][col + 1] == player.getPusher()) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW)).append(col);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && BOARD[row + 1][col + 1] == player.getPusher() && ((BOARD[row - 1][col - 1] == EMPTY || BOARD[row - 1][col - 1] == player.getOpponent().getPawn() || BOARD[row - 1][col - 1] == player.getOpponent().getPusher()))) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW - 1)).append(col);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && BOARD[row - 1][col + 1] == player.getPusher() && (BOARD[row + 1][col - 1] == EMPTY || BOARD[row + 1][col - 1] == player.getOpponent().getPawn() || BOARD[row + 1][col - 1] == player.getOpponent().getPusher())) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW + 1)).append(col);
                            possibleMoves.add(sb.toString());
                        }
                    }
                } else if (BOARD[row][col] == player.getPusher()) {
                    if (player == Player.RED && col < 7) { // PUSHER ROUGE
                        if (BOARD[row][col + 1] == EMPTY) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && (BOARD[row - 1][col + 1] == EMPTY || BOARD[row - 1][col + 1] == player.getOpponent().getPawn() || BOARD[row - 1][col + 1] == player.getOpponent().getPusher())) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW - 1)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }

                        if (row < 7 && (BOARD[row + 1][col + 1] == EMPTY || BOARD[row + 1][col + 1] == player.getOpponent().getPawn() || BOARD[row + 1][col + 1] == player.getOpponent().getPusher())) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW + 1)).append(col + 2);
                            possibleMoves.add(sb.toString());
                        }
                    } else if (col > 0) { // PUSHER NOIR
                        if (BOARD[row][col - 1] == EMPTY) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW)).append(col);
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && (BOARD[row - 1][col - 1] == EMPTY || BOARD[row - 1][col - 1] == player.getOpponent().getPawn() || BOARD[row - 1][col - 1] == player.getOpponent().getPusher())) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW - 1)).append(col);
                            possibleMoves.add(sb.toString());
                        }

                        if (row < 7 && (BOARD[row + 1][col - 1] == EMPTY || BOARD[row + 1][col - 1] == player.getOpponent().getPawn() || BOARD[row + 1][col - 1] == player.getOpponent().getPusher())) {
                            sb.setLength(0);
                            sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW + 1)).append(col);
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
