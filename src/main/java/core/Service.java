package core;

import connector.Request;
import connector.Response;

import java.io.File;

import java.io.IOException;

public interface Service extends LifeCycle {

    void addContainer(Container container) throws IOException;

    Server getServer();

    void setServer(Server server);

    void setName(String name);

    String getName();

    void invoke(Request request, Response response);

}
