package client;

import universal.logger.ChatsLogger;
import universal.logger.Logger;
import universal.reader.SettingsFileReader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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

    private static final String COMMAND_TO_EXIT = "/exit";

    public Client(String host, String fileNameForSetting,
                  String fileNameForLog) {
        this.host = host;
        this.port = new SettingsFileReader(fileNameForSetting).read();
        this.logger = ChatsLogger.getInstance();
        this.fileNameForLog = fileNameForLog;
        validNames = new CopyOnWriteArrayList<>();

    }

    public String chooseName(String name) {
        validNames.add("петр");
        while (true) {
            try {
                if (validNames.contains(name)) {
                    System.out.println("Имя уже занято. Введите другое"); //нужно убрать здесь работу с консолью
                } else {
                    validNames.add(name);
                    break;
                }
            } catch (NoSuchElementException e) {
                System.out.println("Ошибка ввода!");
                return null;
            }
        }
        return name;
    }

    public String getName() {
        return name;
    }


    public synchronized void start() {
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);

        try (final SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            try {
                logger.log("Приложение сетевой чат запущено", fileNameForLog);
                socketChannel.connect(socketAddress);
            } catch (IOException e) {
                System.out.println("Не удалось подключиться к серверу!");
                return;
            }

            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);

            System.out.println("Введите имя!");
            name = chooseName(scanner.nextLine());

            String msg;
            while (true) {
                System.out.println(getName() + ", введите сообщение:");
                msg = scanner.nextLine();

                if (msg.equals(COMMAND_TO_EXIT)) break;

                socketChannel.write(ByteBuffer.wrap((name + ": " + msg).getBytes(StandardCharsets.UTF_8)));
                logger.log(getName() + ": " + msg, fileNameForLog);

                int bytesCount = socketChannel.read(inputBuffer);
                System.out.println("Сообщение доставлено: "
                        + new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8));
                inputBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
