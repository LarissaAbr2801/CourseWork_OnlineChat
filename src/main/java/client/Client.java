package client;

import universal.logger.ChatsLogger;
import universal.logger.Logger;
import universal.reader.SettingsFileReader;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Client {

    private static final String COMMAND_TO_EXIT = "/exit";

    private final String host;
    private final int port;
    private final Logger logger;
    private final String fileNameForLog;
    private final File fileLog;
    private String name;
    private SocketChannel socketChannel;
    private Scanner scanner;
    private volatile boolean isWorking = true;

    public Client(String host, String fileNameForSetting,
                  String fileNameForLog) {
        this.host = host;
        this.port = new SettingsFileReader(fileNameForSetting).read();
        this.logger = ChatsLogger.getInstance();
        this.fileNameForLog = fileNameForLog;
        this.fileLog = new File(fileNameForLog);
    }

    public void start() throws IOException {
        fileLog.createNewFile();

        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        try {
            this.socketChannel = SocketChannel.open();
            this.scanner = new Scanner(System.in);
            try {
                logger.log("Приложение сетевой чат запущено", fileNameForLog);
                socketChannel.connect(socketAddress);
                process();
            } catch (IOException e) {
                System.out.println("Не удалось подключиться к серверу!");
                logger.log(String.format("Ошибка при подключении к серверу: '%s' %s",
                        e, LocalTime.now()), fileNameForLog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }


    private void process() throws IOException {
        final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);

        System.out.println("Введите имя!");
        name = scanner.nextLine();

        new Thread(() -> readMessage(inputBuffer)).start();

        new Thread(() -> writeMessage(scanner)).start();

    }

    public void writeMessage(Scanner scanner) {
        while (isWorking) {
            try {
                System.out.println(getName() + ", введите сообщение:");
                String msg = scanner.nextLine();

                if (msg.equals(COMMAND_TO_EXIT)) {
                    isWorking = false;
                }

                socketChannel.write(ByteBuffer.wrap((name + ": " + msg)
                        .getBytes(StandardCharsets.UTF_8)));
                logger.log(getName() + ": " + msg, fileNameForLog);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readMessage(ByteBuffer inputBuffer) {
        while (isWorking) {
            try {
                String msg;
                int bytesCount = socketChannel.read(inputBuffer);

                msg = new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8);
                System.out.println(msg);
                logger.log(String.format("У пользователя '%s': '%s'",
                        name, msg), fileNameForLog);

                inputBuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
