package log320.evaluators;

import log320.Board;
import log320.Player;

public interface IEvaluator {
    // Ne pas oublier de vérifier la victoire dans l'évaluation
    int evaluate(Board board, Player player);
}
