package hes.pr2.model.room;

import java.util.UUID;

public class Room {
    private String roomName;
    private UUID roomId;

    public Room(String roomName, UUID roomId) {
        this.roomName = roomName;
        this.roomId = roomId;
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
