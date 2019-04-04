package core;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StandContainer implements Container {

    private Servlet servlet;

    @Override
    public void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

    @Override
    public void start() throws IOException {

    }

    @Override
    public void stop() {

    }

    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }
}
