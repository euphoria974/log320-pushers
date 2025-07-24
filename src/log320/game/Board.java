package log320.game;

import log320.entities.Move;
import log320.entities.Player;
import log320.entities.UndoMoveState;
import log320.transposition.ZobristHash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static log320.Const.*;

public class Board {
    private static final long MASK_COL_A = 0x0101010101010101L;
    private static final long MASK_COL_H = 0x8080808080808080L;

    private final MoveComparator MOVE_COMPARATOR_RED = new MoveComparator(this, Player.RED);
    private final MoveComparator MOVE_COMPARATOR_BLACK = new MoveComparator(this, Player.BLACK);
    private final List<UndoMoveState> MOVE_STATE_POOL = new ArrayList<>(1000);
    private final Stack<UndoMoveState> MOVE_STACK = new Stack<>();
    private final BoardEvaluator EVALUATOR = new BoardEvaluator(this);

    private long redPushers = 0L;
    private long redPawns = 0L;
    private long blackPushers = 0L;
    private long blackPawns = 0L;

    private Move lastMove = null;
    private int moveStatePoolIndex = 0;
    private long zobristHash = 0L;

    public Board() {
        for (int i = 0; i < 1000; i++) {
            MOVE_STATE_POOL.add(new UndoMoveState());
        }
    }

    public Board(String s) {
        this();
        build(s);
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

        zobristHash = ZobristHash.computeHash(this);
    }

    public void init() {
        redPushers = 0x00000000000000FFL;
        redPawns = 0x000000000000FF00L;
        blackPushers = 0xFF00000000000000L;
        blackPawns = 0x00FF000000000000L;

        zobristHash = ZobristHash.computeHash(this);
    }

    public void clear() {
        redPushers = 0L;
        redPawns = 0L;
        blackPushers = 0L;
        blackPawns = 0L;

        zobristHash = 0L;
    }

    public void print() {
        char[] board = new char[64];
        Arrays.fill(board, 'x');

        for (int i = 0; i < 64; i++) {
            long bit = 1L << i;
            if ((redPushers & bit) != 0) board[i] = 'R';
            if ((redPawns & bit) != 0) board[i] = 'r';
            if ((blackPushers & bit) != 0) board[i] = 'B';
            if ((blackPawns & bit) != 0) board[i] = 'b';
        }

        for (int row = 7; row >= 0; row--) {
            for (int col = 0; col < 8; col++) {
                System.out.print(board[row * 8 + col] + " ");
            }

            System.out.println();
        }

        System.out.println("==========================");
    }


    public boolean isExposed(Player player, int row, int col) {
        int forwardRow = row + player.getDirection();
        int doubleForwardRow = forwardRow + player.getDirection();

        if (forwardRow < 0 || forwardRow > 7) {
            return false;
        }

        // Convert positions to indices
        int forwardIndex = forwardRow * 8 + col;
        int doubleForwardIndex = doubleForwardRow * 8 + col;

        // Get opponent pieces
        long opponentPushers = player == Player.RED ? blackPushers : redPushers;
        long opponentPawns = player == Player.RED ? blackPawns : redPawns;

        // Check for opponent pushers at forward diagonal positions
        if (col > 0) {
            long leftDiagBit = 1L << (forwardIndex - 1);
            if ((opponentPushers & leftDiagBit) != 0) {
                return true;
            }
        }

        if (col < 7) {
            long rightDiagBit = 1L << (forwardIndex + 1);
            if ((opponentPushers & rightDiagBit) != 0) {
                return true;
            }
        }

        // Check for opponent pawns with pushers behind them (left diagonal)
        if (col > 1 && doubleForwardRow >= 0 && doubleForwardRow < 8) {
            long leftPawnBit = 1L << (forwardIndex - 1);
            long leftPusherBit = 1L << (doubleForwardIndex - 2);
            if ((opponentPawns & leftPawnBit) != 0 && (opponentPushers & leftPusherBit) != 0) {
                return true;
            }
        }

        // Check for opponent pawns with pushers behind them (right diagonal)
        if (col < 6 && doubleForwardRow >= 0 && doubleForwardRow < 8) {
            long rightPawnBit = 1L << (forwardIndex + 1);
            long rightPusherBit = 1L << (doubleForwardIndex + 2);
            if ((opponentPawns & rightPawnBit) != 0 && (opponentPushers & rightPusherBit) != 0) {
                return true;
            }
        }

        return false;
    }

