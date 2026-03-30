package hes.pr2.command;

import hes.pr2.RoomManager.RoomManager;
import hes.pr2.model.client.Client;

public class ListCommand implements Command {
    private final RoomManager roomManager;
    private RoomCommandReceiver roomCommandReceiver;

    public ListCommand(RoomManager roomManager, RoomCommandReceiver roomCommandReceiver) {
        this.roomManager = roomManager;
        this.roomCommandReceiver = roomCommandReceiver;
    }

    @Override
    public void execute( Client client, String[] args) {
        roomCommandReceiver.listRoom(client, roomManager);
    }
}