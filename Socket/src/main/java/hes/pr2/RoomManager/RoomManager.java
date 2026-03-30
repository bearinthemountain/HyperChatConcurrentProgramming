package hes.pr2.RoomManager;

import hes.pr2.model.client.Client;
import hes.pr2.model.room.Room;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    public ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

    public void initRooms() {
        List<String> defaultNames = Arrays.asList("Général", "Gaming", "Aide");

        for (String name : defaultNames) {
            Room room = new Room(name, UUID.randomUUID());
            rooms.put(name, room);
        }
    }

    public Room join(Client client, String roomName){
        Room room = rooms.get(roomName);
        if (room != null) {
            room.addClient(client);
            client.setRoom(room);
            System.out.println("Room founded and client added " + roomName + " : " + client.getPseudo());
            return room;
        }
        
        System.out.println("Room " + roomName + " not found!");
        return null;
    }

    public void leave(Client client, String roomName){
        Room room = rooms.get(roomName);
        if (room != null) {
            room.removeClient(client);
        }
    }
}
