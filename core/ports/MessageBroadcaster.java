package core.ports;

import model.Message;

public interface MessageBroadcaster {
    //définir les règles

    void broadcast(Message message);
}
