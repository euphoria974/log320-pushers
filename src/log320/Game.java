package log320;

import java.util.ArrayList;

import static log320.Const.RANDOM;

public class Game {
    private final Board BOARD;
    private final CPUPlayer CPU_PLAYER;

    public Game(String boardState, Player currentPlayer) {
        this.BOARD = new Board(boardState, currentPlayer);
        this.CPU_PLAYER = new CPUPlayer(BOARD, currentPlayer);
    }

    public String getNextMove() {
        ArrayList<String> moves = CPU_PLAYER.getNextMove();
        String move = moves.get(RANDOM.nextInt(moves.size()));
        BOARD.play(move);
        return move;
    }

    public void play(String move) {
        BOARD.play(move);
    }

    public void printBoard() {
        BOARD.print();
    }

    public String getLastMove() {
        return BOARD.getLastMove();
    }
}
