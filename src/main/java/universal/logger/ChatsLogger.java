package universal.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ChatsLogger implements Logger {

    private static ChatsLogger logger;

    private ChatsLogger() {
    }

    public static ChatsLogger getInstance() {
        if (logger == null) return logger = new ChatsLogger();
        return logger;

    }

    public void log(String msg, String fileName) {
        try (BufferedWriter bfWriter = new BufferedWriter(new FileWriter(fileName, true))) {
            bfWriter.write(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
