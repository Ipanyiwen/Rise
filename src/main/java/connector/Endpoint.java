package connector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public abstract class Endpoint<S> {

    protected volatile InetAddress address;

    protected volatile int port;

    protected volatile int backlog = 100;

    protected volatile boolean running = false;

    protected volatile S serverSocket;

    public abstract void bind() throws IOException;

    public abstract void init();

    public void start() throws IOException {
        bind();
        init();
    }

    public void stop() {

    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public S getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(S serverSocket) {
        this.serverSocket = serverSocket;
    }

    protected abstract boolean processSocket(Socket socket);



}
