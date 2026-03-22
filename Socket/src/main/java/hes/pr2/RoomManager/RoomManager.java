package hes.pr2.RoomManager;

import hes.pr2.model.client.Client;
import hes.pr2.model.room.Room;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    public ConcurrentHashMap<Room, Set<Client>> concurrentHashMap = new ConcurrentHashMap<>();


    public void join(Client client, String roomName){
        Boolean found = false;
        ConcurrentHashMap.KeySetView<Room, Set<Client>> keySetView =  concurrentHashMap.keySet();
        while (keySetView.iterator().hasNext() && !found){
            Room roomToTry = keySetView.iterator().next();
            if(roomToTry.getRoomName() == roomName){
                Set<Client> clientList = concurrentHashMap.get(roomToTry);
                clientList.add(client);
                concurrentHashMap.put(roomToTry, clientList);
                System.out.println("Room founded and client added " + roomToTry.getRoomName() + " : " + client.getPseudo() );
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
}
