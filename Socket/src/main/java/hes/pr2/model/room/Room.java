package hes.pr2.model.room;

import hes.pr2.model.client.Client;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private String roomName;
    private UUID roomId;
    private final java.util.Set<Client> clients = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Room(String roomName, UUID roomId) {
        this.roomName = roomName;
        this.roomId = roomId;
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public void removeClient(Client client) {
        clients.remove(client);
    }

    public java.util.Set<Client> getClients() {
        return clients;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }
}
