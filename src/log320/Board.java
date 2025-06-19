package log320;

import log320.evaluators.DefenseEvaluator;
import log320.evaluators.IEvaluator;

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
    private final IEvaluator EVALUATOR = new DefenseEvaluator();

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

    public int evaluate(Player player) {
        return EVALUATOR.evaluate(this, player);
    }

    public ArrayList<Move> getPossibleMoves(Player player) {
        ArrayList<Move> possibleMoves = new ArrayList<>(32);
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

        possibleMoves.sort(player == Player.RED ? MOVE_COMPARATOR_RED : MOVE_COMPARATOR_BLACK);
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
