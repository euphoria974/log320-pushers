package tests;

import log320.Const;
import log320.entities.Move;
import log320.entities.Player;
import log320.game.Board;
import log320.game.MoveComparator;

import java.util.ArrayList;
import java.util.List;

public class MoveComparatorTest {
    public static void main(String[] args) {
        Board board = new Board();
        board.set(0, 6, Player.RED.getPusher());
        board.set(1, 3, Player.RED.getPusher());
        MoveComparator moveComparator = new MoveComparator(board, Player.RED);

        Move move1 = Const.ALL_MOVES.get("A7B8");
        Move move2 = Const.ALL_MOVES.get("B4C5");

        List<Move> moves = new ArrayList<>();

        moves.add(move1);
        moves.add(move2);

        moves.sort(moveComparator);
        System.out.println("Sorted moves:");
        for (Move move : moves) {
            System.out.println(move + " with score: " + moveComparator.getMoveScore(move));
        }
    }
}
