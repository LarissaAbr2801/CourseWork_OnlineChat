package server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {

    static final int THREADS_QUANTITY = 4;

    public static void main(String[] args) throws IOException {
        Server server = new Server("localhost", "src/main/java/universal/settings.txt",
                "src/main/java/server/file.log");
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(THREADS_QUANTITY);

        for (int i = 0; i < THREADS_QUANTITY; i++) {
            executorService.submit(new Thread(server::process));
        }
        executorService.shutdown();

    }
}
