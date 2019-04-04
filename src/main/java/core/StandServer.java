package core;

import connector.Connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class StandServer implements Server{

    private List<Service> services = new ArrayList<>();

    private List<Connector> connectors = new ArrayList<>();

    public void initApps() {
        File files = new File(Constants.WEB_ROOT);
        for (File file : files.listFiles()) {
            if (file.isDirectory()) {
                System.out.println(file.getName());
                Service service = new StandService(file.getName());
                service.setServer(this);
                services.add(service);
                Mapper.addService(file.getName(), service);
            }

        }
    }




    @Override
    public void start() throws IOException {
        initApps();

        for (Service service : services) {
            service.start();
        }

        for (Connector connector : connectors) {
            connector.start();
        }
    }

    @Override
    public void stop() {
        for (Service service : services) {
            service.stop();
        }

        for (Connector connector : connectors) {
            connector.stop();
        }

    }

    @Override
    public void addService(Service service) throws IOException {
        if (service != null) {
            services.add(service);
            service.setServer(this);
            service.start();
        }

    }

    @Override
    public void addConnector(Connector connector) throws IOException {
        if (connector != null) {
            connectors.add(connector);
            connector.setServer(this);
            connector.start();
        }
    }
}
