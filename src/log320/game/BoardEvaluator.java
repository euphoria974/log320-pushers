package log320.game;

import log320.entities.Player;

import static log320.Const.LOSS_SCORE;
import static log320.Const.WIN_SCORE;

public class BoardEvaluator {
    private final Board BOARD;

    private final int pusherScore = 50;
    private final int pawnScore = 15;
    private final int pusherDistanceToWinningRowScore = 3;
    private final int nextPlayBonus = -50;

    public BoardEvaluator(Board board) {
        this.BOARD = board;
    }

    /**
     * Évalue le score du joueur donné.
     * <p>
     * Le score est calculé en fonction de la position des pions et des pousseurs sur le plateau.
     * Un score de WIN_SCORE indique une victoire, un score de LOSS_SCORE indique une défaite.
     * Si le joueur n'a pas gagné ou perdu, le score est calculé par rapport à l'adversaire.
     *
     * @param player Le joueur à évaluer.
     * @return Le score du joueur.
     */
    public int evaluate(Player player) {
        if (BOARD.hasPlayerWon(player)) {
            return WIN_SCORE;
        }

        if (BOARD.hasPlayerWon(player.getOpponent())) {
            return LOSS_SCORE;
        }

        int score = (pusherScore * (Long.bitCount(BOARD.getRedPushers()) - Long.bitCount(BOARD.getBlackPushers())) +
                pawnScore * (Long.bitCount(BOARD.getRedPawns()) - Long.bitCount(BOARD.getBlackPawns()))) * (player == Player.RED ? 1 : -1);

        long pushers = (player == Player.RED) ? BOARD.getRedPushers() : BOARD.getBlackPushers();
        long oppPushers = (player == Player.RED) ? BOARD.getBlackPushers() : BOARD.getRedPushers();

        for (long bits = pushers; bits != 0; bits &= bits - 1) {
            int idx = Long.numberOfTrailingZeros(bits);
            int row = idx / 8;
            score += pusherDistanceToWinningRowScore * Math.abs(row - player.getWinningRow());
        }

        for (long bits = oppPushers; bits != 0; bits &= bits - 1) {
            int idx = Long.numberOfTrailingZeros(bits);
            int row = idx / 8;
            score -= pusherDistanceToWinningRowScore * Math.abs(row - player.getOpponent().getWinningRow());
        }

        return score + nextPlayBonus;
    }
}