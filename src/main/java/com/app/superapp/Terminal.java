package com.app.superapp;

import com.app.superapp.Socket.SockAddr;
import com.sun.jna.*;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.app.superapp.Terminal.SysCalls.SOCK_STREAM;

public class Terminal {
    Stage stage;
    AnchorPane pane;
    TextArea textArea;
    String marker = "[" + System.getProperty("user.name") + "] ";

    void start(){
        stage = new Stage();
        textArea = new TextArea();
        pane = new AnchorPane(textArea);
        AnchorPane.setTopAnchor(textArea, (double) 0);
        AnchorPane.setBottomAnchor(textArea, (double) 0);
        AnchorPane.setRightAnchor(textArea, (double) 0);
        AnchorPane.setLeftAnchor(textArea, (double) 0);
        Scene scene = new Scene(pane, 600, 400);
        load();
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Терминал");
        stage.show();
    }

    void load(){
        textArea.setOnKeyPressed(this::enter);
        textArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            int caretPosition = newValue.intValue();

            if (caretPosition < marker.length()) {
                textArea.positionCaret(marker.length());
            } else if (caretPosition > textArea.getLength()) {
                textArea.positionCaret(textArea.getLength());
            }
        });
        textArea.appendText(marker);
    }
    void enter(KeyEvent event){
        String currentLine = textArea.getParagraphs().get(textArea.getParagraphs().size()-1).toString();
        if (!currentLine.startsWith(marker))
            textArea.replaceText(textArea.getText().lastIndexOf(currentLine), textArea.getText().lastIndexOf(currentLine) + currentLine.length(), marker);
        if(event.getCode().equals(KeyCode.ENTER)){

            String lastStroke = String.valueOf(textArea.getParagraphs().get(textArea.getParagraphs().size()-2));
            String command = lastStroke.substring(marker.length());
            textArea.appendText(Commands.enterCommand(command) + "\n");
            textArea.appendText(marker);
        }
        if(textArea.getCaretPosition() < marker.length() && event.getCode().equals(KeyCode.BACK_SPACE)){
            textArea.appendText(marker.substring(marker.length()-1));
        }
    }
    static class Commands {
        static int currentFD;
        static int currentSD;
        static ArrayList<Integer> fds;
        public static String enterCommand(String command){
            String[] commands = command.split(" ");
            switch (commands[0]){

                case "mkdir" -> {
                    return makeDir(commands[1], commands[2]); // 1 - путь, 2 - имя каталога
                }
                case "open" -> {
                    return open(commands[1]); // принимает путь до файла для запуска его файлового дескриптора
                }
                case "close" -> {
                    return close(Integer.parseInt(commands[1])); // принимает файловый дескриптор
                }
                case "fdMax" -> {
                    return fdMax();
                }
                case "crservsock" -> {
                    return createServerSocket();
                }
                case "bindsock" -> {
                    return bindSocket(Integer.parseInt(commands[1]), commands[2], Integer.parseInt(commands[3])); // 1 - сокет, 2 - адрес, 3 - порт
                }
                case "connectsock" -> {
                    return connectSocket(Integer.parseInt(commands[1]), commands[2], Integer.parseInt(commands[3])); // 1 - сокет, 2 - адрес, 3 - порт
                }
                case "sendtoserv" -> {
                    return sendData(Integer.parseInt(commands[1]), commands[2]); // 1 - адрес, 2 - порт, 3 - сообщение
                }
                case "recvmsg" -> {
                    return receiveData(Integer.parseInt(commands[1])); // принимает дескриптор сокета
                }
                case "listensock" -> {
                    return listenSocket(Integer.parseInt(commands[1]), Integer.parseInt(commands[2])); // 1 - соект 2 - очередь сообщений
                }
                case "closesock" -> {
                    return closeSocket(Integer.parseInt(commands[1])); // принимает дескриптор сокета
                }
                case "help" -> {
                    return """
                            mkdir ПУТЬ ИМЯ_КАТАЛОГА - создать каталог по выбранному пути
                            open ПУТЬ - запускает файловый дескриптор. Принимает путь до файла
                            close файловый_дескриптор - закрывает файловый дескриптор
                            fdMax - выводит максимальное число файловых дескрипторов
                            crservsock - создает сокет. Возвращает дескриптор сокета
                            bindsock СОКЕТ АДРЕС ПОРТ - привязывает сокет к адресу в Интернете.
                                                        Требуется для создания сервера
                            connectsock СОКЕТ АДРЕС ПОРТ - соединяет сокет с указанным адресом.
                                          Требуется для подключения клиента к серверу
                            sendtoserv СОКЕТ СООБЩЕНИЕ - отправить сообщение на сервер
                            recvmsg СОКЕТ - принять сообщение от клиента
                            listensock СОКЕТ ОЧЕРЕДЬ_СООБЩЕНИЙ - прослушивать сокет
                            closesock СОКЕТ - закрыть сокет
                            """;
                }
                default -> {
                    return "Неверная команда. help для информации по командам";
                }
            }
        }

        static String makeDir(String path, String catalogName){
            if(SysCalls.INSTANCE.mkdir(path+"/"+catalogName, 0777) == 0)// 0777 - Режим полного доступа
                return "Директория " + path+"/"+catalogName + " создана";
            else return "Ошибка создания директории";
        }
        static String open(String pathname) {
            currentFD = SysCalls.INSTANCE.open(pathname, 2); //2 - флаг открытия для чтения или записи
            fds.add(currentFD);
            if (currentFD != -1)
                return "Файл открыт с файловым дескриптором - " + currentFD;
            else return "Ошибка в открытии файла";
        }

        static String close(int fd) {
            fds.remove((Integer) fd);
            if (SysCalls.INSTANCE.close(fd) == 0)
                return "Файл закрыт";
            else return "Ошибка в закрытии файла";
        }
        static String fdMax(){
            return "Максимальное количество открытых файловых дескрипторов: " + getMaxFileDescriptors() + "\n";
        }
        public static int getMaxFileDescriptors() {
            // Получаем максимальное количество открытых файловых дескрипторов
            return SysCalls.INSTANCE.getdtablesize();
        }


        static String createServerSocket(){
            int protocol = 0; // Для протокола по умолчанию

            int socketDescriptor = SysCalls.INSTANCE.socket(SysCalls.AF_INET, SOCK_STREAM, protocol);
            if (socketDescriptor == -1) {
                return "Ошибка создания сокета";
            } else {
                currentSD = socketDescriptor;
                return "Дескриптор созданного сокета " + socketDescriptor;
            }
        }
        static String bindSocket(int socketDesc, String hostname, int port) {
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(hostname, port);
                InetAddress address = socketAddress.getAddress();
                byte[] addressBytes = address.getAddress();

                SockAddr addr = new SockAddr();
                addr.sin_family = 2; //IPv4
                addr.sin_port = (short) port;
                System.arraycopy(addressBytes, 0, addr.sin_addr, 0, addressBytes.length);

                int result = SysCalls.INSTANCE.bind(socketDesc, addr, addr.size());

                if (result == -1) {
                    return "Не удалось связать сокет с адресом";
                } else {
                    return "Сокет с адресом успешно связан";
                }
            } catch (Exception e) {
                return "Ошибка: " + e.getMessage();
            }
        }
        static String connectSocket(int socketDesc, String hostname, int port) {
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(hostname, port);
                InetAddress address = socketAddress.getAddress();
                byte[] addressBytes = address.getAddress();

                SockAddr addr = new SockAddr();
                addr.sin_family = 2; //IPv4
                addr.sin_port = (short) port;
                System.arraycopy(addressBytes, 0, addr.sin_addr, 0, addressBytes.length);

                int result = SysCalls.INSTANCE.connect(socketDesc, addr, addr.size());

                if (result == -1) {
                    return "Не удалось установить соединение с адресом";
                } else {
                    return "Соединение с адресом успешно установлено";
                }
            } catch (Exception e) {
                return "Ошибка: " + e.getMessage();
            }
        }
        static String receiveData(int clientSocket) {
            try {
                byte[] buffer = new byte[1024]; // Буфер для принимаемых данных

                int bytesRead = SysCalls.INSTANCE.recv(clientSocket, buffer, buffer.length, 0);

                if (bytesRead == -1) {
                    return "Ошибка при приеме данных";
                }

                String data = new String(Arrays.copyOf(buffer, bytesRead));
                return "Полученные данные: " + data;
            } catch (Exception e) {
                e.printStackTrace();
                return new String();
            }
        }
        static String sendData(int socketDesc, String msg) {
            try {
                byte[] messageBytes = msg.getBytes();
                Memory buffer = new Memory(msg.length());
                buffer.write(0, messageBytes, 0, msg.length());

                int result = SysCalls.INSTANCE.send(socketDesc, buffer, msg.length(), 0);

                if (result == -1) {
                    return "Ошибка при отправке данных";
                } else {
                    return "Данные успешно отправлены";
                }
            } catch (Exception e) {
                return "Ошибка: " + e.getMessage();
            }
        }
    }
    static String listenSocket(int socketDesc, int backlog) {
        try {
            int result = SysCalls.INSTANCE.listen(socketDesc, backlog);

            if (result == -1) {
                return "Ошибка при установке очереди ожидания";
            } else {
                return "Очередь ожидания успешно установлена";
            }
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }
        static String closeSocket(int socket){
            if(SysCalls.INSTANCE.close(socket) == 0)
                return "Сокет закрыт";
            else return "Ошибка при закрытии сокета";
        }
        interface SysCalls extends Library{
            SysCalls INSTANCE = Native.load("c", SysCalls.class);
            int AF_INET = 2; // значение для IPv4
            int SOCK_STREAM = 1; // значение для TCP
            int F_GETPATH = 50; // Этот флаг будет использоваться в fcntl
            int socket(int domain, int type, int protocol);
            int connect(int socket, SockAddr address, int addressLength);
            int accept(int socket, SockAddr address, int addressLength);
            int send(int socket, Memory buffer, int size, int flags);
            int bind(int sockfd, SockAddr addr, int addrlen);
            int listen(int socket, int backlog);
            int recv(int sockfd, byte[] buf, int len, int flags);
            int getnameinfo(Pointer sa, int salen, Pointer host, int hostlen, Pointer serv, int servlen, int flags);

            int readlink(String path, byte[] buffer, int size);
            int fcntl(int fd, int cmd, Pointer arg);
            int mkdir(String path, int mode);
            int open(String path, int flags);
            int close(int fd);
            // Системный вызов для получения списка открытых файловых дескрипторов
            int getfdlist(FDesc[] list, int size);
            int getdtablesize();

            // Структура для хранения списка открытых файловых дескрипторов
            class FDesc extends Structure {
                public int fd;
                public String name;

                protected List<String> getFieldOrder() {
                    return Arrays.asList("fd", "name");
                }
            }


        }
}
