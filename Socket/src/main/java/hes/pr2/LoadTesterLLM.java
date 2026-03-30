package hes.pr2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTesterLLM {

    // --- CONFIGURATION ---
    private static final int NUM_CLIENTS = 1000;
    private static final int NUM_VIP_LLM_BOTS = 5;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final String OLLAMA_MODEL = "llama3";

    // --- MÉTRIQUES ---
    private static final AtomicInteger connectedCount = new AtomicInteger(0);
    private static final AtomicInteger totalMessagesReceived = new AtomicInteger(0);
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("🤖 Démarrage du Load Tester LLM avec " + NUM_CLIENTS + " bots...");

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= NUM_CLIENTS; i++) {
                final int botId = i;
                executor.submit(() -> startBot(botId));
                Thread.sleep(10);
            }
            System.out.println("✅ Les " + NUM_CLIENTS + " tâches ont été lancées !");
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    private static void startBot(int botId) {
        boolean isAIBot = botId <= NUM_VIP_LLM_BOTS;
        String botName = isAIBot ? "BotIA-" + botId : "Bot-" + botId;

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            in.readLine(); // Message de bienvenue
            out.println(botName);

            int count = connectedCount.incrementAndGet();
            if (count % 100 == 0) System.out.println("🔌 " + count + " bots connectés...");

            String[] rooms = {"Général", "Gaming", "Aide"};
            String myRoom = rooms[new Random().nextInt(rooms.length)];
            out.println("/join " + myRoom);

            // NOUVEAU : Timer pour éviter que l'IA ne spamme si elle reçoit trop de messages
            final long[] lastSpeakTime = {System.currentTimeMillis()};
            Random random = new Random();

            // --- ÉCOUTE EN ARRIÈRE-PLAN ---
            Thread.startVirtualThread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        int msgCount = totalMessagesReceived.incrementAndGet();
                        if (msgCount % 5000 == 0) {
                            System.out.println("📊 [TRAFIC] " + msgCount + " messages ont circulé !");
                        }

                        // LOGIQUE DE RÉACTION DES BOTS IA
                        if (isAIBot) {
                            // Si le message vient d'une autre IA (et pas de soi-même)
                            if (serverMessage.contains("BotIA-") && !serverMessage.contains(botName)) {
                                long now = System.currentTimeMillis();

                                // Cooldown : Le bot doit s'être tu pendant au moins 20 secondes
                                if (now - lastSpeakTime[0] > 20000) {
                                    // 50% de chance de répondre pour éviter que les 5 IA répondent en même temps
                                    if (random.nextBoolean()) {
                                        lastSpeakTime[0] = now; // On bloque les autres réponses

                                        // Extraction propre du texte du message
                                        int msgIndex = serverMessage.indexOf("Message: ");
                                        int timeIndex = serverMessage.indexOf(" | Timestamp:");
                                        if (msgIndex != -1 && timeIndex != -1) {
                                            String context = serverMessage.substring(msgIndex + 9, timeIndex);

                                            // Simulation d'un temps de frappe (2 à 5 secondes)
                                            Thread.sleep(2000 + random.nextInt(3000));

                                            // On demande à Ollama de réagir !
                                            String response = askOllama(myRoom, botName, context) + " 🤖";
                                            out.println(response);
                                        }
                                    }
                                }
                            }
                        }

                        if (botId == 1) { // L'espion affiche toujours
                            System.out.println("👁️ [ESPION " + botName + "] : " + serverMessage);
                        }
                    }
                } catch (Exception e) {
                    // Silencieux
                }
            });

            // --- BOUCLE D'ENVOI PRINCIPALE ---
            if (isAIBot) {
                // Les IA font juste un message "Brise-glace" au début, puis elles ne parlent
                // que si elles sont provoquées par la boucle d'écoute ci-dessus !
                Thread.sleep(5000 + random.nextInt(10000));
                out.println(askOllama(myRoom, botName, "") + " 🤖");

                // Le thread principal s'endort, l'IA est gérée par le listener
                Thread.sleep(Long.MAX_VALUE);
            } else {
                // Les bots normaux sont FORTEMENT ralentis (un message toutes les 2 à 4 minutes)
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(120000 + random.nextInt(120000));
                    out.println("Hello ! Je suis " + botName + " et je discute dans " + myRoom);
                }
            }

        } catch (Exception e) {
            // Silencieux
        }
    }

    // --- MÉTHODE POUR CONTACTER OLLAMA ---
    private static String askOllama(String room, String botName, String context) {
        try {
            String prompt;
            if (context == null || context.isEmpty()) {
                prompt = "Tu es " + botName + ". Fais une courte phrase pour lancer une discussion dans le salon " + room + ". Ne mets pas de guillemets.";
            } else {
                String cleanContext = context.replace("\"", "'").replace("\n", " ");
                prompt = "Tu es " + botName + " dans le salon " + room + ". Quelqu'un vient de dire : '" + cleanContext + "'. Réagis à ce message en une seule courte phrase. Ne mets pas de guillemets.";
            }

            String jsonRequest = "{\n" +
                    "  \"model\": \"" + OLLAMA_MODEL + "\",\n" +
                    "  \"prompt\": \"" + prompt + "\",\n" +
                    "  \"stream\": false\n" +
                    "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11435/api/generate")) // Assure-toi que c'est le bon port !
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();
            int start = body.indexOf("\"response\":\"") + 12;
            int end = body.indexOf("\"", start);

            if (start > 11 && end > start) {
                return body.substring(start, end).replace("\\n", " ").replace("\\\"", "'");
            }
            return "Ahah, bien dit !";

        } catch (Exception e) {
            return "Désolé, j'ai eu un bug de réflexion...";
        }
    }
}