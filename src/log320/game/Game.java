package log320.game;

import log320.entities.Move;
import log320.entities.Player;

import static log320.Const.ALL_MOVES;

public class Game {
    private Board board;
    private CPUPlayer cpuPlayer;

    public Game() {
    }

    public void start(String boardState, Player currentPlayer) {
        this.board = new Board(boardState);
        this.cpuPlayer = new CPUPlayer(board, currentPlayer);
    }

    public void start(Player currentPlayer) {
        this.board = new Board();
        this.board.init();
        this.cpuPlayer = new CPUPlayer(board, currentPlayer);
    }

    public Move getNextMove() {
        Move move = cpuPlayer.getNextMove();
        board.play(move);
        return move;
    }

    public void play(String moveString) {
        Move move = ALL_MOVES.get(moveString);
        board.play(move);
    }

    public void printBoard() {
        board.print();
    }

    public Move getLastMove() {
        return board.getLastMove();
    }
}
