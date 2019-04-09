package core;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StaticContainer implements Container {

    @Override
    public void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //读取静态文件，返回结果。
        
    }

    @Override
    public void start() throws IOException {

    }

    @Override
    public void stop() {

    }
}
