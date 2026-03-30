package hes.pr2;


import hes.pr2.BroadcastManager.BroadCastManager;
import hes.pr2.RoomManager.RoomManager;
import hes.pr2.command.CommandHandler;
import hes.pr2.command.RoomCommandReceiver;
import hes.pr2.model.client.Client;
import hes.pr2.model.room.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;

public class ServerWhatsapp {

    // On les met ici pour qu'ils soient accessibles par toutes les méthodes
    public final RoomManager roomManager = new RoomManager();
    private final BroadCastManager broadCastManager = new BroadCastManager(roomManager);
    RoomCommandReceiver roomCommandReceiver = new RoomCommandReceiver();
    private final CommandHandler commandHandler = new hes.pr2.command.CommandHandler(roomManager, broadCastManager, roomCommandReceiver);

    public static void main(String[] args) {
        ServerWhatsapp server = new ServerWhatsapp();
        server.startServer();
    }



    public void startServer() {
        System.out.println("🚀 HyperChat démarré...");
        roomManager.initRooms();

//        1. Le Scope de ServerWhatsapp.java (Le Scope "Longue durée")                                                                                                                                                                                                           LSP
//              *   Son rôle : Gérer les connexions des clients.                                                                                                                                                                                                                       • jdtls Socket                       ▄
//              *   Pourquoi : La boucle serverSocket.accept() est infinie. Dès qu'un client A se connecte, $
//              le serveur doit commencer à l'écouter (dans handleClient). Mais s'il fait ça de façon bloquante,
//              le client B ne pourra jamais se connecter !                                                                    █
//              *   L'utilité du Scope : À chaque nouveau client, le Scope dit : "Toi, va t'occuper du client A en tâche de fond (fork())
//              , pendant que moi je retourne à la porte d'entrée attendre le client B". Ce scope reste ouvert tant que le serveur tourne.                    ▼ Modified Files                     █


        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) { //Scope shut down on failure
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket clientSocket = serverSocket.accept();

                    //create sub task on the primary task
                    scope.fork(() -> {
                        handleClient(clientSocket);
                        return null;
                    });
                }
            } catch (IOException e) {
                scope.shutdown();
                throw e;
            }
            //scope close automaticaly called
            scope.join().throwIfFailed();
        } catch (Exception e) {
            System.err.println("Serveur stopped : " + e.getMessage());
        }
    }

    // Pas besoin de passer roomManager en paramètre, il est déjà dans la classe !
    public void handleClient(Socket socketClient) {
        // Utilise try-with-resources pour fermer le socket automatiquement
        try (socketClient;
             var reader = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
             var writer = new PrintWriter(socketClient.getOutputStream(), true)) {


            writer.println("Welcome, please put your pseudo :");
            String name = reader.readLine();

            Client client = new Client(UUID.randomUUID(), name);
            client.reader = reader;
            client.writer = writer;
            roomManager.join(client, "Général");

            writer.println("Welcome " + name);

            // IMPORTANT : La boucle de vie du client
            String line;
            while ((line = reader.readLine()) != null) {
                // Au lieu de toujours envoyer un message, on délègue au CommandHandler
                commandHandler.handle(client, line);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected.");
        }
    }
}