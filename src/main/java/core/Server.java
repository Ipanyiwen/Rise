package core;

import connector.Connector;

import java.io.IOException;

public interface Server extends LifeCycle {

    void addService(Service service) throws IOException;

    void addConnector(Connector connector) throws IOException;
}
