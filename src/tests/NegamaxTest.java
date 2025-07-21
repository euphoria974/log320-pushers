package tests;

import log320.entities.Player;
import log320.game.Board;
import log320.game.CPUPlayer;

public class NegamaxTest {
    public static void main(String[] args) {
        Board board = new Board("0 0 0 0 0 2 2 0 " +
                "0 2 2 1 0 0 0 1 " +
                "0 0 2 0 4 0 0 2 " +
                "1 0 1 0 0 4 1 0 " +
                "3 0 1 0 2 0 4 4 " +
                "0 0 2 0 0 3 0 3 " +
                "4 0 0 0 3 0 4 3 " +
                "4 0 0 0 0 0 0 4");

        System.out.println("red score: " + board.evaluate(Player.RED));
        System.out.println("black score: " + board.evaluate(Player.BLACK));

        CPUPlayer cpuPlayer = new CPUPlayer(board, Player.RED);

        System.out.println(cpuPlayer.getNextMove());
    }
}
