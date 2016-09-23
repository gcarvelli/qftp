import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class QFTPControlServer implements Runnable {
    private HashMap<String, QFTPPosition> positionCache;

    public QFTPControlServer(HashMap<String, QFTPPosition> positionCache) {
        this.positionCache = positionCache;
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
            String line = reader.readLine();
            File f = new File(QFTPServer.directoryRoot + line);
            if(f.exists() && !f.isDirectory()) {
                QFTPPosition position = new QFTPPosition();
                position.position = 0;
                position.filename = f.getAbsolutePath();
                positionCache.put(socket.getInetAddress().toString(), position);

                writer.write("200 file found\r\n");
                writer.flush();
                writer.close();
            } else {
                writer.write("500 file not found\r\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
