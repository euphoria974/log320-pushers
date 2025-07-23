package log320;

import log320.entities.Player;
import log320.game.Game;
import log320.transposition.ZobristHash;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import static log320.Const.ALL_MOVES;

class Client {
    public static void main(String[] args) {
        // Init new instances
        final Game game = new Game();
        ALL_MOVES.get("A1A2");
        ZobristHash.getPiecesTable();

        try {
            Socket client = new Socket("localhost", 8888);
            BufferedInputStream input = new BufferedInputStream(client.getInputStream());
            BufferedOutputStream output = new BufferedOutputStream(client.getOutputStream());

            while (true) {
                char cmd = (char) input.read();
                System.out.println("\033[93;40mCommande serveur: " + cmd);

                // Debut de la partie en joueur rouge
                if (cmd == '1') {
                    byte[] aBuffer = new byte[1024];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    game.start(new String(aBuffer).trim(), Player.RED);

                    System.out.println("\033[93;40mNouvelle partie! Vous jouez rouge");

                    String move = game.getNextMove().toString();
                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }

                // Debut de la partie en joueur Noir
                if (cmd == '2') {
                    System.out.println("\033[93;40mNouvelle partie! Vous jouez noir");
                    byte[] aBuffer = new byte[1024];
                    int size = input.available();
                    input.read(aBuffer, 0, size);
                    game.start(new String(aBuffer).trim(), Player.BLACK);
                }

                // Le serveur demande le prochain coup
                // Le message contient aussi le dernier coup joue.
                if (cmd == '3') {
                    byte[] aBuffer = new byte[16];

                    int size = input.available();
                    input.read(aBuffer, 0, size);

                    String s = new String(aBuffer);
                    String m = s.replaceAll("[^A-Za-z0-9]", "");
                    game.play(m);

                    System.out.println("\033[93;40mDernier coup :" + s);
                    System.out.println("\033[93;40mVotre tour");

                    String move = game.getNextMove().toString();
                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }

                // Le dernier coup est invalide
                if (cmd == '4') {
                    game.printBoard();
                    System.out.println("\033[91;40m" + game.getLastMove() + " est invalide!! Entrez un nouveau coup");

                    String move = game.getNextMove().toString();
                    output.write(move.getBytes(), 0, move.length());
                    output.flush();
                }

                // La partie est terminée
                if (cmd == '5') {
                    byte[] aBuffer = new byte[16];
                    int size = input.available();
                    input.read(aBuffer, 0, size);

                    String s = new String(aBuffer);
                    String m = s.replaceAll("[^A-Za-z0-9]", "");
                    game.play(m);

                    System.out.println("\033[93;40mPartie Terminé. Le dernier coup joué est: " + s);
                    output.flush();
                    game.over();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
