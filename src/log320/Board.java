package log320;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static log320.Const.*;

public class Board {
    private final MoveComparator MOVE_COMPARATOR_RED, MOVE_COMPARATOR_BLACK;
    private final int[][] BOARD = new int[8][8];
    private final Stack<UndoMoveState> MOVE_STACK = new Stack<>();
    private final List<UndoMoveState> MOVE_STATE_POOL = new ArrayList<>(1000);

    private Move lastMove = null;
    private int moveStatePoolIndex = 0;

    public Board() {
        MOVE_COMPARATOR_RED = new MoveComparator(this, Player.RED);
        MOVE_COMPARATOR_BLACK = new MoveComparator(this, Player.BLACK);

        for (int i = 0; i < 1000; i++) {
            MOVE_STATE_POOL.add(new UndoMoveState());
        }
    }

    public Board(String s) {
        this();
        build(s);
    }

    public int[][] getBoard() {
        return BOARD;
    }

    public Move getLastMove() {
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

    public void clear() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                BOARD[i][j] = EMPTY;
            }
        }
    }

    public void print() {
        for (int[] ints : BOARD) {
            System.out.println(Arrays.toString(ints).replaceAll("[\\[\\],]", "").replaceAll(",", " "));
        }
    }

    public void play(Move move) {
        lastMove = move;

        int movedPiece = BOARD[move.getFromRow()][move.getFromCol()];
        int capturedPiece = BOARD[move.getToRow()][move.getToCol()];

        UndoMoveState ms = MOVE_STATE_POOL.get(moveStatePoolIndex++);
        MOVE_STACK.push(ms.set(move, movedPiece, capturedPiece));

        BOARD[move.getFromRow()][move.getFromCol()] = EMPTY;
        BOARD[move.getToRow()][move.getToCol()] = movedPiece;
    }

    public void undo() {
        if (MOVE_STACK.isEmpty()) return;
        UndoMoveState ms = MOVE_STACK.pop();
        BOARD[ms.move.getToRow()][ms.move.getToCol()] = ms.capturedPiece;
        BOARD[ms.move.getFromRow()][ms.move.getFromCol()] = ms.movedPiece;
        moveStatePoolIndex--;
    }

    public boolean hasWon(Player player) {
        return evaluate(player) == WIN_SCORE;
    }

    public int evaluate(Player player) {
        // TODO: retirer des points si des pions sont exposés
        int score = 0;

        int playerPushers = 0, playerPawns = 0;
        int opponentPushers = 0, opponentPawns = 0;

        for (int row = 0; row < 8; row++) {
            if (BOARD[row][player.getWinningCol()] == player.getPawn() || BOARD[row][player.getWinningCol()] == player.getPusher())
                return WIN_SCORE;
            if (BOARD[row][player.getOpponent().getWinningCol()] == player.getOpponent().getPawn() || BOARD[row][player.getOpponent().getWinningCol()] == player.getOpponent().getPusher())
                return LOSS_SCORE;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (BOARD[row][col] == player.getPusher()) {
                    playerPushers++;
                    score += player == Player.RED ? (col * col) / 10 : ((7 - col) * (7 - col)) / 10;

                    if (col == player.getWinningCol()) {
                        score += 500;
                    }
                } else if (BOARD[row][col] == player.getOpponent().getPusher()) {
                    opponentPushers++;
                    score -= player == Player.RED ? (col * col) / 10 : ((7 - col) * (7 - col)) / 10;
                } else if (BOARD[row][col] == player.getPawn()) {
                    playerPawns++;
                    score += player == Player.RED ? (col * col) / 10 : ((7 - col) * (7 - col)) / 10;

                    if (col == player.getWinningCol()) {
                        score += 500;
                    }
                } else if (BOARD[row][col] == player.getOpponent().getPawn()) {
                    opponentPawns++;
                    score -= player == Player.RED ? (col * col) / 10 : ((7 - col) * (7 - col)) / 10;
                }

                if ((BOARD[row][col] == player.getPawn() || BOARD[row][col] == player.getPusher()) && row >= 2 && row <= 5) {
                    score += 15;
                }
            }
        }

        if (opponentPushers == 0) {
            return WIN_SCORE;
        }

        if (playerPushers == 0) {
            return LOSS_SCORE;
        }

        score += (playerPushers * 300 + playerPawns * 100) - (opponentPushers * 300 + opponentPawns * 100);

        List<Move> myMoves = getPossibleMoves(player);
        List<Move> oppMoves = getPossibleMoves(player.getOpponent());
        score += (myMoves.size() - oppMoves.size()) * 10;

        score += 20 * countPotentialPushes(player);

        return score;
    }

    private int countPotentialPushes(Player player) {
        int count = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = player == Player.RED ? 0 : 1; col < (player == Player.RED ? 7 : 8); col++) {
                boolean isPusher = false;
                if (BOARD[row][col] == player.getPusher()) {
                    isPusher = true;
                }

                if (isPusher) {
                    int piece = BOARD[row][col + player.getForwardColumn()];
                    if (piece == player.getPawn()) {
                        count++;
                    }

                    if (row > 0) {
                        piece = BOARD[row - 1][col + player.getForwardColumn()];
                        if (piece == player.getPawn()) {
                            count++;
                        }
                    }

                    if (row < 7) {
                        piece = BOARD[row + 1][col + player.getForwardColumn()];
                        if (piece == player.getPawn()) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    public List<Move> getPossibleMoves(Player player) {
        List<Move> possibleMoves = new ArrayList<>(32);
        StringBuilder sb = new StringBuilder(4);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (BOARD[row][col] == player.getPawn()) {
                    if (BOARD[row][col + player.getForwardColumn()] == EMPTY && BOARD[row][col - player.getForwardColumn()] == player.getPusher()) {
                        sb.setLength(0);
                        sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW)).append(col + 1 + player.getForwardColumn());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }

                    if (row > 0 && row < 7 && BOARD[row + 1][col - player.getForwardColumn()] == player.getPusher() && ((BOARD[row - 1][col + player.getForwardColumn()] == EMPTY || BOARD[row - 1][col + player.getForwardColumn()] == player.getOpponent().getPawn() || BOARD[row - 1][col + player.getForwardColumn()] == player.getOpponent().getPusher()))) {
                        sb.setLength(0);
                        sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW - 1)).append(col + 1 + player.getForwardColumn());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }

                    if (row > 0 && row < 7 && BOARD[row - 1][col - player.getForwardColumn()] == player.getPusher() && ((BOARD[row + 1][col + player.getForwardColumn()] == EMPTY || BOARD[row + 1][col + player.getForwardColumn()] == player.getOpponent().getPawn() || BOARD[row + 1][col + player.getForwardColumn()] == player.getOpponent().getPusher()))) {
                        sb.setLength(0);
                        sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW + 1)).append(col + 1 + player.getForwardColumn());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }
                } else if (BOARD[row][col] == player.getPusher()) {
                    if (BOARD[row][col + player.getForwardColumn()] == EMPTY) {
                        sb.setLength(0);
                        sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW)).append(col + 1 + player.getForwardColumn());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }

                    if (row > 0 && (BOARD[row - 1][col + player.getForwardColumn()] == EMPTY || BOARD[row - 1][col + player.getForwardColumn()] == player.getOpponent().getPawn() || BOARD[row - 1][col + player.getForwardColumn()] == player.getOpponent().getPusher())) {
                        sb.setLength(0);
                        sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW - 1)).append(col + 1 + player.getForwardColumn());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }

                    if (row < 7 && (BOARD[row + 1][col + player.getForwardColumn()] == EMPTY || BOARD[row + 1][col + player.getForwardColumn()] == player.getOpponent().getPawn() || BOARD[row + 1][col + player.getForwardColumn()] == player.getOpponent().getPusher())) {
                        sb.setLength(0);
                        sb.append((char) (row + CHAR_TO_ROW)).append(col + 1).append((char) (row + CHAR_TO_ROW + 1)).append(col + 1 + player.getForwardColumn());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }
                }
            }
        }

        // TODO: Sort the possible moves based on heuristics
        // possibleMoves.sort(player == Player.RED ? MOVE_COMPARATOR_RED : MOVE_COMPARATOR_BLACK);
        return possibleMoves;
    }

    public Board clone() {
        Board clone = new Board();
        for (int i = 0; i < 8; i++) {
            System.arraycopy(this.BOARD[i], 0, clone.BOARD[i], 0, 8);
        }
        return clone;
    }
}
