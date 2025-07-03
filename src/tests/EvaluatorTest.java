package tests;

import log320.entities.Player;
import log320.game.Board;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EvaluatorTest {
    public static void main(String[] args) {
        Path path = Paths.get("boardStates", "test7.brd");
        String content;
        try {
            List<String> lines = Files.readAllLines(path);
            content = String.join("", lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Board board = new Board(content.substring(1));
        System.out.println("red score: " + board.evaluate(Player.RED));
        System.out.println("black score: " + board.evaluate(Player.BLACK));

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board.isExposed(Player.RED, i, j);
                board.isExposed(Player.BLACK, i, j);

                board.set(i, j, Player.RED.getPawn());
                board.set(i, j, Player.BLACK.getPawn());
                board.isPawnActivated(Player.RED, i, j);
                board.isPawnActivated(Player.BLACK, i, j);
            }
        }
    }
}
