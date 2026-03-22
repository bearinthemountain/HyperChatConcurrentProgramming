package hes.pr2.model.message;

import hes.pr2.model.client.Client;
import hes.pr2.model.room.Room;

import java.security.Timestamp;

public record Message(String uuid, Room room, Client client, Timestamp timestamp) {
}
