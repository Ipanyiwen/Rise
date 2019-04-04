package core;

import connector.Connector;

import java.io.IOException;

public class TestMain {
    public static void main(String[] args) throws IOException {
        Server server = new StandServer();
        server.addConnector(new Connector());
        server.start();
    }
}
