package core;

import connector.Connector;

import java.io.IOException;

public class TestMain {
    public static void main(String[] args) throws IOException {
        Server server = new StandServer();
        Service service = new StandService();
        service.addConnector(new Connector());
        server.addService(service);
        server.start();
    }
}
