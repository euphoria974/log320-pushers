package tests;

import log320.entities.Player;
import log320.game.Board;
import log320.game.CPUPlayer;

public class BoardTest {
    public static void main(String[] args) {
        Board b = new Board("0 2 2 0 0 0 2 0 " +
                "1 0 2 0 2 0 1 2 " +
                "0 0 3 0 1 0 0 1 " +
                "4 0 0 0 1 4 0 0 " +
                "0 0 1 0 0 0 0 3 " +
                "3 0 0 0 3 0 0 3 " +
                "4 3 0 4 3 0 3 4 " +
                "0 0 0 4 4 0 0 4 ");

        CPUPlayer cpuPlayer = new CPUPlayer(b, Player.BLACK);
        System.out.println(cpuPlayer.quiescenceSearch(b, Integer.MAX_VALUE, Integer.MIN_VALUE, Player.RED, System.currentTimeMillis()));
    }
}
