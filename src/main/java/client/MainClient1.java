package client;

import java.io.IOException;

public class MainClient1 {


    public static void main(String[] args) {

        Client client = new Client("localhost",
                "src/main/java/universal/settings.txt",
                "src/main/java/client/file1.log");
        new Thread(() -> {
            try {
                client.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
