package tests;

import log320.entities.Player;
import log320.game.Board;
import log320.game.CPUPlayer;

public class BoardTest {
    public static void main(String[] args) {
        Board black = new Board("0 0 0 0 0 0 0 0 " +
                "0 0 0 0 0 0 0 0 " +
                "0 0 2 0 0 0 0 0 " +
                "0 0 0 1 0 0 0 0 " +
                "0 3 0 0 0 0 0 0 " +
                "4 0 0 0 0 0 0 0 " +
                "0 0 0 0 0 0 0 0 " +
                "0 0 0 0 0 0 0 0 ");
        black.print();
        CPUPlayer blackPlayer = new CPUPlayer(black, Player.BLACK);
        System.out.println(blackPlayer.getNextMove());

        Board red = new Board("0 0 0 0 0 0 0 0 " +
                "0 0 0 0 0 0 0 0 " +
                "0 0 0 0 0 0 0 0 " +
                "0 0 2 1 0 0 0 0 " +
                "0 3 0 0 0 0 0 0 " +
                "4 0 0 0 0 0 0 0 " +
                "0 0 0 0 0 0 0 0 " +
                "0 0 0 0 0 0 0 0 ");
        red.print();
        CPUPlayer redPlayer = new CPUPlayer(red, Player.RED);
        System.out.println(redPlayer.getNextMove());
    }
}
