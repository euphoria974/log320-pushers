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
        zobristHash = ZobristHash.computeHash(this);
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

    public Board clone() {
        Board clone = new Board();
        clone.redPushers = this.redPushers;
        clone.redPawns = this.redPawns;
        clone.blackPushers = this.blackPushers;
        clone.blackPawns = this.blackPawns;
        clone.zobristHash = this.zobristHash;
        return clone;
    }

    public int getPieceFromIndex(int index) {
        long bit = 1L << index;
        if ((redPushers & bit) != 0) return RED_PUSHER;
        if ((redPawns & bit) != 0) return RED_PAWN;
        if ((blackPushers & bit) != 0) return BLACK_PUSHER;
        if ((blackPawns & bit) != 0) return BLACK_PAWN;
        return EMPTY;
    }

    public void play(Move move) {
        lastMove = move;

        int fromRow = move.getFrom() / 8;
        int fromCol = move.getFrom() % 8;
        int toRow = move.getTo() / 8;
        int toCol = move.getTo() % 8;

        long fromBit = 1L << move.getFrom();
        long toBit = 1L << move.getTo();

        int movedPiece = EMPTY;
        int capturedPiece = EMPTY;

        UndoMoveState ms = MOVE_STATE_POOL.get(moveStatePoolIndex++);
        MOVE_STACK.push(ms.set(redPushers, redPawns, blackPushers, blackPawns, zobristHash));

        // trouver la pièce qu'on bouge et l'enlever
        if ((redPushers & fromBit) != 0) {
            movedPiece = RED_PUSHER;
            redPushers &= ~fromBit;
        } else if ((redPawns & fromBit) != 0) {
            movedPiece = RED_PAWN;
            redPawns &= ~fromBit;
        } else if ((blackPushers & fromBit) != 0) {
            movedPiece = BLACK_PUSHER;
            blackPushers &= ~fromBit;
        } else if ((blackPawns & fromBit) != 0) {
            movedPiece = BLACK_PAWN;
            blackPawns &= ~fromBit;
        }

        // trouver la pièce qu'on capture et l'enlever
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

        // mettre la pièce à la nouvelle position
        if (movedPiece == RED_PUSHER) {
            redPushers |= toBit;
        } else if (movedPiece == RED_PAWN) {
            redPawns |= toBit;
        } else if (movedPiece == BLACK_PUSHER) {
            blackPushers |= toBit;
        } else if (movedPiece == BLACK_PAWN) {
            blackPawns |= toBit;
        }

        // mettre à jour le hash
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

            // diagonal gauche << 7
            long pusherDiagLeft = ((redPushers & ~MASK_COL_A) << 7) & ~redPieces;
            possibleMoves.addAll(generateMovesFromBitboard(pusherDiagLeft, 7));

            // diagonal droite << 9
            long pusherDiagRight = ((redPushers & ~MASK_COL_H) << 9) & ~redPieces;
            possibleMoves.addAll(generateMovesFromBitboard(pusherDiagRight, 9));

            // Pions
            // up << 8
            long forwardPawns = redPawns & (redPushers << 8);
            long forwardTo = (forwardPawns << 8) & ~occ;
            possibleMoves.addAll(generateMovesFromBitboard(forwardTo, 8));

            // diagonal gauche << 7
            long diagLeftPawns = (redPawns & ~MASK_COL_H) & ((redPushers & ~MASK_COL_A) << 7);
            long diagLeftTo = (diagLeftPawns << 7) & ~redPieces & ~MASK_COL_H;
            possibleMoves.addAll(generateMovesFromBitboard(diagLeftTo, 7));

            // diagonal droite << 9
            long diagRightPawns = (redPawns & ~MASK_COL_A) & ((redPushers & ~MASK_COL_H) << 9);
            long diagRightTo = (diagRightPawns << 9) & ~redPieces & ~MASK_COL_A;
            possibleMoves.addAll(generateMovesFromBitboard(diagRightTo, 9));
        } else {
            // Pushers
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

            // diagonal gauche >> 7
            long diagLeftPawns = (blackPawns & ~MASK_COL_A) & ((blackPushers & ~MASK_COL_H) >> 7);
            long diagLeftTo = (diagLeftPawns >> 7) & ~blackPieces & ~MASK_COL_A;
            possibleMoves.addAll(generateMovesFromBitboard(diagLeftTo, -7));

            // diagonal droite >> 9
            long diagRightPawns = (blackPawns & ~MASK_COL_H) & ((blackPushers & ~MASK_COL_A) >> 9);
            long diagRightTo = (diagRightPawns >> 9) & ~blackPieces & ~MASK_COL_H;
            possibleMoves.addAll(generateMovesFromBitboard(diagRightTo, -9));
        }

        return possibleMoves;
    }

    public List<Move> getCaptureMoves(Player player) {
        return getPossibleMoves(player).stream().filter(move -> canEat(player, move.getFrom() / 8, move.getFrom() % 8)).toList();
    }

    public boolean hasPlayerWon(Player player) {
        if (player == Player.RED) {
            return (allReds() & 0xFF00000000000000L) != 0 || blackPushers == 0L;
        } else {
            return (allBlacks() & 0x00000000000000FFL) != 0 || redPushers == 0L;
        }
    }

    public int evaluate(Player player) {
        long myPushers = player == Player.RED ? getRedPushers() : getBlackPushers();
        long opponentPushers = player.getOpponent() == Player.RED ? getRedPushers() : getBlackPushers();
        long myPawns = player == Player.RED ? getRedPawns() : getBlackPawns();
        long opponentPawns = player.getOpponent() == Player.RED ? getRedPawns() : getBlackPawns();

        int pushersDiff = Long.bitCount(myPushers) - Long.bitCount(opponentPushers);
        int pawnsDiff = Long.bitCount(myPawns) - Long.bitCount(opponentPawns);

        int backedPushers = getBackedPushers(player) - getBackedPushers(player.getOpponent());
        int halfBoard = getHalfBoardScore(player) - getHalfBoardScore(player.getOpponent());
        int mobility = getPossibleMovesSize(player) - getPossibleMovesSize(player.getOpponent());

        return PUSHER_WEIGHT * pushersDiff +
                PAWN_WEIGHT * pawnsDiff +
                BACKED_PUSHER_WEIGHT * backedPushers +
                HALF_BOARD_WEIGHT * halfBoard +
                MOBILITY_WEIGHT * mobility;
    }

    public boolean isExposed(Player player, int row, int col) {
        int forwardRow = row + player.getDirection();
        int doubleForwardRow = forwardRow + player.getDirection();

        if (forwardRow < 0 || forwardRow > 7) {
            return false;
        }

        int forwardIndex = forwardRow * 8 + col;
        int doubleForwardIndex = doubleForwardRow * 8 + col;

        long opponentPushers = player == Player.RED ? blackPushers : redPushers;
        long opponentPawns = player == Player.RED ? blackPawns : redPawns;

        // check pour pusher en diagonale gauche
        if (col > 0) {
            long leftDiagBit = 1L << (forwardIndex - 1);
            if ((opponentPushers & leftDiagBit) != 0) {
                return true;
            }
        }

        // check pour pusher en diagonale droite
        if (col < 7) {
            long rightDiagBit = 1L << (forwardIndex + 1);
            if ((opponentPushers & rightDiagBit) != 0) {
                return true;
            }
        }

        // check pour pion activé en diagonale gauche
        if (col > 1 && doubleForwardRow >= 0 && doubleForwardRow < 8) {
            long leftPawnBit = 1L << (forwardIndex - 1);
            long leftPusherBit = 1L << (doubleForwardIndex - 2);
            if ((opponentPawns & leftPawnBit) != 0 && (opponentPushers & leftPusherBit) != 0) {
                return true;
            }
        }

        // check pour pion activé en diagonale droite
        if (col < 6 && doubleForwardRow >= 0 && doubleForwardRow < 8) {
            long rightPawnBit = 1L << (forwardIndex + 1);
            long rightPusherBit = 1L << (doubleForwardIndex + 2);
            if ((opponentPawns & rightPawnBit) != 0 && (opponentPushers & rightPusherBit) != 0) {
                return true;
            }
        }

        return false;
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

    private void set(int row, int col, int piece) {
        int index = row * 8 + col;
        long bit = 1L << index;

        // remove old piece if any
        redPushers &= ~bit;
        redPawns &= ~bit;
        blackPushers &= ~bit;
        blackPawns &= ~bit;

        // add new piece
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
                break;
        }
    }

    private boolean canEat(Player player, int row, int col) {
        int rowToCheck = row + player.getDirection();

        if (rowToCheck < 0 || rowToCheck > 7) {
            return false;
        }

        long opponentPieces = player.getOpponent() == Player.RED ?
                redPushers :
                blackPushers;

        int leftCol = col - 1;
        int rightCol = col + 1;

        // diagonal gauche
        if (leftCol >= 0 && ((opponentPieces & (1L << (rowToCheck * 8 + leftCol))) != 0)) {
            return true;
        }

        // diagonal droite
        if (rightCol < 8 && ((opponentPieces & (1L << (rowToCheck * 8 + rightCol))) != 0)) {
            return true;
        }

        return false;
    }

    private int getBackedPushers(Player player) {
        long pushers = player == Player.RED ? redPushers : blackPushers;
        long myPieces = player == Player.RED ? allReds() : allBlacks();

        int backedPushers = 0;

        for (int i = 0; i < 64; i++) {
            if ((pushers & (1L << i)) != 0) {
                int row = i / 8;
                int col = i % 8;

                // ddiagonal gauche
                if (row > 0 && col > 0) {
                    int diagLeftIdx = (row - 1) * 8 + (col - 1);
                    if ((myPieces & (1L << diagLeftIdx)) != 0) {
                        backedPushers++;
                        continue;
                    }
                }

                // diagonal droite
                if (row > 0 && col < 7) {
                    int diagRightIdx = (row - 1) * 8 + (col + 1);
                    if ((myPieces & (1L << diagRightIdx)) != 0) {
                        backedPushers++;
                    }
                }
            }
        }

        return backedPushers;
    }

    private int getHalfBoardScore(Player player) {
        long pushers = player == Player.RED ? redPushers : blackPushers;
        int count = 0;

        if (player == Player.RED) {
            // rows 4-7
            for (int row = 4; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    int idx = row * 8 + col;

                    if ((pushers & (1L << idx)) != 0) {
                        count++;
                    }
                }
            }
        } else {
            // rows 0-3
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 8; col++) {
                    int idx = row * 8 + col;

                    if ((pushers & (1L << idx)) != 0) {
                        count++;
                    }
                }
            }
        }

        return count;
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

    private int getPossibleMovesSize(Player player) {
        int size = 0;

        long occ = occupied();
        long blackPieces = allBlacks();
        long redPieces = allReds();

        if (player == Player.RED) {
            // Pushers
            // up << 8
            long pusherForward = (redPushers << 8) & ~occ;
            size += generateMovesFromBitboardSize(pusherForward, 8);

            // diagonal gauche << 7
            long pusherDiagLeft = ((redPushers & ~MASK_COL_A) << 7) & ~redPieces;
            size += generateMovesFromBitboardSize(pusherDiagLeft, 7);

            // diagonal droite << 9
            long pusherDiagRight = ((redPushers & ~MASK_COL_H) << 9) & ~redPieces;
            size += generateMovesFromBitboardSize(pusherDiagRight, 9);

            // Pions
            // up << 8
            long forwardPawns = redPawns & (redPushers << 8);
            long forwardTo = (forwardPawns << 8) & ~occ;
            size += generateMovesFromBitboardSize(forwardTo, 8);

            // diagonal gauche << 7
            long diagLeftPawns = (redPawns & ~MASK_COL_H) & ((redPushers & ~MASK_COL_A) << 7);
            long diagLeftTo = (diagLeftPawns << 7) & ~redPieces & ~MASK_COL_H;
            size += generateMovesFromBitboardSize(diagLeftTo, 7);

            // diagonal droite << 9
            long diagRightPawns = (redPawns & ~MASK_COL_A) & ((redPushers & ~MASK_COL_H) << 9);
            long diagRightTo = (diagRightPawns << 9) & ~redPieces & ~MASK_COL_A;
            size += generateMovesFromBitboardSize(diagRightTo, 9);
        } else {
            // Pushers
            // down >> 8
            long pusherForward = (blackPushers >> 8) & ~occ & ~(0xFFL << 56);
            size += generateMovesFromBitboardSize(pusherForward, -8);

            // diagonal droite >> 7
            long pusherDiagRight = ((blackPushers & ~MASK_COL_H) >> 7) & ~blackPieces;
            size += generateMovesFromBitboardSize(pusherDiagRight, -7);

            // diagonal gauche >> 9
            long pusherDiagLeft = ((blackPushers & ~MASK_COL_A) >> 9) & ~blackPieces & 0x007F7F7F7F7F7F7FL;
            size += generateMovesFromBitboardSize(pusherDiagLeft, -9);

            // Pions
            // up >> 8
            long forwardPawns = blackPawns & (blackPushers >> 8); // pushers directly behind
            long forwardTo = (forwardPawns >> 8) & ~occ; // destination must be empty
            size += generateMovesFromBitboardSize(forwardTo, -8);

            // diagonal gauche >> 7
            long diagLeftPawns = (blackPawns & ~MASK_COL_A) & ((blackPushers & ~MASK_COL_H) >> 7);
            long diagLeftTo = (diagLeftPawns >> 7) & ~blackPieces & ~MASK_COL_A;
            size += generateMovesFromBitboardSize(diagLeftTo, -7);

            // diagonal droite >> 9
            long diagRightPawns = (blackPawns & ~MASK_COL_H) & ((blackPushers & ~MASK_COL_A) >> 9);
            long diagRightTo = (diagRightPawns >> 9) & ~blackPieces & ~MASK_COL_H;
            size += generateMovesFromBitboardSize(diagRightTo, -9);
        }

        return size;
    }

    private int generateMovesFromBitboardSize(long toBits, int shift) {
        int size = 0;

        while (toBits != 0) {
            int toIndex = Long.numberOfTrailingZeros(toBits);
            size++;
            toBits &= toBits - 1;
        }

        return size;
    }
}
