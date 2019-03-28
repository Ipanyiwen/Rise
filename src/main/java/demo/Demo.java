package demo;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;

public class Demo {
    public static void main(String[] args) throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        File file = new File("/home/pan/temp/t1/");
        URL url = file.toURL();
        System.out.println(url.toString());
        URLClassLoader loader = new URLClassLoader(new URL[]{url});
        Class<?> clazz = loader.loadClass("demo.DemoServlet");
        Object o = clazz.newInstance();
        System.out.println(o);
        ClassLoader parent = loader.getParent();
        System.out.println(parent);
    }
}
