package hes.pr2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTester {

    private static final int NUM_CLIENTS = 1000;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final AtomicInteger totalMessagesReceived = new AtomicInteger(0);
    private static final AtomicInteger connectedCount = new AtomicInteger(0);

    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start of the load tester " + NUM_CLIENTS + " Virtual Threads...");

        ScheduledExecutorService metricsReporter = Executors.newSingleThreadScheduledExecutor();
        metricsReporter.scheduleAtFixedRate(LoadTester::printMetrics, 5, 5, TimeUnit.SECONDS);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= NUM_CLIENTS; i++) {
                final int botId = i;
                executor.submit(() -> startBot(botId));
                Thread.sleep(10);
            }

            System.out.println("The " + NUM_CLIENTS + " tasks has been launched !");

            Thread.sleep(Long.MAX_VALUE);
        } finally {
            metricsReporter.shutdownNow();
        }
    }

    private static void printMetrics() {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long max = rt.maxMemory();
        long used = total - free;
        int threads = threadBean.getThreadCount();

        System.out.printf(" METRICS => threads=%d, connected=%d, messages=%d, mem_used=%.2fMB, mem_total=%.2fMB, mem_max=%.2fMB%n",
                threads,
                connectedCount.get(),
                totalMessagesReceived.get(),
                used / 1024.0 / 1024.0,
                total / 1024.0 / 1024.0,
                max / 1024.0 / 1024.0);
    }

    private static void startBot(int botId) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            in.readLine();
            String botName = "Bot-" + botId;
            out.println(botName);

            int count = connectedCount.incrementAndGet();
            if (count % 500 == 0) {
                System.out.println(count + " bots connected and ready...");
            }

            String[] rooms = {"Général", "Gaming", "Aide"};
            String myRoom = rooms[new Random().nextInt(rooms.length)];
            out.println("/join " + myRoom);

            Thread.startVirtualThread(() -> {
                try {
                    while (in.readLine() != null) {
                        int msgCount = totalMessagesReceived.incrementAndGet();
                        if (msgCount % 5000 == 0) {
                            System.out.println("[TRAFIC] " + msgCount + " number of message received on the server");
                        }
                    }
                } catch (Exception ignored) { }
            });

            Random random = new Random();
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(20000 + random.nextInt(10000));
                out.println("Hello ! I am " + botName + " and I talk in " + myRoom);
            }

        } catch (Exception e) {
            // silencieux en cas d'erreur réseau
        }
    }
}