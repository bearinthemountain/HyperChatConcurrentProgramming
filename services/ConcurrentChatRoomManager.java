package services;

import core.ports.ChatRoomManager;
import model.ClientContext;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConcurrentChatRoomManager implements ChatRoomManager {

    // chacun peut rejoindre sans bloquer comme avec un synch
    private final ConcurrentHashMap<String, Set<ClientContext>> rooms = new ConcurrentHashMap<>();

    @Override
    public void joinRoom(String roomId, ClientContext client) {
        // computeIfAbsent crée le Set si la room n'existe pas encore
        rooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>())
                .add(client);
    }

    @Override
    public void leaveRoom(String roomId, ClientContext client) {
        Set<ClientContext> clients = rooms.get(roomId);
        if (clients != null) {
            clients.remove(client);
            if (clients.isEmpty()) {
                rooms.remove(roomId);
            }
        }
    }

    @Override
    public Set<ClientContext> getActiveClients(String roomId) {
        // Retourne le Set de la room ou un Set vide immuable si la room n'existe pas
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }
}
