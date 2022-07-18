package client;

public class MainClient {

    static final int THREADS_QUANTITY = 4;

    public static void main(String[] args) {

        for (int i = 0; i < THREADS_QUANTITY; i++) {
            new Thread(new Client("localhost",
                    "src/main/java/universal/settings.txt",
                    "src/main/java/client/file.log")::start).start();
        }
    }
}
