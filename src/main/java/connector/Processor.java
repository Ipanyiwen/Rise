package connector;

import javax.servlet.ServletException;
import java.io.IOException;

public interface Processor {
    void process() throws IOException, ServletException;
}
