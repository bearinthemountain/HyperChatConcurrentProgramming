package core.ports;

import model.ClientContext;

import java.util.Set;

public interface ChatRoomManager {
    void joinRoom(String roomId, ClientContext client);
    void leaveRoom(String roomId, ClientContext client);
    Set<ClientContext> getActiveClients(String roomId);
}
