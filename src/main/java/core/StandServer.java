package core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StandServer implements Server{

    private List<Service> services = new ArrayList<>();

    @Override
    public void start() throws IOException {
        for (Service service : services) {
            service.start();
        }
    }

    @Override
    public void stop() {
        for (Service service : services) {
            service.stop();
        }

    }

    @Override
    public void addService(Service service) throws IOException {
        if (service != null) {
            services.add(service);
            service.start();
        }
    }
}
