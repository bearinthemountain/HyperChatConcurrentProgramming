package hes.pr2.model.client;

import hes.pr2.model.room.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

public class Client {

    private UUID uuid;
    private String pseudo;
    private Socket socketClient;
    private Room room;
    public PrintWriter writer;
    public BufferedReader reader;

    public Client(UUID uuid, String pseudo) {
        this.uuid = uuid;
        this.pseudo = pseudo;
     }



    public Socket getSocketClient() {
        return socketClient;
    }

    public void setSocketClient(Socket socketClient) {
        this.socketClient = socketClient;
    }


    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}

