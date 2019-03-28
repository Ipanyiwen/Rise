package connector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class HttpProtocolHandler implements ProtocolHandler{

    private Endpoint<ServerSocket> endpoint;
    private Connector connector;

    public HttpProtocolHandler(Connector connector) throws UnknownHostException {
        this.connector = connector;
        this.endpoint = new Http11Endpoint<ServerSocket>(this);
        endpoint.setPort(8090);
        endpoint.setBacklog(1);
        endpoint.setAddress(InetAddress.getByName("127.0.0.1"));
    }

    public HttpProtocolHandler(Endpoint<ServerSocket> endpoint) {
        this.endpoint = endpoint;
    }


    @Override
    public void start() throws IOException {
        endpoint.start();
    }

    @Override
    public void stop() {

    }

    public Endpoint<ServerSocket> getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint<ServerSocket> endpoint) {
        this.endpoint = endpoint;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }
}
