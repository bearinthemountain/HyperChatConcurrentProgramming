package hes.pr2.RoomManager;

import hes.pr2.model.client.Client;
import hes.pr2.model.room.Room;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    public ConcurrentHashMap<Room, Set<Client>> concurrentHashMap = new ConcurrentHashMap<>();


    // Dans votre constructeur ou une méthode d'initialisation
    public void initRooms() {
        List<String> defaultNames = Arrays.asList("Général", "Gaming", "Aide");

        for (String name : defaultNames) {
            // On utilise Collections.newSetFromMap pour un Set thread-safe
            concurrentHashMap.put(new Room(name, UUID.randomUUID()), Collections.newSetFromMap(new ConcurrentHashMap<>()));
        }
    }

    public void join(Client client, String roomName){
        Boolean found = false;
        ConcurrentHashMap.KeySetView<Room, Set<Client>> keySetView =  concurrentHashMap.keySet();
        while (keySetView.iterator().hasNext() && !found){
            Room roomToTry = keySetView.iterator().next();
            if(roomToTry.getRoomName() == roomName){
                Set<Client> clientList = concurrentHashMap.get(roomToTry);
                clientList.add(client);
                client.setRoom(roomToTry);
                concurrentHashMap.put(roomToTry, clientList);
                System.out.println("Room founded and client added " + roomToTry.getRoomName() + " : " + client.getPseudo() );
                found = true;
            }
        }
    }
    public void leave(Client client, String roomName){
        Boolean found = false;
        ConcurrentHashMap.KeySetView<Room, Set<Client>> keySetView =  concurrentHashMap.keySet();
        while (keySetView.iterator().hasNext() && !found){
            Room roomToTry = keySetView.iterator().next();
            if(roomToTry.getRoomName() == roomName){
                Set<Client> clientList = concurrentHashMap.get(roomToTry);
                clientList.remove(client);
                concurrentHashMap.put(roomToTry, clientList);
            }
        }
    }

    public Set<Client> getClient(){
        boolean found = false;
        Set<Client> clients = null;
        ConcurrentHashMap.KeySetView<Room, Set<Client>> keySetView = concurrentHashMap.keySet();
        while(keySetView.iterator().hasNext() && !found){
            Room roomTotry = keySetView.iterator().next();
            clients = concurrentHashMap.get(roomTotry);
        }
        return clients;
    }
}
