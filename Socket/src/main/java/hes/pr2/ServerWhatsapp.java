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



        /**
         * STRUCTURED CONCURRENCY ARCHITECTURE
         * * 1. Scope Policy (ShutdownOnFailure):
         * - The scope acts as a parent guardian for all client connection tasks.
         * - If any 'handleClient' task fails, the scope triggers a global shutdown,
         * automatically cancelling all other active sub-tasks.
         * * 2. Task Forking:
         * - Each client connection is isolated into a virtual thread (sub-task).
         * - The lifecycle of these threads is strictly bound to this 'try-with-resources' block.
         * * 3. Synchronization (Join & Throw):
         * - scope.join(): Essential barrier. It blocks the parent thread until all
         * connections are closed or a failure occurs.
         * - throwIfFailed(): Ensures that if a sub-task crashed, the exception is
         * propagated to the parent, preventing "silent" server failures.
         * * 4. Automatic Resource Management:
         * - The 'try-with-resources' ensures that scope.close() is called,
         * guaranteeing no orphan threads are left running after the server stops.
         */
                                //shutdown on sucess --> the scope close automaticaly when one task is done
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) { //Scope shut down on failure if something failed in the scope
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
            scope.join().throwIfFailed(); // wait till all task are done and if one of them failed throw the exception
        } catch (Exception e) {
            System.err.println("Serveur stopped : " + e.getMessage());
        }
    }


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