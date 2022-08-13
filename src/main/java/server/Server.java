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
    private final List<SocketChannel> clients;
    private ServerSocketChannel serverSocket;
    private String clientName;


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
            clients.add(socketChannel);
            logger.log(String.format("Успешное подключение клиента '%s' к серверу!", socketChannel),
                    fileNameForLog);
            readMessage(socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(String.format("Ошибка при подключении клиента к серверу '%s' %s",
                    e, LocalTime.now()), fileNameForLog);
        }

    }

    public void readMessage(SocketChannel socketChannel) throws IOException {
        final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);

        while (socketChannel.isConnected()) {

            int bytesCount = socketChannel.read(inputBuffer);

            if (bytesCount == -1) break;

            final var info = new String(inputBuffer.array(), 0, bytesCount,
                    StandardCharsets.UTF_8).split(": ");

            clientName = info[0];
            final String msg = info[1];

            if (msg.equals("/exit")) {
                clients.remove(socketChannel);
            }

            logger.log(String.format("Сообщение '%s' от пользователя '%s'  отправлено в %s",
                    msg, clientName, LocalTime.now()), fileNameForLog);

            inputBuffer.clear();
            sendMessageToAllClients(socketChannel, msg);
        }
    }

    public void sendMessageToAllClients(SocketChannel socketChannel, String msg) {
        if (clients.size() > 1) {
            clients.stream()
                    .filter(client -> !client.equals(socketChannel))
                    .forEach(client -> {
                        try {
                            client.write(ByteBuffer.wrap((String.format("Новое сообщение от %s : %s; ",
                                            clientName, msg)
                                    .getBytes(StandardCharsets.UTF_8))));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } else {
            clients.forEach(client -> {
                try {
                    client.write(ByteBuffer.wrap(("Нет новых сообщений")
                            .getBytes(StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}

