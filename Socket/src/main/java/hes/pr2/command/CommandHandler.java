package hes.pr2.command;

import hes.pr2.BroadcastManager.BroadCastManager;
import hes.pr2.RoomManager.RoomManager;
import hes.pr2.model.client.Client;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {
    private final Map<String, Command> commands = new HashMap<>();
    private final BroadCastManager broadCastManager;

    public CommandHandler(RoomManager roomManager, BroadCastManager broadCastManager, RoomCommandReceiver roomCommandReceiver) {
        this.broadCastManager = broadCastManager;
        
        // Pas propre car manque l'invoker
        commands.put("/join", new JoinCommand(roomManager, roomCommandReceiver));
        commands.put("/list", new ListCommand(roomManager, roomCommandReceiver));

    }

    public void handle(Client client, String input) {
        if (input.startsWith("/")) {
            // C'est une commande (ex: "/join Gaming")
            String[] parts = input.split(" ");
            String commandName = parts[0].toLowerCase();

            Command command = commands.get(commandName);
            if (command != null) {
                // On exécute la commande correspondante
                command.execute(client, parts);
            } else {
                client.writer.println("Unkown command : " + commandName);
            }
        } else {
            // Ce n'est pas une commande, c'est un message normal pour le salon
            if (client.getRoom() != null) {
                broadCastManager.send(client, client.getRoom(), input);
            } else {
                client.writer.println("/join <salon>.");
            }
        }
    }
}