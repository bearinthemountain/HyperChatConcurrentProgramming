package hes.pr2.command;

import hes.pr2.RoomManager.RoomManager;
import hes.pr2.model.client.Client;
import hes.pr2.model.room.Room;

public class RoomCommandReceiver {

    public void joinRoom(Client client, RoomManager roomManager, String[] args) {
        if (args.length < 2) {
            client.writer.println("Usage: /join <nom_du_salon>");
            return;
        }

        String roomName = args[1];

        // 1. Quitter le salon actuel si le client est déjà dans un salon
        if (client.getRoom() != null) {
            roomManager.leave(client, client.getRoom().getRoomName());
        }

        // 2. Rejoindre le nouveau salon
        Room newRoom = roomManager.join(client, roomName);
        if (newRoom != null) {
            client.writer.println("You have join the room : " + roomName);
        } else {
            client.writer.println("The room '" + roomName + "' doesn't exist.");
        }
    }

    public void listRoom(Client client, RoomManager roomManager) {
         client.writer.println("=== Liste des salons disponibles ===");
         for(String roomName : roomManager.rooms.keySet()) {
             int nbClients = roomManager.rooms.get(roomName).getClients().size();
             client.writer.println("- " + roomName + " (" + nbClients + " personnes)");
         }
         client.writer.println("====================================");
    }
}
