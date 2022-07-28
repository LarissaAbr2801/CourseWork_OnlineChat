import client.Client;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ClientTests {

    static Client client;
    static final File file = new File("src/test/java/testFile.log");
    static final String host = "localhost";
    static ExecutorService executorService;

    @BeforeAll
    public static void initAll() {
        executorService = Executors.newFixedThreadPool(2);
    }

    @BeforeEach
    public void initEach() throws IOException {
        file.createNewFile();
        System.out.println("Тест для метода класса Client запущен");
    }

    @AfterEach
    public void finishEach() {
        file.delete();
        System.out.println("Тест для метода класса Client завершен");
    }

    @AfterAll
    public static void finishAll() {
        executorService.shutdown();
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
    void testWriteMessage(String msg, String expected) throws ExecutionException, InterruptedException {
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.bind(new InetSocketAddress(host, 8080));
            SocketChannel socketChannel = executorService.submit(serverSocket :: accept).get();
            client = new Client(host,
                    "src/main/java/universal/settings.txt",
                    file.getName());
            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);
            while (socketChannel.isConnected()) {

                int bytesCount = socketChannel.read(inputBuffer);

                if (bytesCount == -1) break;

                final var info = new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8).split(": ");

                final String message = info[1];

                inputBuffer.clear();

                socketChannel.write(ByteBuffer.wrap((message)
                        .getBytes(StandardCharsets.UTF_8)));
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        String result = executorService.submit(() -> client.writeMessage(new Scanner(msg))).get();

        Assertions.assertEquals(result, expected);
    }

    private static Stream<Arguments> sourceWriteMessage() {
        return Stream.of(Arguments.of("Привет", "Привет"));
    }

}
