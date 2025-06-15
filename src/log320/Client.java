package log320;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import static log320.Const.RANDOM;

class Client {
    // 0: case vide
    // 1: petit noir
    // 2: gros noir
    // 3: petit rouge
    // 4: gros rouge

    private static Board board;
    private static CPUPlayer cpuPlayer;

    public static void main(String[] args) {
        /* TEST EVALUATE
        // Player 3 wins by reaching the other side
        resetBoard();
        BOARD[0][7] = 3;
        System.out.println("Player 3 wins: " + (log320.Client.evaluate(3) == 100)); // True

        // Player 3 wins if no opponent pusher is found
        resetBoard();
        for (int i = 0; i < 8; i++) {
            BOARD[i][7] = 0;
        }
        BOARD[0][7] = 3;
        System.out.println("Player 3 wins: " + (log320.Client.evaluate(3) == 100)); // True

        // Opponent wins by reaching the other side
        resetBoard();
        BOARD[0][0] = 1;
        System.out.println("Opponent wins: " + (log320.Client.evaluate(3) == -100)); // True

        // Opponent wins if player has no pusher
        resetBoard();
        for (int i = 0; i < 8; i++) {
            BOARD[i][0] = 0;
        }
        BOARD[0][7] = 3;
        System.out.println("Opponent wins: " + (log320.Client.evaluate(3) == 100)); // True

        // No winner
        resetBoard();
        BOARD[1][1] = 3;
        BOARD[2][2] = 2;
        System.out.println("No winner: " + (log320.Client.evaluate(3) == 0)); // True
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
                    board = new Board(new String(aBuffer).trim(), 3);
                    cpuPlayer = new CPUPlayer(board, 3);

                    System.out.println("Nouvelle partie! Vous jouer rouge");

                    ArrayList<String> moves = cpuPlayer.getNextMove();
                    String move = moves.get(RANDOM.nextInt(moves.size()));
                    board.play(move);

                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }

                // Debut de la partie en joueur Noir
                if (cmd == '2') {
                    System.out.println("Nouvelle partie! Vous jouer noir");
                    byte[] aBuffer = new byte[1024];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    board = new Board(new String(aBuffer).trim(), 1);
                    cpuPlayer = new CPUPlayer(board, 1);
                }

                // Le serveur demande le prochain coup
                // Le message contient aussi le dernier coup joue.
                if (cmd == '3') {
                    byte[] aBuffer = new byte[16];

                    int size = input.available();
                    input.read(aBuffer, 0, size);

                    String s = new String(aBuffer);
                    String m = s.replaceAll("[^A-Za-z0-9]", "");
                    board.play(m);

                    System.out.println("Dernier coup :" + s);
                    System.out.println("Votre tour");

                    ArrayList<String> moves = cpuPlayer.getNextMove();
                    String move = moves.get(RANDOM.nextInt(moves.size()));
                    board.play(move);

                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }

                // Le dernier coup est invalide
                if (cmd == '4') {
                    board.print();
                    System.out.println(board.getLastMove() + " est invalide, entrez un nouveau coup");

                    ArrayList<String> moves = cpuPlayer.getNextMove();
                    String move = moves.get(RANDOM.nextInt(moves.size()));
                    board.play(move);

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
}
