package log320.game;

import log320.entities.Move;
import log320.entities.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static log320.Const.ALL_MOVES;
import static log320.Const.WIN_SCORE;

public class Game {
    private final List<Move> PLAYED_MOVES = new ArrayList<>();

    private Board board;
    private CPUPlayer cpuPlayer;
    private Player currentPlayer;

    public Game() {
    }

    public void start(String boardState, Player currentPlayer) {
        this.board = new Board(boardState);
        this.cpuPlayer = new CPUPlayer(board, currentPlayer);
        this.currentPlayer = currentPlayer;
    }

    public void start(Player currentPlayer) {
        this.board = new Board();
        this.board.init();
        this.cpuPlayer = new CPUPlayer(board, currentPlayer);
        this.currentPlayer = currentPlayer;
    }

    public void over() {
        if (board.evaluate(currentPlayer) == WIN_SCORE) {
            return;
        }

        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/" + currentPlayer.name() + "-" + UUID.randomUUID() + ".txt"))) {
            for (Move move : PLAYED_MOVES) {
                writer.write(move.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Move getNextMove() {
        Move move = cpuPlayer.getNextMove();
        board.play(move);
        PLAYED_MOVES.add(move);
        return move;
    }

    public void play(String moveString) {
        Move move = ALL_MOVES.get(moveString);
        board.play(move);

        Move lastMove = !PLAYED_MOVES.isEmpty() ? PLAYED_MOVES.getLast() : null;
        if (lastMove == null || !lastMove.toString().equals(move.toString())) {
            PLAYED_MOVES.add(move);
        }
    }

    public void undo() {
        board.undo();
    }

    public void printBoard() {
        board.print();
    }

    public Move getLastMove() {
        return board.getLastMove();
    }
}
