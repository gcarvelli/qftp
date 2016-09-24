import QFTP.QFTPContext;
import QFTP.QFTPControlServer;
import QFTP.QFTPSession;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class QFTPServer {

    private static String welcomeMessage = "200 Welcome to the server, please enter the file you would like to download.";
    private static QFTPContext context;

    public static void main(String[] args) {

        if(args.length != 2) {
            System.out.println("usage: java QFTPServer <control port> <directory root>");
            return;
        }

        context = new QFTPContext();
        context.positionCache = new HashMap<>();
        context.welcomeMessage = welcomeMessage;
        context.directoryRoot = args[1];

        new Thread(new QFTPControlServer(context)).start();

        ServerSocket serverSocket;
        try {
            context.controlPort = Integer.parseInt(args[0]);
            serverSocket = new ServerSocket(context.controlPort);
        } catch (IOException e) {
            System.err.println("Problem connecting to port 17, please use sudo.");
            return;
        }

        while(true) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        new QFTPSession(context, socket).run();
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
