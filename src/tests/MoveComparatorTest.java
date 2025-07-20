package tests;

import log320.entities.Move;
import log320.entities.Player;
import log320.game.Board;
import log320.game.MoveComparator;

import java.util.ArrayList;
import java.util.List;

import static log320.Const.*;

public class MoveComparatorTest {
    public static void main(String[] args) {
        Board board = new Board();
        board.set(0, 6, RED_PUSHER);
        board.set(1, 3, RED_PUSHER);
        board.set(2, 2, RED_PUSHER);
        board.set(3, 5, RED_PUSHER);
        board.set(4, 1, RED_PUSHER);
        board.set(4, 6, BLACK_PAWN);
        board.set(5, 2, BLACK_PUSHER);
        board.set(0, 1, RED_PUSHER);
        board.set(0, 2, RED_PUSHER);
        board.set(0, 3, RED_PUSHER);
        MoveComparator moveComparator = new MoveComparator(board, Player.RED);

        List<Move> moves = new ArrayList<>();

        moves.add(ALL_MOVES.get("A7B8"));
        moves.add(ALL_MOVES.get("B4C5"));
        moves.add(ALL_MOVES.get("C3D4"));
        moves.add(ALL_MOVES.get("F4E5"));
        moves.add(ALL_MOVES.get("B5A6"));
        moves.add(ALL_MOVES.get("C3B4"));
        moves.add(ALL_MOVES.get("F4G5"));
        moves.add(ALL_MOVES.get("B5C6"));
        moves.add(ALL_MOVES.get("B1B2"));
        moves.add(ALL_MOVES.get("C1C2"));
        moves.add(ALL_MOVES.get("D1D2"));

        long startTime = System.nanoTime();
        moves.sort(moveComparator);
        long duration = System.nanoTime() - startTime;
        System.out.println("Sorting took " + duration + " nanoseconds");

        System.out.println("Sorted moves:");
        for (Move move : moves) {
            System.out.println(move + " with score: " + moveComparator.getMoveScore(move));
        }
    }
}
