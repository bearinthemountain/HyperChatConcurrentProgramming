package hes.pr2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTester {

    // Nombre de clients à simuler
    private static final int NUM_CLIENTS = 1000;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final AtomicInteger totalMessagesReceived = new AtomicInteger(0);


    // Compteur thread-safe pour savoir combien de bots sont connectés
    private static final AtomicInteger connectedCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Démarrage du Load Tester avec " + NUM_CLIENTS + " Virtual Threads...");

        //
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= NUM_CLIENTS; i++) {
                final int botId = i;
                
                executor.submit(() -> startBot(botId));
                Thread.sleep(10);
            }
            
            System.out.println("Les " + NUM_CLIENTS + " tâches ont été lancées !");
            
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    private static void startBot(int botId) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 1. Lire le message de bienvenue du serveur ("Welcome, please put your pseudo :")
            in.readLine();

            // 2. Envoyer le pseudo
            String botName = "Bot-" + botId;
            out.println(botName);

            // Afficher la progression dans la console tous les 500 bots
            int count = connectedCount.incrementAndGet();
            if (count % 500 == 0) {
                System.out.println(count + " bots connectés et opérationnels...");
            }

            // 3. Rejoindre un salon au hasard
            String[] rooms = {"Général", "Gaming", "Aide"};
            String myRoom = rooms[new Random().nextInt(rooms.length)];
            out.println("/join " + myRoom);

            Thread.startVirtualThread(() -> {
                try {
                    while (in.readLine() != null) {
                        // On lit le message, mais on l'ignore silencieusement 
                        // (sinon 10 000 bots qui impriment dans la console, ça va freeze ton PC)
                        int msgCount = totalMessagesReceived.incrementAndGet();
                        // On affiche un résumé tous les 50 000 messages pour voir le trafic global
                        if (msgCount % 5000 == 0) {
                            System.out.println("📊 [TRAFIC] " + msgCount + " messages ont été reçus au total sur le réseau !");
                        }
                    }
                } catch (Exception e) {
                    // Silencieux à la déconnexion
                }
            });

            // 5. Envoyer un message aléatoire de temps en temps
            Random random = new Random();
            while (!Thread.currentThread().isInterrupted()) {
                // Le bot attend entre 30 et 60 secondes avant de parler
                // (Pour éviter que 10 000 bots parlent tous à la même seconde)
                Thread.sleep(90000 + random.nextInt(90000));
                out.println("Hello ! Je suis " + botName + " et je discute dans " + myRoom);
            }

        } catch (Exception e) {
            // En cas d'erreur réseau (ex: le serveur est éteint), on ne fait rien pour ne pas spammer
        }
    }
}