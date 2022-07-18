package universal.reader;

import java.io.FileReader;
import java.io.IOException;

public class SettingsFileReader implements Reader {

    private final String fileName;

    public SettingsFileReader(String fileName) {
        this.fileName = fileName;
    }


    public int read() {
        StringBuilder port = new StringBuilder();

        try (FileReader reader = new FileReader(fileName)) {
            int c;
            while ((c = reader.read()) != -1) {
                port.append((char) c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(String.valueOf(port));
    }
}
