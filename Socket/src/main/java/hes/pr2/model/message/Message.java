package hes.pr2.model.message;

import hes.pr2.model.client.Client;
import hes.pr2.model.room.Room;

import java.sql.Timestamp;
import java.util.UUID;

// Immutable Message Record
// A record in Java is a special type of class that is concise and immutable by default.
// It is ideal for representing simple data carriers like messages in a chat application.
// This Message record encapsulates all relevant information about a chat message,
// including the sender, the room it was sent to, the content of the message, and the timestamp of when it was sent.
// The overridden toString() method provides a clear and formatted representation of the message for logging or display purposes.
public record Message(UUID uuid, Room room, Client client, String content, Timestamp timestamp) {
    @Override
    public String toString() {
        return "Client: " + client.getPseudo() + " | Room: " + room.getRoomName() + " | Message: " + content + " | Timestamp: " + timestamp;
    }
}
