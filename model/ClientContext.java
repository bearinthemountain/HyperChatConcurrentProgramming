package model;

import java.nio.channels.SocketChannel;

public class ClientContext {
    private final String clientId;
    private final SocketChannel socketChannel;
    private final String username;

    public ClientContext(String clientId, SocketChannel socketChannel, String username) {
        this.clientId = clientId;
        this.socketChannel = socketChannel;
        this.username = username;
    }

    public String getClientId() {
        return clientId;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public String getUsername() {
        return username;
    }
}
