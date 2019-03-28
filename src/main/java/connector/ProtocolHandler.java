package connector;

import java.io.IOException;

public interface ProtocolHandler {

    /**
     * 启动协议
     */
    void start() throws IOException;

    /**
     * 停止协议
     */
    void stop();

}
