package tests;

import log320.entities.Player;
import log320.game.Board;
import log320.game.CPUPlayer;

public class BoardTest {
    public static void main(String[] args) {
        Board black = new Board("0 0 0 0 0 2 0 0 " +
                "1 0 2 2 0 2 0 0 " +
                "0 0 0 1 0 0 1 2 " +
                "0 0 0 4 1 1 0 1 " +
                "0 0 2 0 3 0 3 0 " +
                "3 4 0 0 4 3 0 3 " +
                "3 4 0 3 4 0 0 0 " +
                "4 0 0 0 0 0 0 4 "
        );
        black.print();
        System.out.println(new CPUPlayer(black, Player.BLACK).getNextMove());
    }
}
