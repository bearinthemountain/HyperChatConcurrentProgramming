package hes.pr2.model.message;

import hes.pr2.model.client.Client;
import hes.pr2.model.room.Room;

import java.sql.Timestamp;
import java.util.UUID;

public record Message(UUID uuid, Room room, Client client, String content, Timestamp timestamp) {
}
