import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;

public class QFTPSession {

    private Socket socket;
    private PrintWriter writer;
    private HashMap<String, QFTPPosition> positionCache;

    public QFTPSession(Socket socket, HashMap<String, QFTPPosition> positionCache) throws IOException {
        System.out.println("starting session");
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream());
        this.positionCache = positionCache;
    }

    public void run() {
        SocketAddress remoteAddress = socket.getRemoteSocketAddress();
        System.out.println("running session for " + remoteAddress.toString());

        if(positionCache.keySet().contains(remoteAddress.toString())) {
            writer.write("You're in the dictionary! \r\n");
        } else {
            writer.write("You're not in the dictionary \r\n");
        }

        writer.flush();
        writer.close();
    }
}
