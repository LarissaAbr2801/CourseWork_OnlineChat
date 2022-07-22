import client.Client;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.stream.Stream;

public class ClientTests {

    static Client client;
    static final File file = new File("src/test/java//testFile.log");

    @BeforeAll
    public static void initAll() throws IOException {
        client = new Client("localhost",
                "src/main/java/universal/settings.txt",
                file.getName());
        file.createNewFile();
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
    public static void finishAll() {
       file.delete();
    }

    @ParameterizedTest
    @MethodSource("sourceIsValidName")
    void testIsValidName(String name, boolean expected) {
        boolean result = client.isValidName(name);
        Assertions.assertEquals(expected, result);
    }

    private static Stream<Arguments> sourceIsValidName() {
        return Stream.of(Arguments.of("Иван", true),
                Arguments.of("Иван", false));
    }

    @ParameterizedTest
    @MethodSource("sourceWriteMessage")
    void testWriteMessage(String msg, String expected) {
        String result = client.writeMessage(new Scanner(msg));

        Assertions.assertEquals(result, expected);

    }

    private static Stream<Arguments> sourceWriteMessage() {
        return Stream.of(Arguments.of("Привет", "Привет"));
    }

}
