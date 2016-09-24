package QFTP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QFTPControlServer implements Runnable {
    private QFTPContext context;

    public QFTPControlServer(QFTPContext context) {
        this.context = context;
    }

    public void run() {
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(5050);
            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("connection accepted");
                new Thread(() -> { newConnection(socket); }).start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void newConnection(Socket socket) {
        try {
            System.out.println("newConnection starting");
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(context.welcomeMessage + "\r\n");
            writer.flush();
            String line = reader.readLine();
            // Make sure we're not getting hacked
            Path path;
            try {
                path = QFTPSecurity.resolvePath(Paths.get(context.directoryRoot), Paths.get(line));
            }  catch (IllegalArgumentException e) {
                writer.write(e.getMessage() + "\r\n");
                writer.flush();
                writer.close();
                e.printStackTrace();
                return;
            }

            // Make sure the file exists
            File f = new File(path.toString());
            context.positionCache.remove(socket.getInetAddress().toString());
            if(f.exists() && !f.isDirectory()) {
                QFTPPosition position = new QFTPPosition();
                position.position = 0;
                position.filename = f.getAbsolutePath();
                context.positionCache.put(socket.getInetAddress().toString(), position);

                writer.write("200 file found\r\n");
                writer.flush();
                writer.close();
            } else {
                writer.write("400 file not found\r\n");
                writer.flush();
                writer.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
