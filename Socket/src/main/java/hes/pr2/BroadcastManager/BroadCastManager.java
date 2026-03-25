package hes.pr2.BroadcastManager;

import hes.pr2.RoomManager.RoomManager;
import hes.pr2.model.client.Client;
import hes.pr2.model.message.Message;
import hes.pr2.model.room.Room;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class BroadCastManager {
    RoomManager roomManager;

    public BroadCastManager(RoomManager roomManager) {
        this.roomManager = roomManager;

    }

    public void send(Client client, Room roomToSend, String message){
        Timestamp timestamp = Timestamp.from(Instant.now());
        Message message1 = new Message(UUID.randomUUID(), roomToSend, client, message, timestamp);
        client.writer.println(message1.content());
        client.reader.readLine();
    }
}
