package core;


import connector.Request;
import connector.Response;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandService implements Service{

    private List<Container> containers = new ArrayList<>();

    private Server server;

    private String name;

    private Map<String, Container> servletMap = new HashMap<>();



    public StandService(String name) {
        this.name = name;
        init();

    }

    public void init() {
        String path = Constants.WEB_ROOT + File.separator + name;
        findServlets(new File(path));
    }

    @Override
    public void start() throws IOException {
        for (Container container : containers) {
            container.start();
        }
    }

    @Override
    public void stop() {
        for (Container container : containers) {
            container.stop();
        }

    }

    @Override
    public void addContainer(Container container) throws IOException {
        if (container != null) {
            containers.add(container);
            container.start();
        }
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    private void findServlets(File file) {
        // 解析xml
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                loadClass(f.getPath());
            } else {
                findServlets(f);
            }
        }
    }

    @Override
    public void invoke(Request request, Response response) {
        String uri = request.getRequestURI();
        String key = uri.substring(name.length() + (uri.startsWith("/") ? 2 : 1), uri.length());
        key = key.replaceAll(File.separator, ".");

        System.out.println(key);
        if (servletMap.containsKey(key)) {
            try {
                servletMap.get(key).invoke(request, response);
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(uri);
            try {
                String NotFound404 = new String("HTTP/1.1 404 Not Found\r\n" +
                        "Connection: keep-alive\r\n" +
                        "Content-Encoding: utf-8\r\n" +
                        "Content-Type: text/html; charset=utf8,gbk\r\n" +
                        "Server: Rise/1.0.0 \r\n\r\n" +
                        "<html>" +
                        "<head><title>404 Not Found</title></head>" +
                        "<body bgcolor=\"white\">" +
                        "<center><h1>404 Not Found</h1></center>" +
                        "<hr><center>Rise/1.0.0</center>" +
                        "</body>" +
                        "</html>");
                response.getWriter().write(NotFound404);
                response.getWriter().flush();
//                response.sendError(404);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void loadClass(String filePath) {
        String path = Constants.WEB_ROOT + File.separator + name;
        filePath = filePath.substring(path.length(), filePath.length());
        filePath = filePath.replaceAll(File.separator, ".");
        String servletName = filePath.substring(0, filePath.lastIndexOf("."));
        System.out.println(servletName);
        URLClassLoader loader = null;
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(Constants.WEB_ROOT+File.separator + name);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString() ;
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        }
        catch (IOException e) {
            System.out.println(e.toString());
        }
        Class myClass = null;
        try {
            myClass = loader.loadClass(servletName);
            Servlet servlet = (Servlet) myClass.newInstance();
            ServletContainer container = new ServletContainer(servlet);
            servletMap.put(servletName, container);
        }
        catch (ClassNotFoundException e) {
            System.out.println(e.getStackTrace());
        } catch (IllegalAccessException e) {
            System.out.println(e.getStackTrace());
        } catch (InstantiationException e) {
            System.out.println(e.getStackTrace());
        }


    }
}
