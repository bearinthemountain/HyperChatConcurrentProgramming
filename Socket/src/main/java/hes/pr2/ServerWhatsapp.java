package hes.pr2;


import hes.pr2.BroadcastManager.BroadCastManager;
import hes.pr2.RoomManager.RoomManager;
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
    private final BroadCastManager broadCastManager = new BroadCastManager();

    static void main(String[] args) {
        ServerWhatsapp server = new ServerWhatsapp();
        server.startServer();
    }

    public void startServer() {
        System.out.println("🚀 HyperChat démarré...");
        roomManager.initRooms();

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket clientSocket = serverSocket.accept();

                    // Ici, handleClient a accès à roomManager car ils sont dans la même classe
                    scope.fork(() -> {
                        handleClient(clientSocket);
                        return null;
                    });
                }
            } catch (IOException e) {
                scope.shutdown();
                throw e;
            }
            scope.join().throwIfFailed();
        } catch (Exception e) {
            System.err.println("Serveur arrêté : " + e.getMessage());
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
            roomManager.join(client, "Général");

            writer.println("Bienvenue " + name);

            // IMPORTANT : La boucle de vie du client
            String line;
            while ((line = reader.readLine()) != null) {
                // On utilise le broadcastManager ici
            //    broadCastManager.broadcast("General", name + ": " + line);
            }

        } catch (IOException e) {
            System.out.println("Client déconnecté.");
        }
    }
}