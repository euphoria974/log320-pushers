package log320;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static log320.Const.*;

public class Board {
    private final MoveComparator MOVE_COMPARATOR_RED, MOVE_COMPARATOR_BLACK;
    private final int[] BOARD = new int[64];
    private final Stack<UndoMoveState> MOVE_STACK = new Stack<>();
    private final List<UndoMoveState> MOVE_STATE_POOL = new ArrayList<>(1000);
    private final BoardEvaluator EVALUATOR = new BoardEvaluator(this);

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

    public Move getLastMove() {
        return lastMove;
    }

    private void build(String s) {
        String[] boardValues = s.split(" ");

        int row = 7, col = 0;
        for (String boardValue : boardValues) {
            place(row, col, Integer.parseInt(boardValue));

            if (++col > 7) {
                col = 0;
                --row;
            }
        }
    }

    public void reset() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (j == 0) {
                    place(i, j, RED_PUSHER);
                } else if (j == 1) {
                    place(i, j, RED_PAWN);
                } else if (j == 6) {
                    place(i, j, BLACK_PAWN);
                } else if (j == 7) {
                    place(i, j, BLACK_PUSHER);
                }
            }
        }
    }

    public void clear() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                place(i, j, EMPTY);
            }
        }
    }

    public void print() {
        // les rangées sont stockées de bas en haut, donc l'impression se fait à l'envers
        for (int row = 7; row >= 0; --row) {
            for (int col = 0; col < 8; ++col) {
                System.out.print(get(row, col) + " ");
            }
            System.out.println();
        }
    }

    public void play(Move move) {
        lastMove = move;

        int movedPiece = get(move.getFromRow(), move.getFromCol());
        int capturedPiece = get(move.getToRow(), move.getToCol());

        UndoMoveState ms = MOVE_STATE_POOL.get(moveStatePoolIndex++);
        MOVE_STACK.push(ms.set(move, movedPiece, capturedPiece));

        place(move.getFromRow(), move.getFromCol(), EMPTY);
        place(move.getToRow(), move.getToCol(), movedPiece);
    }

    public void place(int row, int col, int piece) {
        BOARD[row * 8 + col] = piece;
    }

    public int get(int row, int col) {
        return BOARD[row * 8 + col];
    }

    public void undo() {
        if (MOVE_STACK.isEmpty())
            return;
        UndoMoveState ms = MOVE_STACK.pop();
        place(ms.move.getToRow(), ms.move.getToCol(), ms.capturedPiece);
        place(ms.move.getFromRow(), ms.move.getFromCol(), ms.movedPiece);
        moveStatePoolIndex--;
    }

    public int evaluate(Player player) {
        return EVALUATOR.evaluate(player);
    }

    public boolean isGameOver() {
        for (int col = 0; col < 8; ++col) {
            if (get(RED_WINNING_ROW, col) == RED_PAWN || get(RED_WINNING_ROW, col) == RED_PUSHER) {
                return true;
            }
            if (get(BLACK_WINNING_ROW, col) == BLACK_PAWN || get(BLACK_WINNING_ROW, col) == BLACK_PUSHER) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Move> getPossibleMoves(Player player) {
        ArrayList<Move> possibleMoves = new ArrayList<>(32);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (get(row, col) == player.getPawn()) {
                    // identifie les moves possibles pour un pion simple
                    int targetRow = row + player.getDirection();
                    int pusherRow = row - player.getDirection();
                    if (get(targetRow, col) == EMPTY && get(pusherRow, col) == player.getPusher()) {
                        possibleMoves.add(new Move(row, col, targetRow, col));
                    }

                    if (col > 0 && col < 7) {
                        if (get(pusherRow, col + 1) == player.getPusher() && (get(targetRow, col - 1) == EMPTY ||
                                get(targetRow, col - 1) == player.getOpponent().getPawn() ||
                                get(targetRow, col - 1) == player.getOpponent().getPusher())) {
                            possibleMoves.add(new Move(row, col, targetRow, col - 1));
                        }

                        if (get(pusherRow, col - 1) == player.getPusher() && (get(targetRow, col + 1) == EMPTY ||
                                get(targetRow, col + 1) == player.getOpponent().getPawn() ||
                                get(targetRow, col + 1) == player.getOpponent().getPusher())) {
                            possibleMoves.add(new Move(row, col, targetRow, col + 1));
                        }
                    }
                } else if (get(row, col) == player.getPusher()) {
                    // identifie les moves possibles pour un pusher
                    int targetRow = row + player.getDirection();
                    if (get(targetRow, col) == EMPTY) {
                        possibleMoves.add(new Move(row, col, targetRow, col));
                    }

                    if (col > 0 && (get(targetRow, col - 1) == EMPTY ||
                            get(targetRow, col - 1) == player.getOpponent().getPawn() ||
                            get(targetRow, col - 1) == player.getOpponent().getPusher())) {
                        possibleMoves.add(new Move(row, col, targetRow, col - 1));
                    }

                    if (col < 7 && (get(targetRow, col + 1) == EMPTY ||
                            get(targetRow, col + 1) == player.getOpponent().getPawn() ||
                            get(targetRow, col + 1) == player.getOpponent().getPusher())) {
                        possibleMoves.add(new Move(row, col, targetRow, col + 1));
                    }
                }
            }
        }

        possibleMoves.sort(player == Player.RED ? MOVE_COMPARATOR_RED : MOVE_COMPARATOR_BLACK);
        return possibleMoves;
    }

    public Board clone() {
        Board clone = new Board();
        System.arraycopy(BOARD, 0, clone.BOARD, 0, BOARD.length);
        return clone;
    }

    public boolean isExposed(Player player, int toRow, int toCol) {
        int opponentForward = player.getOpponent().getDirection();
        int playerForward = player.getDirection();

        int[][] threatDiagonals = {{-opponentForward, -1}, {-opponentForward, 1}};
        boolean threatened = false;
        for (int[] dir : threatDiagonals) {
            int row = toRow + dir[0];
            int col = toCol + dir[1];
            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                if (get(row, col) == player.getOpponent().getPusher()) {
                    threatened = true;
                    break;
                }
            }
        }

        int[][] protectDiagonals = {{-playerForward, -1}, {-playerForward, 1}};
        boolean protectedByPusher = false;

        for (int[] dir : protectDiagonals) {
            int row = toRow + dir[0];
            int col = toCol + dir[1];
            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                if (get(row, col) == player.getPusher()) {
                    protectedByPusher = true;
                    break;
                }
            }
        }

        return threatened && !protectedByPusher;
    }

    public boolean canCapture(Player player, int r, int c) {
        int playerForward = player.getDirection();

        int[][] threatDiagonals = {{playerForward, -1}, {playerForward, 1}};

        int[][] protectDiagonals = {{-playerForward, -1}, {-playerForward, 1}};

        for (int[] direction : threatDiagonals) {
            int row = r + direction[0];
            int col = c + direction[1];

            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                if (get(row, col) == player.getOpponent().getPawn()
                        || get(row, col) == player.getOpponent().getPusher()) {
                    if (get(r, c) == player.getPusher()) {
                        return true;
                    }

                    for (int[] dir : protectDiagonals) {
                        int ro = r + dir[0];
                        int co = c + dir[1];

                        if (ro >= 0 && ro < 8 && co >= 0 && co < 8) {
                            if (get(ro, co) == player.getPusher()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean isPawnActivated(Player player, int row, int col) {
        for (int d = -1; d <= 1; d++) {
            if (col + d >= 0 && col + d < 8 && get(row - player.getDirection(), col + d) == player.getPusher()) {
                return true;
            }
        }

        return false;
    }

    public int countPotentialPushes(Player player) {
        int count = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (get(row, col) == player.getPusher()) {
                    int piece = get(row + player.getDirection(), col);
                    if (piece == player.getPawn()) {
                        count++;
                    }

                    if (col > 0) {
                        piece = get(row + player.getDirection(), col - 1);
                        if (piece == player.getPawn()) {
                            count++;
                        }
                    }

                    if (col < 7) {
                        piece = get(row + player.getDirection(), col + 1);
                        if (piece == player.getPawn()) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }
}
