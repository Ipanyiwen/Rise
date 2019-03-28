package connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Http11Endpoint<S> extends Endpoint<ServerSocket> {

    private Executor executor;

    private Acceptor[] acceptors = null;

    private int accepterNum = 1;

    private HttpProtocolHandler protocolHandler;

    private Connector connector;

    public Http11Endpoint(HttpProtocolHandler protocolHandler) {
        this.protocolHandler =  protocolHandler;
        this.connector = this.protocolHandler.getConnector();
    }

    public void startAccepter() {
        if (acceptors == null) {
            acceptors = new Acceptor[accepterNum];
        }

        for (int i = 0; i < accepterNum; i++) {
            acceptors[i] = new Acceptor(this);
            String threadName = "Http11Endpoint-Acceptor-"+ i;
            Thread t = new Thread(acceptors[i], threadName);
            t.start();
        }
    }

    public void bind() throws IOException {
        if (serverSocket == null) {
            if (address == null) {
                serverSocket = new ServerSocket(port, backlog);
            } else {
                serverSocket = new ServerSocket(port, backlog, address);
            }
        }
    }


   public void init() {
        setRunning(true);
        initExecutor();
        startAccepter();
   }

    @Override
    public void stop() {

    }

    @Override
    protected boolean processSocket(Socket socket) {
        executor.execute(new SocketProcessor(socket, connector));
        return true;
    }

    public void initExecutor() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(Constants.ENDPOINT_POOL_SIZE, Constants.ENDPOINT_POOL_MAX_SIZE, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        }
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Acceptor[] getAcceptors() {
        return acceptors;
    }

    public void setAcceptors(Acceptor[] acceptors) {
        this.acceptors = acceptors;
    }

    public int getAccepterNum() {
        return accepterNum;
    }

    public void setAccepterNum(int accepterNum) {
        this.accepterNum = accepterNum;
    }

    public HttpProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public void setProtocolHandler(HttpProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }
}
