import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

class Client {
    // 0: case vide
    // 1: petit noir
    // 2: gros noir
    // 3: petit rouge
    // 4: gros rouge

    static final int MAX_DEPTH = 6;
    static final long MAX_TIME_MILLIS = 4800;

    static final int[][] BOARD = new int[8][8];
    static final int WIN_SCORE = 10000;
    static final int LOSS_SCORE = -10000;
    static final Random RANDOM = new Random();

    static int currentPlayer;
    static int enemyPlayer;

    static String lastMove = "";
    static Stack<MoveState> moveStack = new Stack<>();

    private static void setCurrentPlayer(int player) {
        currentPlayer = player;
        enemyPlayer = player == 3 ? 1 : 3;
    }

    public static void main(String[] args) {
        /* TEST EVALUATE
        // Player 3 wins by reaching the other side
        resetBoard();
        BOARD[0][7] = 3;
        System.out.println("Player 3 wins: " + (Client.evaluate(3) == 100)); // True

        // Player 3 wins if no opponent pusher is found
        resetBoard();
        for (int i = 0; i < 8; i++) {
            BOARD[i][7] = 0;
        }
        BOARD[0][7] = 3;
        System.out.println("Player 3 wins: " + (Client.evaluate(3) == 100)); // True

        // Opponent wins by reaching the other side
        resetBoard();
        BOARD[0][0] = 1;
        System.out.println("Opponent wins: " + (Client.evaluate(3) == -100)); // True

        // Opponent wins if player has no pusher
        resetBoard();
        for (int i = 0; i < 8; i++) {
            BOARD[i][0] = 0;
        }
        BOARD[0][7] = 3;
        System.out.println("Opponent wins: " + (Client.evaluate(3) == 100)); // True

        // No winner
        resetBoard();
        BOARD[1][1] = 3;
        BOARD[2][2] = 2;
        System.out.println("No winner: " + (Client.evaluate(3) == 0)); // True
*/

        try {
            Socket client = new Socket("localhost", 8888);
            BufferedInputStream input = new BufferedInputStream(client.getInputStream());
            BufferedOutputStream output = new BufferedOutputStream(client.getOutputStream());

            while (true) {
                char cmd = (char) input.read();
                System.out.println("Commande serveur: " + cmd);

                // Debut de la partie en joueur rouge
                if (cmd == '1') {
                    byte[] aBuffer = new byte[1024];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    buildBoard(new String(aBuffer).trim());

                    setCurrentPlayer(3);
                    System.out.println("Nouvelle partie! Vous jouer rouge");

                    ArrayList<String> moves = getNextMoveAB();
                    String move = moves.get(RANDOM.nextInt(moves.size()));
                    play(move);

                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }

                // Debut de la partie en joueur Noir
                if (cmd == '2') {
                    setCurrentPlayer(1);
                    System.out.println("Nouvelle partie! Vous jouer noir");
                    byte[] aBuffer = new byte[1024];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    buildBoard(new String(aBuffer).trim());
                }

                // Le serveur demande le prochain coup
                // Le message contient aussi le dernier coup joue.
                if (cmd == '3') {
                    byte[] aBuffer = new byte[16];

                    int size = input.available();
                    input.read(aBuffer, 0, size);

                    String s = new String(aBuffer);
                    String m = s.replaceAll("[^A-Za-z0-9]", "");
                    play(m);

                    System.out.println("Dernier coup :" + s);
                    System.out.println("Votre tour");

                    ArrayList<String> moves = getNextMoveAB();
                    String move = moves.get(RANDOM.nextInt(moves.size()));
                    play(move);

                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }

                // Le dernier coup est invalide
                if (cmd == '4') {
                    printBoard();
                    System.out.println(lastMove + " est invalide, entrez un nouveau coup");

                    ArrayList<String> moves = getNextMoveAB();
                    String move = moves.get(RANDOM.nextInt(moves.size()));
                    play(move);

                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }

                // La partie est terminée
                if (cmd == '5') {
                    byte[] aBuffer = new byte[16];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    String s = new String(aBuffer);
                    System.out.println("Partie Terminé. Le dernier coup joué est: " + s);
                    output.flush();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void buildBoard(String s) {
        List<String> boardValues = Arrays.asList(s.split(" ")).reversed();

        int row = 0, col = 0;
        for (String boardValue : boardValues) {
            BOARD[row][col] = Integer.parseInt(boardValue);
            row++;
            if (row == 8) {
                col++;
                row = 0;
            }
        }
    }

    private static void resetBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (j == 0) {
                    BOARD[i][j] = 4;
                } else if (j == 1) {
                    BOARD[i][j] = 3;
                } else if (j == 6) {
                    BOARD[i][j] = 1;
                } else if (j == 7) {
                    BOARD[i][j] = 2;
                }
            }
        }
    }

    private static void printBoard() {
        for (int[] ints : BOARD) {
            System.out.println(Arrays.toString(ints).replaceAll("[\\[\\],]", "").replaceAll(",", " "));
        }
    }

    private static void play(String move) {
        lastMove = move;

        int fromRow = move.charAt(0) - 65;
        int fromCol = Character.getNumericValue(move.charAt(1)) - 1;
        int toRow = move.charAt(2) - 65;
        int toCol = Character.getNumericValue(move.charAt(3)) - 1;
        int movedPiece = BOARD[fromRow][fromCol];
        int capturedPiece = BOARD[toRow][toCol];

        moveStack.push(new MoveState(fromRow, fromCol, toRow, toCol, movedPiece, capturedPiece));

        BOARD[fromRow][fromCol] = 0;
        BOARD[toRow][toCol] = movedPiece;
    }

    private static void undo() {
        if (moveStack.isEmpty()) return;
        MoveState ms = moveStack.pop();
        BOARD[ms.toRow][ms.toCol] = ms.capturedPiece;
        BOARD[ms.fromRow][ms.fromCol] = ms.movedPiece;
    }

    private static int evaluate(int player) {
        int opponent = player == 3 ? 1 : 3;
        int playerWinningCol = player == 3 ? 7 : 0;
        int opponentWinningCol = opponent == 3 ? 7 : 0;
        int playerPusher = player + 1;
        int opponentPusher = opponent + 1;

        int playerScore = 0, opponentScore = 0;
        int playerCount = 0, opponentCount = 0;
        boolean hasPlayerPusher = false, hasOpponentPusher = false;

        for (int row = 0; row < 8; row++) {
            if (BOARD[row][playerWinningCol] == player) return WIN_SCORE;
            if (BOARD[row][opponentWinningCol] == opponent) return LOSS_SCORE;
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = BOARD[row][col];
                boolean isCenter = (row >= 2 && row <= 5) && (col >= 2 && col <= 5);

                if (piece == player) {
                    playerScore += 100 + ((player == 3) ? col * 10 : (7 - col) * 10);
                    if (isCenter) playerScore += 20;
                    if (col == 0 || col == 7) playerScore -= 10;
                    playerCount++;
                } else if (piece == playerPusher) {
                    playerScore += 150 + ((player == 3) ? col * 15 : (7 - col) * 15);
                    if (isCenter) playerScore += 30;
                    if (col == 0 || col == 7) playerScore -= 15;
                    if ((player == 3 && col < 7 && BOARD[row][col + 1] == opponentPusher) ||
                            (player == 1 && col > 0 && BOARD[row][col - 1] == opponentPusher)) {
                        playerScore += 40;
                    }
                    hasPlayerPusher = true;
                    playerCount++;
                } else if (piece == opponent) {
                    opponentScore += 100 + ((opponent == 3) ? col * 10 : (7 - col) * 10);
                    if (isCenter) opponentScore += 20;
                    if (col == 0 || col == 7) opponentScore -= 10;
                    opponentCount++;
                } else if (piece == opponentPusher) {
                    opponentScore += 150 + ((opponent == 3) ? col * 15 : (7 - col) * 15);
                    if (isCenter) opponentScore += 30;
                    if (col == 0 || col == 7) opponentScore -= 15;
                    if ((opponent == 3 && col < 7 && BOARD[row][col + 1] == playerPusher) ||
                            (opponent == 1 && col > 0 && BOARD[row][col - 1] == playerPusher)) {
                        opponentScore += 40;
                    }
                    hasOpponentPusher = true;
                    opponentCount++;
                }
            }
        }

        if (!hasPlayerPusher) return LOSS_SCORE;
        if (!hasOpponentPusher) return WIN_SCORE;

        playerScore += (playerCount - opponentCount) * 50;
        return playerScore - opponentScore;
    }

