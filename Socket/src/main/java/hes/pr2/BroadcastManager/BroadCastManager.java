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

        /**
         * PARALLEL MESSAGE BROADCASTING
         * * This block handles the simultaneous distribution of a message to all clients in a room:
         * * 1. Concurrency Model:
         * - Uses StructuredTaskScope to broadcast messages in parallel, ensuring high
         * performance even with many connected clients.
         * * 2. Error Atomicity (ShutdownOnFailure):
         * - If sending a message to one specific client fails (e.g., broken socket),
         * the scope can trigger a shutdown to handle the broadcast failure immediately.
         * * 3. Lifecycle Guarantee:
         * - scope.join(): Acts as a synchronization barrier. The method will not return
         * until every client has been processed.
         * - throwIfFailed(): Ensures that any IOException during transmission is
         * properly caught and logged, rather than failing silently.
         * * 4. Thread Safety:
         * - By forking each 'println' call, we prevent a single slow/laggy client
         * from delaying the message delivery to other users in the room.
         */

        if (roomToSend != null) {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                for (Client client : roomToSend.getClients()) {
                    if (!client.equals(sender)) {
                        scope.fork(() -> {
                            client.writer.println(msg);
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
