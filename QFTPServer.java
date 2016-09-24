import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class QFTPServer {

    public static String directoryRoot;
    public static int controlPort;
    public static String welcomeMessage = "200 Welcome to the server, please enter the file you would like to download.";
    private static HashMap<String, QFTPPosition> positionCache;

    public static void main(String[] args) {

        if(args.length != 2) {
            System.out.println("usage: java QFTPServer <control port> <directory root>");
            return;
        }

        positionCache = new HashMap<>();
        directoryRoot = args[1];

        new Thread(new QFTPControlServer(positionCache)).start();

        ServerSocket serverSocket;
        try {
            controlPort = Integer.parseInt(args[0]);
            serverSocket = new ServerSocket(controlPort);
        } catch (IOException e) {
            System.err.println("Problem connecting to port 17, please use sudo.");
            return;
        }

        while(true) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        new QFTPSession(socket, positionCache).run();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
