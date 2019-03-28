package connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Acceptor implements Runnable {

    private Endpoint endpoint;

    public Acceptor(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void run() {
        while (endpoint.isRunning()) {
            ServerSocket s = (ServerSocket) endpoint.getServerSocket();
            try {
                Socket socket = s.accept();
                endpoint.processSocket(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
