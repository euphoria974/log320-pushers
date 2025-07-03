package tests;

import log320.entities.Move;
import log320.entities.Player;
import log320.game.Board;

import java.util.Collections;
import java.util.List;

import static log320.Const.LOSS_SCORE;
import static log320.Const.WIN_SCORE;

public class ArrayOutOfBoundsTest {
    public static void main(String[] args) {
        Board board = new Board();
        board.init();

        int score = board.evaluate(Player.RED);
        Player player = Player.RED;
        while (score != WIN_SCORE && score != LOSS_SCORE) {
            board.play(pickRandomMove(board, player));
            score = board.evaluate(player);
            player = player.getOpponent();
            System.out.println("Current score: " + score + " for player: " + player);
        }
    }

    private static Move pickRandomMove(Board board, Player player) {
        List<Move> moves = board.getPossibleMoves(player);
        Collections.shuffle(moves);
        return moves.getFirst();
    }
}
