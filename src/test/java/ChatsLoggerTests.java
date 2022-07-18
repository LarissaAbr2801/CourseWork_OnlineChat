import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import universal.logger.ChatsLogger;
import universal.logger.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

public class ChatsLoggerTests {

    Logger logger;
    File file = new File("src/test/java//testFile.log");

    @BeforeEach
    public void initEach() throws IOException {
        logger = ChatsLogger.getInstance();
        file.createNewFile();
        System.out.println("Тест для метода класса ChatsLogger запущен");
    }

    @AfterEach
    public void finishEach() {
        file.delete();
        System.out.println("Тест для метода класса ChatsLogger завершен");
    }

    @ParameterizedTest
    @MethodSource("sourceLog")
    void testLog(String msg) {
        logger.log(msg, file.getAbsolutePath());

        StringBuilder readFile = new StringBuilder();

        try (FileReader reader = new FileReader(file.getAbsolutePath())) {
            int c;
            while ((c = reader.read()) != -1) {
                readFile.append((char) c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(msg + "\n", String.valueOf(readFile));
    }

    private static Stream<String> sourceLog() {
        return Stream.of(("Запись в файл"), ("Всем привет!"), ("Всем пока"));
    }

}