    public Board clone() {
        Board clone = new Board();
        clone.redPushers = this.redPushers;
        clone.redPawns = this.redPawns;
        clone.blackPushers = this.blackPushers;
        clone.blackPawns = this.blackPawns;
        clone.zobristHash = this.zobristHash;
        return clone;
    }

    // permet de cloner et joeur un move
    public Board clone(Move move) {
        Board clone = clone();
        clone.play(move);
        return clone;
    }

    public void play(Move move) {
        lastMove = move;

        // Convert indices to row/col for compatibility
        int fromRow = move.getFrom() / 8;
        int fromCol = move.getFrom() % 8;
        int toRow = move.getTo() / 8;
        int toCol = move.getTo() % 8;

        // Determine which piece is being moved
        long fromBit = 1L << move.getFrom();
        long toBit = 1L << move.getTo();

        int movedPiece = EMPTY;
        int capturedPiece = EMPTY;

        UndoMoveState ms = MOVE_STATE_POOL.get(moveStatePoolIndex++);
        MOVE_STACK.push(ms.set(redPushers, redPawns, blackPushers, blackPawns, zobristHash));

        // Find the moved piece
        if ((redPushers & fromBit) != 0) {
            movedPiece = RED_PUSHER;
            redPushers &= ~fromBit; // Remove from source
        } else if ((redPawns & fromBit) != 0) {
            movedPiece = RED_PAWN;
            redPawns &= ~fromBit; // Remove from source
        } else if ((blackPushers & fromBit) != 0) {
            movedPiece = BLACK_PUSHER;
            blackPushers &= ~fromBit; // Remove from source
        } else if ((blackPawns & fromBit) != 0) {
            movedPiece = BLACK_PAWN;
            blackPawns &= ~fromBit; // Remove from source
        }

        // Find the captured piece (if any)
        if ((redPushers & toBit) != 0) {
            capturedPiece = RED_PUSHER;
            redPushers &= ~toBit;
        } else if ((redPawns & toBit) != 0) {
            capturedPiece = RED_PAWN;
            redPawns &= ~toBit;
        } else if ((blackPushers & toBit) != 0) {
            capturedPiece = BLACK_PUSHER;
            blackPushers &= ~toBit;
        } else if ((blackPawns & toBit) != 0) {
            capturedPiece = BLACK_PAWN;
            blackPawns &= ~toBit;
        }

        if (movedPiece == RED_PUSHER) {
            redPushers |= toBit;
        } else if (movedPiece == RED_PAWN) {
            redPawns |= toBit;
        } else if (movedPiece == BLACK_PUSHER) {
            blackPushers |= toBit;
        } else if (movedPiece == BLACK_PAWN) {
            blackPawns |= toBit;
        }

        zobristHash = ZobristHash.updateHash(zobristHash, fromRow, fromCol, movedPiece, toRow, toCol, capturedPiece);
    }

    public void undo() {
        if (MOVE_STACK.isEmpty()) return;
        UndoMoveState ms = MOVE_STACK.pop();

        redPushers = ms.redPushers;
        redPawns = ms.redPawns;
        blackPushers = ms.blackPushers;
        blackPawns = ms.blackPawns;
        zobristHash = ms.zobristHash;

        moveStatePoolIndex--;
    }

    public int evaluate(Player player) {
        return EVALUATOR.evaluate(player);
    }

    public ArrayList<Move> getSortedPossibleMoves(Player player) {
        ArrayList<Move> possibleMoves = getPossibleMoves(player);
        possibleMoves.sort(player == Player.RED ? MOVE_COMPARATOR_RED : MOVE_COMPARATOR_BLACK);
        return possibleMoves;
    }

