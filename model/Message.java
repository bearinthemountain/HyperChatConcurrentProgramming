package model;

import java.time.Instant;

public record Message(String senderId,
                      String roomId,
                      String textContenent,
                      Instant timestamp) {
    //classe Record permet d'être sur qu'un message est immuable

    //record crée automatiquement getter et setter
}
