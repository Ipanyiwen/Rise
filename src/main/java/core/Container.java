package core;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface Container extends LifeCycle {
    void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