    public ArrayList<Move> getPossibleMoves(Player player) {
        ArrayList<Move> possibleMoves = new ArrayList<>(32);

        long occ = occupied();
        long blackPieces = allBlacks();
        long redPieces = allReds();

        if (player == Player.RED) {
            // Pushers
            // up << 8
            long pusherForward = (redPushers << 8) & ~occ;
            possibleMoves.addAll(generateMovesFromBitboard(pusherForward, 8));

            // diaognal gauche << 7
            long pusherDiagLeft = ((redPushers & ~MASK_COL_A) << 7) & ~redPieces;
            possibleMoves.addAll(generateMovesFromBitboard(pusherDiagLeft, 7));

            // diaognal droite << 9
            long pusherDiagRight = ((redPushers & ~MASK_COL_H) << 9) & ~redPieces;
            possibleMoves.addAll(generateMovesFromBitboard(pusherDiagRight, 9));

            // Pions
            // up << 8
            long forwardPawns = redPawns & (redPushers << 8);
            long forwardTo = (forwardPawns << 8) & ~occ;
            possibleMoves.addAll(generateMovesFromBitboard(forwardTo, 8));

            // diaognal gauche << 7
            long diagLeftPawns = (redPawns & ~MASK_COL_H) & ((redPushers & ~MASK_COL_A) << 7);
            long diagLeftTo = (diagLeftPawns << 7) & ~occ & ~MASK_COL_H;
            possibleMoves.addAll(generateMovesFromBitboard(diagLeftTo, 7));

            // diaognal droite << 9
            long diagRightPawns = (redPawns & ~MASK_COL_A) & ((redPushers & ~MASK_COL_H) << 9);
            long diagRightTo = (diagRightPawns << 9) & ~occ & ~MASK_COL_A;
            possibleMoves.addAll(generateMovesFromBitboard(diagRightTo, 9));
        } else {
            // down >> 8
            long pusherForward = (blackPushers >> 8) & ~occ & ~(0xFFL << 56);
            possibleMoves.addAll(generateMovesFromBitboard(pusherForward, -8));

            // diagonal droite >> 7
            long pusherDiagRight = ((blackPushers & ~MASK_COL_H) >> 7) & ~blackPieces;
            possibleMoves.addAll(generateMovesFromBitboard(pusherDiagRight, -7));

            // diagonal gauche >> 9
            long pusherDiagLeft = ((blackPushers & ~MASK_COL_A) >> 9) & ~blackPieces & 0x007F7F7F7F7F7F7FL;
            possibleMoves.addAll(generateMovesFromBitboard(pusherDiagLeft, -9));

            // Pions
            // up >> 8
            long forwardPawns = blackPawns & (blackPushers >> 8); // pushers directly behind
            long forwardTo = (forwardPawns >> 8) & ~occ; // destination must be empty
            possibleMoves.addAll(generateMovesFromBitboard(forwardTo, -8));

            // diaognal gauche >> 7
            long diagLeftPawns = (blackPawns & ~MASK_COL_A) & ((blackPushers & ~MASK_COL_H) >> 7);
            long diagLeftTo = (diagLeftPawns >> 7) & ~blackPieces & ~MASK_COL_A;
            possibleMoves.addAll(generateMovesFromBitboard(diagLeftTo, -7));

            // diaognal droite >> 9
            long diagRightPawns = (blackPawns & ~MASK_COL_H) & ((blackPushers & ~MASK_COL_A) >> 9);
            long diagRightTo = (diagRightPawns >> 9) & ~blackPieces & ~MASK_COL_H;
            possibleMoves.addAll(generateMovesFromBitboard(diagRightTo, -9));
        }

        return possibleMoves;
    }

    private ArrayList<Move> generateMovesFromBitboard(long toBits, int shift) {
        ArrayList<Move> moves = new ArrayList<>();

        while (toBits != 0) {
            int toIndex = Long.numberOfTrailingZeros(toBits);
            int fromIndex = toIndex - shift;
            moves.add(ALL_MOVES.get(Move.toString(fromIndex, toIndex)));
            toBits &= toBits - 1;
        }

        return moves;
    }

    public boolean hasPlayerWon(Player player) {
        if (player == Player.RED) {
            return (redPushers & 0xFF00000000000000L) != 0 || blackPushers == 0L;
        } else {
            return (blackPushers & 0x00000000000000FFL) != 0 || redPushers == 0L;
        }
    }

