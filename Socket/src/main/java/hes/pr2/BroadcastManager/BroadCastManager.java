package hes.pr2.BroadcastManager;

import hes.pr2.RoomManager.RoomManager;
import hes.pr2.model.client.Client;
import hes.pr2.model.message.Message;
import hes.pr2.model.room.Room;


import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;

public class BroadCastManager {
    RoomManager roomManager;

    public BroadCastManager(RoomManager roomManager) {
        this.roomManager = roomManager;

    }
    public void send(Client sender, Room roomToSend, String message){
        Timestamp timestamp = Timestamp.from(Instant.now());
        Message msg = new Message(UUID.randomUUID(), roomToSend, sender, message, timestamp);


        //     2. Le Scope de Broadcast.java (Le Scope "Courte durée / Éclair")
        //     *   Son rôle : Gérer l'envoi d'un seul message.
        //     *   Pourquoi : Imagine que 100 personnes sont dans le salon "Général". Si tu envoies un message en faisant une simple boucle classique, le serveur va l'envoyer au client 1, attendre qu'il soit bien envoyé, puis l'envoyer au client 2, etc. Si le client 1 a
        //    une très mauvaise connexion (lag), tout le monde va attendre à cause de lui !
        //     *   L'utilité du Scope : Quand un message arrive, ce Scope se crée à la volée. Il crée 100 sous-tâches (fork()) qui envoient le message aux 100 personnes exactement en même temps (en parallèle). Dès que les 100 envois sont terminés (scope.join()), le scope
        //    se ferme instantanément.
        if (roomToSend != null) {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                for (Client client : roomToSend.getClients()) {
                    if (!client.equals(sender)) {
                        scope.fork(() -> {
                            client.writer.println(msg.toString());
                            return null;
                        });
                    }
                }
                scope.join().throwIfFailed();
            } catch (Exception e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
            }
        }
    }
}
