package hes.pr2;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientWhatsapp {
    public static void main(String[] args) {
        String host = "localhost"; // L'adresse du serveur
        int port = 8080;           // Le port défini dans ton serveur

        try (Socket socket = new Socket(host, port);
             var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new PrintWriter(socket.getOutputStream(), true);
             var scanner = new Scanner(System.in)) {

            System.out.println("Connecté au serveur HyperChat !");

            Thread readerThread = Thread.ofVirtual().start(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("\n" + serverMessage);
                        System.out.print("> "); // Petit curseur pour taper
                    }
                } catch (IOException e) {
                    System.out.println("Connexion perdue avec le serveur.");
                }
            });

            // 2. Boucle principale pour ENVOYER tes messages
            System.out.print("> ");
            while (scanner.hasNextLine()) {
                String myMessage = scanner.nextLine();
                out.println(myMessage);
                if (myMessage.equalsIgnoreCase("/quit")) break;
            }

        } catch (IOException e) {
            System.err.println("Impossible de se connecter : " + e.getMessage());
        }
    }
}