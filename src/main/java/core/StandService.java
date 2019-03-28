package core;

import connector.Connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StandService implements Service{

    private List<Connector> connectors = new ArrayList<>();

    private Container container;

    private Server server;

    @Override
    public void start() throws IOException {
        if (container != null) {
            container.start();
        }

        for (Connector connector : connectors) {
            connector.start();
        }

    }

    @Override
    public void stop() {
        if (container != null) {
            container.stop();
        }

        for (Connector connector : connectors) {
            connector.stop();
        }

    }

    @Override
    public void addConnector(Connector connector) throws IOException {
        if (connector != null) {
            connectors.add(connector);
            connector.start();
        }
    }

    @Override
    public void setContainer(Container container) throws IOException {
        if (container != null) {
            this.container.stop();
            this.container = container;
            container.start();
        }
    }

    @Override
    public Server getServer() {
        return server;
    }
}
