package connector;

import core.LifeCycle;

import java.io.IOException;
import java.net.UnknownHostException;

public class Connector implements LifeCycle {

    private ProtocolHandler protocolHandler;

    private String scheme;

    private String proxyName;

    private int proxyPort;

    private int port;

    public Connector() throws UnknownHostException {
        protocolHandler = new HttpProtocolHandler(this);
        scheme = "http";
    }

    public Connector(ProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    @Override
    public void start() throws IOException {
        protocolHandler.start();

    }

    @Override
    public void stop() {

    }

    public ProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public void setProtocolHandler(ProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
