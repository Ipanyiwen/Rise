package core;

import java.io.IOException;

public interface Server extends LifeCycle {

    void addService(Service service) throws IOException;
}
