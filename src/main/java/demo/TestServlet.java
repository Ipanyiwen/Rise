package demo;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class TestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("-----------------------");
        resp.setContentType("text/html");
        PrintWriter var3 = resp.getWriter();
        var3.println("<html>");
        var3.println("<head>");
        var3.println("<title>Test Servlet</title>");
        var3.println("</head>");
        var3.println("<body>");
        var3.println("<h2>Hello Test</h2");
//        Enumeration var4 = req.getHeaderNames();
//
//        while(var4.hasMoreElements()) {
//            String var5 = (String)var4.nextElement();
//            var3.println("<br>" + var5 + " : " + req.getHeader(var5));
//        }
//
//        var3.println("<br><h2>Method</h2");
//        var3.println("<br>" + req.getMethod());
//        var3.println("<br><h2>Parameters</h2");
//        Enumeration var7 = req.getParameterNames();
//
//        while(var7.hasMoreElements()) {
//            String var6 = (String)var7.nextElement();
//            var3.println("<br>" + var6 + " : " + req.getParameter(var6));
//        }
//
//        var3.println("<br><h2>Query String</h2");
//        var3.println("<br>" + req.getQueryString());
//        var3.println("<br><h2>Request URI</h2");
//        var3.println("<br>" + req.getRequestURI());
        var3.println("</body>");
        var3.println("</html>");
        var3.flush();
    }
}
