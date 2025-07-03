package log320;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static log320.Const.*;

public class Board {
    private final int[] BOARD = new int[64];
    private final MoveComparator MOVE_COMPARATOR_RED = new MoveComparator(this, Player.RED);
    private final MoveComparator MOVE_COMPARATOR_BLACK = new MoveComparator(this, Player.BLACK);
    private final Stack<UndoMoveState> MOVE_STACK = new Stack<>();
    private final List<UndoMoveState> MOVE_STATE_POOL = new ArrayList<>(1000);
    private final BoardEvaluator EVALUATOR = new BoardEvaluator(this);

    private Move lastMove = null;
    private int moveStatePoolIndex = 0;

    public Board() {
        for (int i = 0; i < 1000; i++) {
            MOVE_STATE_POOL.add(new UndoMoveState());
        }
    }

    public Board(String s) {
        this();
        build(s);
    }

    public Move getLastMove() {
        return lastMove;
    }

    public void build(String s) {
        // handle starting from an active board state
        Integer[] boardValues = Arrays.stream(s.split(" ")).limit(64).map(Integer::parseInt).toArray(Integer[]::new);

        int row = 7, col = 0;
        for (Integer boardValue : boardValues) {
            set(row, col, boardValue);

            if (++col > 7) {
                col = 0;
                row--;
            }
        }
    }

    public void init() {
        clear();

        for (int col = 0; col < 8; col++) {
            set(0, col, Player.RED.getPusher());
            set(1, col, Player.RED.getPawn());
            set(6, col, Player.BLACK.getPawn());
            set(7, col, Player.BLACK.getPusher());
        }
    }

    public void clear() {
        Arrays.fill(BOARD, EMPTY);
    }

    public void print() {
        // les rangées sont stockées de bas en haut, donc l'impression se fait à l'envers
        for (int row = 7; row >= 0; --row) {
            for (int col = 0; col < 8; ++col) {
                System.out.print(get(row, col) + " ");
            }

            System.out.println();
        }
        System.out.println("==========================");
    }

    public int get(int row, int col) {
        return BOARD[row * 8 + col];
    }

    public void set(int row, int col, int piece) {
        BOARD[row * 8 + col] = piece;
    }

    public void play(Move move) {
        lastMove = move;

        int movedPiece = get(move.getFromRow(), move.getFromCol());
        int capturedPiece = get(move.getToRow(), move.getToCol());

        UndoMoveState ms = MOVE_STATE_POOL.get(moveStatePoolIndex++);
        MOVE_STACK.push(ms.set(move, movedPiece, capturedPiece));

        set(move.getFromRow(), move.getFromCol(), EMPTY);
        set(move.getToRow(), move.getToCol(), movedPiece);
    }

    public void undo() {
        if (MOVE_STACK.isEmpty()) return;
        UndoMoveState ms = MOVE_STACK.pop();
        set(ms.move.getToRow(), ms.move.getToCol(), ms.capturedPiece);
        set(ms.move.getFromRow(), ms.move.getFromCol(), ms.movedPiece);
        moveStatePoolIndex--;
    }

    public int evaluate(Player player) {
        return EVALUATOR.evaluate(player);
    }

