package core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Mapper {

    private static Map<String, Service> serviceMapper = new ConcurrentHashMap<>();

    public static void addService(String key, Service service) {
        serviceMapper.put(key, service);
    }

    public static Service getService(String key) {
        if (key.startsWith("/")) {
            key = key.substring(1, key.length());
        }
        int i = key.indexOf("/");
        if (i != -1) {
            key = key.substring(0, i);
        }
        if (!serviceMapper.containsKey(key)) {
            return null;
        }

        return serviceMapper.get(key);
    }

}
