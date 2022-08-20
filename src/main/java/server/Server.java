package server;

import universal.logger.ChatsLogger;
import universal.logger.Logger;
import universal.reader.SettingsFileReader;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private static final String COMMAND_TO_EXIT = "/exit";

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
        } catch (SocketException e) {
            String exception = "Соединение с клиентом прервано!";
            System.out.println(exception);
            logger.log(exception, fileNameForLog);
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

            if (msg.equals(COMMAND_TO_EXIT)) {
                socketChannel.write(ByteBuffer.wrap((String.format(COMMAND_TO_EXIT).getBytes(StandardCharsets.UTF_8))));
                break;
            }

            logger.log(String.format("Сообщение '%s' от пользователя '%s'  отправлено в %s",
                    msg, clientName, LocalTime.now()), fileNameForLog);

            inputBuffer.clear();

            new Thread(() -> sendMessageToAllClients(socketChannel, msg)).start();
        }
    }

    public void sendMessageToAllClients(SocketChannel socketChannel, String msg) {
        try {
            clients.stream()
                    .filter(client -> !client.equals(socketChannel))
                    .forEach(client -> {
                        try {
                            client.write(ByteBuffer.wrap((String.format("Новое сообщение от %s : %s; ",
                                            clientName, msg)
                                    .getBytes(StandardCharsets.UTF_8))));
                        } catch (IOException e) {
                            String exception = "Отправка сообщений недоступна по причине отсутствия других клиентов!";
                            System.out.println(exception);
                            logger.log(exception, fileNameForLog);
                        }
                    });
        } catch (Throwable e) {
            System.out.println("Ошибка при записи сообщения!");
            System.out.println(e);
            logger.log(e.toString(), fileNameForLog);
        }
    }
}

