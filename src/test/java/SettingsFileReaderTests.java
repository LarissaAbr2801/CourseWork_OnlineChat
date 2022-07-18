import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import universal.reader.Reader;
import universal.reader.SettingsFileReader;

import java.io.*;
import java.util.stream.Stream;

public class SettingsFileReaderTests {

    Reader reader;
    File file = new File("src/test/java//testFile.log");

    @BeforeEach
    public void initEach() throws IOException {
        reader = new SettingsFileReader(file.getAbsolutePath());
        file.createNewFile();
        System.out.println("Тест для метода класса SettingsFileReader запущен");
    }

    @AfterEach
    public void finishEach() {
        file.delete();
        System.out.println("Тест для метода класса SettingsFileReader завершен");
    }

    @ParameterizedTest
    @MethodSource("sourceRead")
    void testRead(int port) {
        try (FileOutputStream writer = new FileOutputStream(file.getAbsolutePath())) {
            byte[] bytes = String.valueOf(port).getBytes();
            writer.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(port, reader.read());
    }

    private static Stream<Integer> sourceRead() {
        return Stream.of(8080, 9999, 6334);
    }

}