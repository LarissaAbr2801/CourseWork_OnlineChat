import client.Client;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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
        client = new Client(host,
                "src/main/java/universal/settings.txt",
                file.getName());

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
    @MethodSource("sourceWriteMessage")
    void testWriteMessage(String msg, String expected) throws IOException {
        client.writeMessage(new Scanner(msg));

        InetSocketAddress socketAddress = new InetSocketAddress(host, 8080);

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(socketAddress);
        final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);

        int bytesCount = socketChannel.read(inputBuffer);

        msg = new String(inputBuffer.array(), 0, bytesCount,
                StandardCharsets.UTF_8);

        String result = msg;

        Assertions.assertEquals(result, expected);
    }

    private static Stream<Arguments> sourceWriteMessage() {
        return Stream.of(Arguments.of("Привет", "Привет"));
    }

    void testReadMessage(String msg, String expected) throws ExecutionException, InterruptedException {
        //String result = executorService.submit(() -> client.writeMessage(new Scanner(msg))).get();

        //Assertions.assertEquals(result, expected);
    }

    private static Stream<Arguments> sourceReadMessage() {
        return Stream.of(Arguments.of("Привет", "Привет"));
    }
}
