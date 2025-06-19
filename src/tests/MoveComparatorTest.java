package tests;

import log320.*;

import java.util.ArrayList;
import java.util.List;

public class MoveComparatorTest {
    public static void main(String[] args) {
        Board board = new Board();
        board.place(0, 6, Player.RED.getPusher());
        board.place(1, 3, Player.RED.getPusher());
        MoveComparator moveComparator = new MoveComparator(board, Player.RED);

        Move move1 = Const.ALL_MOVES.get("A7B8");
        Move move2 = Const.ALL_MOVES.get("B4C5");

        List<Move> moves = new ArrayList<>();

        moves.add(move1);
        moves.add(move2);

        moves.sort(moveComparator);
        System.out.println("Sorted moves:");
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            System.out.println(move + " with score: " + moveComparator.getMoveScore(move));
        }
    }
}
