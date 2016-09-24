package QFTP;

import java.io.*;
import java.net.Socket;

public class QFTPSession {

    private QFTPContext context;
    private Socket socket;
    private PrintWriter writer;

    public QFTPSession(QFTPContext context, Socket socket) throws IOException {
        System.out.println("starting session");
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream());
        this.context = context;
    }

    public void run() {
        try {
            String remoteAddress = socket.getInetAddress().toString();
            System.out.println("running session for " + remoteAddress);

            if(context.positionCache.keySet().contains(remoteAddress)) {
                File f = new File(context.positionCache.get(remoteAddress).filename);
                RandomAccessFile fileStream = new RandomAccessFile(f.getAbsoluteFile(), "r");
                fileStream.seek(context.positionCache.get(remoteAddress).position);
                byte[] buffer = new byte[1024];
                int bytesRead = fileStream.read(buffer, 0, 510);

                if(bytesRead > 0) {
                    writer.write(toCharBuffer(buffer));
                    writer.write("\r\n");
                    System.out.println("wrote " + bytesRead + " bytes");
                    context.positionCache.get(remoteAddress).position += bytesRead;
                    System.out.println("new position for " + remoteAddress + ": " + context.positionCache.get(remoteAddress).position);

                    if(fileStream.read() == -1) {
                        context.positionCache.remove(remoteAddress);
                    }
                } else {
                    context.positionCache.remove(remoteAddress);
                }
            } else {
                System.out.println("remote address: " + remoteAddress);
                System.out.println("keyset: " + context.positionCache.keySet());
                writer.write("400 No file transfer is in progress for your IP. Connect to port " + context.controlPort + " to begin a file transfer.\r\n");
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
