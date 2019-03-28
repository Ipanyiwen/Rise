package core;

import java.io.IOException;

public interface LifeCycle {
    void start() throws IOException;

    void stop();
}
