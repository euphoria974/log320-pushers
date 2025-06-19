package log320;

import java.util.ArrayList;

import static log320.Const.ALL_MOVES;
import static log320.Const.RANDOM;

public class Game {
    private final Board BOARD;
    private final CPUPlayer CPU_PLAYER;

    public Game(String boardState, Player currentPlayer) {
        this.BOARD = new Board(boardState);
        this.CPU_PLAYER = new CPUPlayer(BOARD, currentPlayer);
    }

    public Move getNextMove() {
        ArrayList<Move> moves = CPU_PLAYER.getNextMove();
        Move move = moves.get(RANDOM.nextInt(moves.size()));
        BOARD.play(move);
        return move;
    }

    public void play(Move move) {
        BOARD.play(move);
    }

    public void play(String moveString) {
        Move move = ALL_MOVES.get(moveString);
        BOARD.play(move);
    }

    public void printBoard() {
        BOARD.print();
    }

    public Move getLastMove() {
        return BOARD.getLastMove();
    }
}
