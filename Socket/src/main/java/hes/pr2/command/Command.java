package hes.pr2.command;

import hes.pr2.model.client.Client;

public interface Command {
    void execute(Client client, String[] args);
}