    public ArrayList<Move> getPossibleMoves(Player player) {
        ArrayList<Move> possibleMoves = new ArrayList<>(32);
        StringBuilder sb = new StringBuilder(4);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (get(row, col) == player.getPawn()) {
                    if (col > 0 && col < 7 && get(row - player.getDirection(), col + 1) == player.getPusher() && (get(row + player.getDirection(), col - 1) == EMPTY || get(row + player.getDirection(), col - 1) == player.getOpponent().getPawn() || get(row + player.getDirection(), col - 1) == player.getOpponent().getPusher())) {
                        sb.setLength(0);
                        sb.append((char) (col + COL_CHAR_OFFSET)).append(row + 1).append((char) (col + COL_CHAR_OFFSET - 1)).append(row + 1 + player.getDirection());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }

                    if (get(row + player.getDirection(), col) == EMPTY && get(row - player.getDirection(), col) == player.getPusher()) {
                        sb.setLength(0);
                        sb.append((char) (col + COL_CHAR_OFFSET)).append(row + 1).append((char) (col + COL_CHAR_OFFSET)).append(row + 1 + player.getDirection());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }

                    if (col > 0 && col < 7 && get(row - player.getDirection(), col - 1) == player.getPusher() && (get(row + player.getDirection(), col + 1) == EMPTY || get(row + player.getDirection(), col + 1) == player.getOpponent().getPawn() || get(row + player.getDirection(), col + 1) == player.getOpponent().getPusher())) {
                        sb.setLength(0);
                        sb.append((char) (col + COL_CHAR_OFFSET)).append(row + 1).append((char) (col + COL_CHAR_OFFSET + 1)).append(row + 1 + player.getDirection());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }
                } else if (get(row, col) == player.getPusher()) {
                    if (col > 0 && (get(row + player.getDirection(), col - 1) == EMPTY || get(row + player.getDirection(), col - 1) == player.getOpponent().getPawn() || get(row + player.getDirection(), col - 1) == player.getOpponent().getPusher())) {
                        sb.setLength(0);
                        sb.append((char) (col + COL_CHAR_OFFSET)).append(row + 1).append((char) (col + COL_CHAR_OFFSET - 1)).append(row + 1 + player.getDirection());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }

                    if (get(row + player.getDirection(), col) == EMPTY) {
                        sb.setLength(0);
                        sb.append((char) (col + COL_CHAR_OFFSET)).append(row + 1).append((char) (col + COL_CHAR_OFFSET)).append(row + 1 + player.getDirection());
                        possibleMoves.add(ALL_MOVES.get(sb.toString()));
                    }

                    if (col < 7 && (get(row + player.getDirection(), col + 1) == EMPTY || get(row + player.getDirection(), col + 1) == player.getOpponent().getPawn() || get(row + player.getDirection(), col + 1) == player.getOpponent().getPusher())) {
                        sb.setLength(0);
                        sb.append((char) (col + COL_CHAR_OFFSET)).append(row + 1).append((char) (col + COL_CHAR_OFFSET + 1)).append(row + 1 + player.getDirection());
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
        System.arraycopy(this.BOARD, 0, clone.BOARD, 0, 64);
        return clone;
    }

    // permet de cloner et joeur un moe
    public Board clone(Move move) {
        Board clone = new Board();
        System.arraycopy(this.BOARD, 0, clone.BOARD, 0, 64);
        clone.play(move);
        return clone;
    }

    public boolean isRowCovered(Player player, int row) {
        Boolean[] covered = new Boolean[8];
        Arrays.fill(covered, false);

        for (int col = 0; col < 8; col++) {
            if (get(row - player.getDirection(), col) == player.getPusher()) {
                if (col > 0) {
                    covered[col - 1] = true;
                }

                if (col < 7) {
                    covered[col + 1] = true;
                }
            } else if (col > 0 && col < 7 && get(row - player.getDirection(), col) == player.getPawn()) {
                if (get(row - player.getDirection() - player.getDirection(), col - 1) == player.getPusher()) {
                    covered[col + 1] = true;
                }

                if (get(row - player.getDirection() - player.getDirection(), col + 1) == player.getPusher()) {
                    covered[col - 1] = true;
                }
            }
        }

        return Arrays.stream(covered).allMatch(c -> c);
    }

    public boolean isExposed(Player player, int row, int col) {
        int forwardRow = row + player.getDirection();
        int doubleForwardRow = forwardRow + player.getDirection();

        if (forwardRow < 0 || forwardRow > 7) {
            return false;
        }

        if ((col > 0 && get(forwardRow, col - 1) == player.getOpponent().getPusher()) ||
                (col < 7 && get(forwardRow, col + 1) == player.getOpponent().getPusher())) {
            return true;
        } else if (col > 1 && doubleForwardRow >= 0 && doubleForwardRow < 8 &&
                get(forwardRow, col - 1) == player.getOpponent().getPawn() &&
                get(doubleForwardRow, col - 2) == player.getOpponent().getPusher()) {
            return true;
        }

        return col < 6 && doubleForwardRow >= 0 && doubleForwardRow < 8 &&
                get(forwardRow, col + 1) == player.getOpponent().getPawn() &&
                get(doubleForwardRow, col + 2) == player.getOpponent().getPusher();
    }

    // TODO activé dans une direction particvulière
    public boolean isPawnActivated(Player player, int row, int col) {
        int backRow = row - player.getDirection();

        if (backRow < 0 || backRow > 7) {
            return false;
        }

        return (col > 0 && get(backRow, col - 1) == player.getPusher()) ||
                get(backRow, col) == player.getPusher() ||
                (col < 7 && get(backRow, col + 1) == player.getPusher());
    }
}