    public boolean isPawnActivated(Player player, int row, int col) {
        int backRow = row - player.getDirection();

        if (backRow < 0 || backRow > 7) {
            return false;
        }

        long playerPushers = player == Player.RED ? redPushers : blackPushers;

        // Check for pusher directly behind
        long behindBit = 1L << (backRow * 8 + col);
        if ((playerPushers & behindBit) != 0) {
            return true;
        }

        // Check for pusher behind diagonally left
        if (col > 0) {
            long leftDiagBit = 1L << (backRow * 8 + col - 1);
            if ((playerPushers & leftDiagBit) != 0) {
                return true;
            }
        }

        // Check for pusher behind diagonally right
        if (col < 7) {
            long rightDiagBit = 1L << (backRow * 8 + col + 1);
            if ((playerPushers & rightDiagBit) != 0) {
                return true;
            }
        }

        return false;
    }

    public boolean canEat(Player player, boolean onlyPushers, int row, int col) {
        int rowToCheck = row + player.getDirection();

        if (rowToCheck < 0 || rowToCheck > 7) {
            return false;
        }

        long opponentPieces = player.getOpponent() == Player.RED ?
                (onlyPushers ? redPushers : allReds()) :
                (onlyPushers ? blackPushers : allBlacks());

        int leftCol = col - 1;
        int rightCol = col + 1;

        // Check left diagonal
        if (leftCol >= 0 && ((opponentPieces & (1L << (rowToCheck * 8 + leftCol))) != 0)) {
            return true;
        }

        // Check right diagonal
        if (rightCol < 8 && ((opponentPieces & (1L << (rowToCheck * 8 + rightCol))) != 0)) {
            return true;
        }

        return false;
    }

    public void set(int row, int col, int piece) {
        int index = row * 8 + col;
        long bit = 1L << index;

        // Clear the position from all bitboards first
        redPushers &= ~bit;
        redPawns &= ~bit;
        blackPushers &= ~bit;
        blackPawns &= ~bit;

        // Set the piece in the appropriate bitboard
        switch (piece) {
            case RED_PUSHER:
                redPushers |= bit;
                break;
            case RED_PAWN:
                redPawns |= bit;
                break;
            case BLACK_PUSHER:
                blackPushers |= bit;
                break;
            case BLACK_PAWN:
                blackPawns |= bit;
                break;
            case EMPTY:
                // Already cleared above, do nothing
                break;
        }
    }

    public int get(int row, int col) {
        int index = row * 8 + col;
        long bit = 1L << index;

        // Check each bitboard to see which piece is at this position
        if ((redPushers & bit) != 0) {
            return RED_PUSHER;
        } else if ((redPawns & bit) != 0) {
            return RED_PAWN;
        } else if ((blackPushers & bit) != 0) {
            return BLACK_PUSHER;
        } else if ((blackPawns & bit) != 0) {
            return BLACK_PAWN;
        } else {
            return EMPTY;
        }
    }

    public int get(int index) {
        long bit = 1L << index;
        if ((redPushers & bit) != 0) return RED_PUSHER;
        if ((redPawns & bit) != 0) return RED_PAWN;
        if ((blackPushers & bit) != 0) return BLACK_PUSHER;
        if ((blackPawns & bit) != 0) return BLACK_PAWN;
        return EMPTY;
    }

    public List<Move> getNoisyMoves(Player player) {
        return getPossibleMoves(player).stream().filter(move -> canEat(player, true, move.getFrom() / 8, move.getFrom() % 8)).toList();
    }

    public long allReds() {
        return redPushers | redPawns;
    }

    public long allBlacks() {
        return blackPushers | blackPawns;
    }

    public long occupied() {
        return allReds() | allBlacks();
    }

    public long getRedPushers() {
        return redPushers;
    }

    public long getRedPawns() {
        return redPawns;
    }

    public long getBlackPushers() {
        return blackPushers;
    }

    public long getBlackPawns() {
        return blackPawns;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public long getHash() {
        return zobristHash;
    }
}
