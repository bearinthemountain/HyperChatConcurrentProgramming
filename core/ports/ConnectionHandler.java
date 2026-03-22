package core.ports;

import model.ClientContext;

public interface ConnectionHandler {
    void handleClient(ClientContext clientContext);
}
