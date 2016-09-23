import java.io.File;
import java.io.FileInputStream;
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
        try {
            String remoteAddress = socket.getInetAddress().toString();
            System.out.println("running session for " + remoteAddress);

            if(positionCache.keySet().contains(remoteAddress)) {
                File f = new File(positionCache.get(remoteAddress).filename);
                FileInputStream fileStream = new FileInputStream(f);
                byte[] buffer = new byte[1024];
                int bytesRead = fileStream.read(buffer, positionCache.get(remoteAddress).position, 500);

                if(bytesRead > 0) {
                    writer.write(toCharBuffer(buffer));
                    writer.write("\r\n");
                } else {
                    positionCache.remove(remoteAddress);
                }
            } else {
                System.out.println("remote address: " + remoteAddress);
                System.out.println("keyset: " + positionCache.keySet());
                writer.write("You're not in the dictionary \r\n");
            }

            writer.flush();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private char[] toCharBuffer(byte[] byteBuffer) {
        char[] charBuffer = new char[byteBuffer.length];
        for(int i = 0; i < byteBuffer.length; i++) {
            charBuffer[i] = (char)byteBuffer[i];
        }

        return charBuffer;
    }
}
