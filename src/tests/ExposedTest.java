package tests;

import log320.entities.Player;
import log320.game.Board;

import static log320.Const.*;

public class ExposedTest {
    public static void main(String[] args) {
        Board b = new Board();
        b.set(3, 3, RED_PUSHER);
        b.set(4, 4, BLACK_PUSHER);

        System.out.println("isExposed: " + b.isExposed(Player.RED, 3, 3)); // true
        System.out.println("isExposed: " + b.isExposed(Player.BLACK, 4, 4)); // true

        b.set(4, 4, BLACK_PAWN);

        System.out.println("isExposed: " + b.isExposed(Player.RED, 3, 3)); // false

        b.clear();

        b.set(3, 3, RED_PUSHER);
        b.set(4, 2, BLACK_PUSHER);

        b.print();

        System.out.println("isExposed: " + b.isExposed(Player.RED, 3, 3)); // true
        System.out.println("isExposed: " + b.isExposed(Player.BLACK, 4, 2)); // true !!!!!

        b.set(4, 2, BLACK_PAWN);

        System.out.println("isExposed: " + b.isExposed(Player.RED, 3, 3)); // false

        b.clear();

        b.set(4, 0, RED_PUSHER);
        b.set(5, 1, BLACK_PAWN);
        b.set(6, 2, BLACK_PUSHER);

        System.out.println("isExposed: " + b.isExposed(Player.RED, 4, 0)); // true

        System.out.println(b.evaluate(Player.RED));

        Board t = new Board("0 0 0 0 0 2 2 0 " +
                "0 2 2 1 4 0 0 1 " +
                "0 0 2 0 4 0 0 2 " +
                "1 0 1 0 0 4 1 0 " +
                "3 0 1 0 2 0 4 4 " +
                "0 0 3 0 0 3 0 3 " +
                "4 0 0 0 3 0 4 3 " +
                "4 0 0 0 0 0 0 4");

        System.out.println("isExposed: " + t.isExposed(Player.BLACK, 3, 4)); // true
        System.out.println("evaluate: " + t.evaluate(Player.BLACK));

        t.init();
        System.out.println("evaluate: " + t.evaluate(Player.BLACK));
    }
}
