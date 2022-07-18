import client.Client;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import universal.logger.ChatsLogger;
import universal.logger.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ClientTests {

    Client client;
    File file = new File("src/main/universal/settings.txt");
    ExecutorService executorService;

    @BeforeAll
    public void initAll() {
        client = new Client("localhost",
                "src/main/java/universal/settings.txt",
                "src/main/java/client/file.log");
        executorService = Executors.newFixedThreadPool(6);
    }

    @BeforeEach
    public void initEach() {
        System.out.println("Тест для метода класса Client запущен");
    }

    @AfterEach
    public void finishEach() {
        System.out.println("Тест для метода класса Client завершен");
    }


    @AfterAll
    public void finishAll() {
        executorService.shutdown();
    }

    @ParameterizedTest
    @MethodSource("sourceChooseName")
    void testChooseName(String expected) {
        String result = client.chooseName(expected);
        Assertions.assertEquals(expected, result);
    }

    private static Stream<String> sourceChooseName() {
        return Stream.of("Иван");
    }

    @ParameterizedTest
    @MethodSource("sourceStart")
    void testStart(String expected) {
        client.start();

    }

    private static Stream<String> sourceStart() {
        return Stream.of();
    }
}
