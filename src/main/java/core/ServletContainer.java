package core;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletContainer implements  Container {

    private Servlet servlet;

    public ServletContainer() {

    }

    public ServletContainer(Servlet servlet) {
        this.servlet = servlet;
    }

    @Override
    public void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String OKMSG = new String("HTTP/1.1 200 OK\r\n" +
                "Connection: keep-alive\r\n" +
                "Content-Encoding: utf-8\r\n" +
                "Content-Type: text/html; charset=utf8,gbk\r\n" +
                "Server: Rise/1.0.0 \r\n\r\n");
        response.getWriter().write(OKMSG);
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
