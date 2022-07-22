package client;

import universal.logger.ChatsLogger;
import universal.logger.Logger;
import universal.reader.SettingsFileReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Client {

    private String name;
    private final String host;
    private final int port;
    private final Logger logger;
    private final String fileNameForLog;
    private final List<String> validNames;
    private SocketChannel socketChannel;
    private Scanner scanner;

    private static final String COMMAND_TO_EXIT = "/exit";
    private static final String OCCUPIED_NAME = "Имя уже занято. Введите другое";

    public Client(String host, String fileNameForSetting,
                  String fileNameForLog) {
        this.host = host;
        this.port = new SettingsFileReader(fileNameForSetting).read();
        this.logger = ChatsLogger.getInstance();
        this.fileNameForLog = fileNameForLog;
        validNames = new CopyOnWriteArrayList<>();
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);

        try {
            this.socketChannel = SocketChannel.open();
            this.scanner = new Scanner(System.in);
            try {
                logger.log("Приложение сетевой чат запущено", fileNameForLog);
                socketChannel.connect(socketAddress);
            } catch (IOException e) {
                System.out.println("Не удалось подключиться к серверу!");
                logger.log(String.format("Ошибка при подключении к серверу: '%s' %s",
                        e, LocalTime.now()), fileNameForLog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isValidName(String name) {
        try {
            if (validNames.contains(name)) {
                return false;
            } else {
                validNames.add(name);
            }
        } catch (NoSuchElementException e) {
            System.out.println("Ошибка ввода!");
        }
        return true;
    }

    public String getName() {
        return name;
    }


    public void start() {
        final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);

        System.out.println("Введите имя!");
        name = scanner.nextLine();

        while (true) {
            if (!isValidName(name)) {
                System.out.println(OCCUPIED_NAME);
                name = scanner.nextLine();
            } else {
                break;
            }
        }

        String msg;
        while (true) {
            System.out.println(getName() + ", введите сообщение:");
            msg = writeMessage(scanner);

            if (msg.equals(COMMAND_TO_EXIT)) break;

            readMessage(inputBuffer);
        }
    }

    public String writeMessage(Scanner scanner) {
        String msg = null;
        try {
            msg = scanner.nextLine();

            socketChannel.write(ByteBuffer.wrap((name + ": " + msg)
                    .getBytes(StandardCharsets.UTF_8)));
            logger.log(getName() + ": " + msg, fileNameForLog);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public void readMessage(ByteBuffer inputBuffer) {
        try {
            String msg;
            int bytesCount = socketChannel.read(inputBuffer);
            msg = new String(inputBuffer.array(), 0, bytesCount,
                    StandardCharsets.UTF_8);
            logger.log(String.format("У пользователя '%s' есть новые сообщения: '%s'",
                    name, msg), fileNameForLog);
            inputBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
