package core;

import connector.Connector;

import java.io.IOException;

public interface Service extends LifeCycle {

    void addConnector(Connector connector) throws IOException;

    void setContainer(Container container) throws IOException;

    Server getServer();

}
