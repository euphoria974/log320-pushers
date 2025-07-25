package tests;

import log320.entities.Player;
import log320.game.Board;

import static log320.Const.ALL_MOVES;

public class BoardTest {
    public static void main(String[] args) {
        Board b = new Board("2 0 0 0 0 2 0 0 " +
                "0 2 1 0 0 1 2 2 " +
                "1 0 1 2 2 2 1 1 " +
                "0 0 0 0 1 0 0 0 " +
                "3 1 3 3 0 0 3 0 " +
                "0 4 3 3 4 4 0 3 " +
                "0 4 0 0 0 0 0 3 " +
                "0 4 0 0 4 0 4 4 ");

        System.out.println(b.evaluate(Player.RED));

        b.play(ALL_MOVES.get("F3E4"));
        System.out.println(b.evaluate(Player.RED));

        b.undo();
        b.play(ALL_MOVES.get("H1G2"));
        System.out.println(b.evaluate(Player.RED));
    }
}
