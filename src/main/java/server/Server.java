package server;

import universal.logger.ChatsLogger;
import universal.logger.Logger;
import universal.reader.SettingsFileReader;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private final String host;
    private final int port;
    private final Logger logger;
    private final String fileNameForLog;
    private ServerSocketChannel serverSocket;
    private final List<SocketChannel> clients;

    public Server(String host, String fileName, String fileNameForLog) {
        this.host = host;
        this.port = new SettingsFileReader(fileName).read();
        this.logger = ChatsLogger.getInstance();
        this.fileNameForLog = fileNameForLog;
        this.clients = new CopyOnWriteArrayList<>();
    }

    public void start() throws IOException {
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(host, port));
        } catch (BindException e) {
            System.out.println("Сервер уже запущен!");
            logger.log(String.format("Ошибка при запуске сервера: '%s' %s",
                    e, LocalTime.now()), fileNameForLog);
        }
    }

    public void process() {

        try (SocketChannel socketChannel = serverSocket.accept()) {

            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);

            clients.add(socketChannel);

            logger.log(String.format("Успешное подключение клиента '%s' к серверу!", socketChannel), fileNameForLog);

            while (socketChannel.isConnected()) {

                int bytesCount = socketChannel.read(inputBuffer);

                if (bytesCount == -1) break;

                final var info = new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8).split(": ");

                final String clientName = info[0];
                final String msg = info[1];
                inputBuffer.clear();

                logger.log(String.format("Сообщение '%s' от пользователя '%s'  отправлено в %s",
                        msg, clientName, LocalTime.now()), fileNameForLog);
                sendMessageToAllClients(socketChannel, msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(String.format("Ошибка при подключении клиента к серверу '%s' %s",
                    e, LocalTime.now()), fileNameForLog);
        }
    }

    public void sendMessageToAllClients(SocketChannel socketChannel, String msg) {
        clients.stream()
                .filter(client -> !client.equals(socketChannel))
                .forEach(client -> {
                    try {
                        client.write(ByteBuffer.wrap((msg)
                                .getBytes(StandardCharsets.UTF_8)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

    }
}

