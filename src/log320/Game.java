package log320;

import static log320.Const.ALL_MOVES;

public class Game {
    private final Board BOARD;
    private final CPUPlayer CPU_PLAYER;

    public Game(String boardState, Player currentPlayer) {
        this.BOARD = new Board(boardState);
        this.CPU_PLAYER = new CPUPlayer(BOARD, currentPlayer);
    }

    public Move getNextMove() {
        Move move = CPU_PLAYER.getNextMove();
        BOARD.play(move);
        return move;
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
