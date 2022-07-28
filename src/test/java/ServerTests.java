import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import server.Server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ServerTests {

    static Server server;
    static final File file = new File("src/test/java/testFile.log");
    static final String host = "localhost";
    static ExecutorService executorService;

    @BeforeAll
    public static void initAll() throws IOException {
        server = new Server(host, "src/main/java/universal/settings.txt",
                file.getName());
        server.start();
        executorService = Executors.newFixedThreadPool(2);
    }

    @BeforeEach
    public void initEach() throws IOException {
        file.createNewFile();
        System.out.println("Тест для метода класса Server запущен");
    }

    @AfterEach
    public void finishEach() {
        file.delete();
        System.out.println("Тест для метода класса Server завершен");
    }

    @AfterAll
    public static void finishAll() {
        executorService.shutdown();
    }

    @ParameterizedTest
    @MethodSource("sourceSendMessageToAllClients")
    void testSendMessageToAllClients(String msg, List<String> expected) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            executorService.submit(new Thread(server::process));
        }
        //один клиент записывает сообщение
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(host, 8080));
            socketChannel.write(ByteBuffer.wrap((msg)
                    .getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //другие клиенты читают сообщение
        for (int i = 0; i < 2; i++) {
            try (SocketChannel socketChannel = SocketChannel.open()) {
                socketChannel.connect(new InetSocketAddress(host, 8080));
                ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);
                int bytesCount = socketChannel.read(inputBuffer);
                msg = new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8);
                result.add(msg);
                inputBuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Assertions.assertEquals(result, expected);
    }

    private static Stream<Arguments> sourceSendMessageToAllClients() {
        return Stream.of(Arguments.of("Привет", List.of("Привет", "Привет")));
    }
}
