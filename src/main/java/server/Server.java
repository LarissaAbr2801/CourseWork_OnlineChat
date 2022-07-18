package server;

import universal.logger.ChatsLogger;
import universal.logger.Logger;
import universal.reader.SettingsFileReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

public class Server {

    private final String host;
    private final int port;
    private final Logger logger;
    private final String fileNameForLog;
    private ServerSocketChannel serverSocket;

    public Server(String host, String fileName, String fileNameForLog) {
        this.host = host;
        this.port = new SettingsFileReader(fileName).read();
        this.logger = ChatsLogger.getInstance();
        this.fileNameForLog = fileNameForLog;
    }

    public void start() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(host, port));
    }

    public void process() {

        try (SocketChannel socketChannel = serverSocket.accept()) {

            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);

            while (socketChannel.isConnected()) {
                int bytesCount = socketChannel.read(inputBuffer);

                if (bytesCount == -1) break;

                final var info = new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8).split(": ");

                final String clientName = info[0];
                final String msg = info[1];
                inputBuffer.clear();

                logger.log(String.format("Успешное подключение клиента '%s' к серверу!", clientName), fileNameForLog);

                socketChannel.write(ByteBuffer.wrap((msg)
                        .getBytes(StandardCharsets.UTF_8)));

                logger.log(String.format("Сообщение '%s' от пользователя '%s'  отправлено в %s",
                        msg, clientName, LocalTime.now()), fileNameForLog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

