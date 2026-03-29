package hes.pr2.command;

import hes.pr2.RoomManager.RoomManager;
import hes.pr2.model.client.Client;
import hes.pr2.model.room.Room;

public class JoinCommand implements Command {
    private final RoomManager roomManager;
    private RoomCommandReceiver roomCommandReceiver;

    public JoinCommand(RoomManager roomManager, RoomCommandReceiver roomCommandReceiver) {
        this.roomManager = roomManager;
        this.roomCommandReceiver = roomCommandReceiver;
    }

    @Override
    public void execute( Client client, String[] args) {
        roomCommandReceiver.joinRoom(client, roomManager, args);
    }
}