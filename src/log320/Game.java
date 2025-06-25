package log320;

public class Game {
    private final Board BOARD;
    private final CPUPlayer CPU_PLAYER;

    public Game(String boardState, Player currentPlayer) {
        this.BOARD = new Board(boardState);
        this.CPU_PLAYER = new CPUPlayer(BOARD, currentPlayer);
    }

    public Move getNextMove() {
        BOARD.print(); // TODO debug, remove eventually
        Move move = CPU_PLAYER.getNextMove();
        BOARD.play(move);
        System.out.println("Picking move " + move + " with score " + move.getScore());
        return move;
    }

    public void play(Move move) {
        BOARD.play(move);
    }

    public void play(String moveString) {
        BOARD.play(Move.fromString(moveString));
    }

    public void printBoard() {
        BOARD.print();
    }

    public Move getLastMove() {
        return BOARD.getLastMove();
    }
}