    public static ArrayList<String> getPossibleMoves(int player) {
        ArrayList<String> possibleMoves = new ArrayList<>(32);
        int enemyPlayer = player == 3 ? 1 : 3;
        StringBuilder sb = new StringBuilder(4);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (BOARD[row][col] == player) {
                    if (player == 3) { // PETIT ROUGE
                        if (BOARD[row][col + 1] == 0 && BOARD[row][col - 1] == player + 1) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 65) + "" + (col + 2));
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && ((BOARD[row - 1][col + 1] == 0 || BOARD[row - 1][col + 1] == enemyPlayer || BOARD[row - 1][col + 1] == enemyPlayer + 1) && BOARD[row + 1][col - 1] == player + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 64) + "" + (col + 2));
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && ((BOARD[row + 1][col + 1] == 0 || BOARD[row + 1][col + 1] == enemyPlayer || BOARD[row + 1][col + 1] == enemyPlayer + 1) && BOARD[row - 1][col - 1] == player + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 66) + "" + (col + 2));
                            possibleMoves.add(sb.toString());
                        }
                    } else { // PETIT NOIR
                        if (BOARD[row][col - 1] == 0 && BOARD[row][col + 1] == player + 1) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 65) + "" + (col));
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && ((BOARD[row - 1][col - 1] == 0 || BOARD[row - 1][col - 1] == enemyPlayer || BOARD[row - 1][col - 1] == enemyPlayer + 1) && BOARD[row + 1][col + 1] == player + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 64) + "" + (col));
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && row < 7 && (BOARD[row + 1][col - 1] == 0 || BOARD[row + 1][col - 1] == enemyPlayer || BOARD[row + 1][col - 1] == enemyPlayer + 1) && BOARD[row - 1][col + 1] == player + 1) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 66) + "" + (col));
                            possibleMoves.add(sb.toString());
                        }
                    }
                } else if (BOARD[row][col] == player + 1) {
                    if (player == 3 && col < 7) { // PUSHER ROUGE
                        if (BOARD[row][col + 1] == 0) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 65) + "" + (col + 2));
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && (BOARD[row - 1][col + 1] == 0 || BOARD[row - 1][col + 1] == enemyPlayer || BOARD[row - 1][col + 1] == enemyPlayer + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 64) + "" + (col + 2));
                            possibleMoves.add(sb.toString());
                        }

                        if (row < 7 && (BOARD[row + 1][col + 1] == 0 || BOARD[row + 1][col + 1] == enemyPlayer || BOARD[row + 1][col + 1] == enemyPlayer + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 66) + "" + (col + 2));
                            possibleMoves.add(sb.toString());
                        }
                    } else if (col > 0) { // PUSHER NOIR
                        if (BOARD[row][col - 1] == 0) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 65) + "" + (col));
                            possibleMoves.add(sb.toString());
                        }

                        if (row > 0 && (BOARD[row - 1][col - 1] == 0 || BOARD[row - 1][col - 1] == enemyPlayer || BOARD[row - 1][col - 1] == enemyPlayer + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 64) + "" + (col));
                            possibleMoves.add(sb.toString());
                        }

                        if (row < 7 && (BOARD[row + 1][col - 1] == 0 || BOARD[row + 1][col - 1] == enemyPlayer || BOARD[row + 1][col - 1] == enemyPlayer + 1)) {
                            sb.setLength(0);
                            sb.append((char) (row + 65) + "" + (col + 1)).append((char) (row + 66) + "" + (col));
                            possibleMoves.add(sb.toString());
                        }
                    }
                }
            }
        }

        possibleMoves.sort((a, b) -> Integer.compare(scoreMove(b, player), scoreMove(a, player)));
        return possibleMoves;
    }

    private static int scoreMove(String move, int player) {
        int fromRow = move.charAt(0) - 65;
        int fromCol = Character.getNumericValue(move.charAt(1)) - 1;
        int toRow = move.charAt(2) - 65;
        int toCol = Character.getNumericValue(move.charAt(3)) - 1;
        int movedPiece = BOARD[fromRow][fromCol];
        int dest = BOARD[toRow][toCol];

        int score = 0;

        if ((player == 3 && toCol == 7) || (player == 1 && toCol == 0)) {
            score += 10000;
        }

        score += (player == 3) ? (toCol - fromCol) * 10 : (fromCol - toCol) * 10;

        if (((player == 3 && (dest == 1 || dest == 2)) || (player == 1 && (dest == 3 || dest == 4)))) {
            score += 500;
        }

        if (movedPiece == player) {
            score += 30; // Adjust this value as needed
        }

        return score;
    }

    // Retourne la liste des coups possibles.  Cette liste contient
    // plusieurs coups possibles si et seuleument si plusieurs coups
    // ont le même score.
    public static ArrayList<String> getNextMoveAB() {
        long startTime = System.currentTimeMillis();
        ArrayList<String> bestMoves = new ArrayList<>();
        ArrayList<String> currentBest = new ArrayList<>();
        int depth = 1;

        while (System.currentTimeMillis() - startTime < MAX_TIME_MILLIS) {
            currentBest.clear();
            int bestScore = Integer.MIN_VALUE;

            for (String move : getPossibleMoves(currentPlayer)) {
                play(move);
                int score = alphaBeta(enemyPlayer, false, Integer.MIN_VALUE, Integer.MAX_VALUE, depth, startTime);
                undo();

                if (score > bestScore) {
                    bestScore = score;
                    currentBest.clear();
                    currentBest.add(move);
                } else if (score == bestScore) {
                    currentBest.add(move);
                }
            }

            if (System.currentTimeMillis() - startTime < MAX_TIME_MILLIS) {
                bestMoves.clear();
                bestMoves.addAll(currentBest);
            }

            depth++;
        }

        return bestMoves;
    }

    private static int alphaBeta(int player, boolean isMax, int alpha, int beta, int dept, long startTime) {
        if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
            return 0; // Or best guess so far
        }

        int opponent = player == 3 ? 1 : 3;
        List<String> possibleMoves = getPossibleMoves(player);

        int score = evaluate(currentPlayer);

        if (score == WIN_SCORE || score == LOSS_SCORE || possibleMoves.isEmpty() || dept >= MAX_DEPTH) {
            return score;
        }

        if (isMax) {
            int maxScore = Integer.MIN_VALUE;

            for (String move : possibleMoves) {
                if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
                    return 0;
                }

                play(move);
                int value = alphaBeta(opponent, false, alpha, beta, dept + 1, startTime);
                undo();

                // Optimisation: early cutoff
                if (value == WIN_SCORE) {
                    return value;
                }

                maxScore = Math.max(maxScore, value);

                if (maxScore >= beta) {
                    break;
                }

                alpha = Math.max(alpha, maxScore);
            }

            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;

            for (String move : possibleMoves) {
                if (System.currentTimeMillis() - startTime >= MAX_TIME_MILLIS) {
                    return 0;
                }

                play(move);
                int value = alphaBeta(opponent, true, alpha, beta, dept + 1, startTime);
                undo();

                // Optimisation: early cutoff
                if (value == LOSS_SCORE) {
                    return value;
                }

                minScore = Math.min(minScore, value);

                if (minScore <= alpha) {
                    break;
                }

                beta = Math.min(beta, minScore);
            }

            return minScore;
        }
    }
}
