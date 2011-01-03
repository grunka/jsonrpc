package se.grunka.jsonrpc;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class ServiceServer {
    private final Server server;
    private final ServletContextHandler handler;


    private ServiceServer(Server server, ServletContextHandler handler) {
        this.server = server;
        this.handler = handler;
    }

    public static ServiceServer create(final int port) {
        Server server = new Server();
        server.setConnectors(new Connector[]{createConnector(port)});
        ServletContextHandler handler = createHandler();
        server.setHandler(handler);
        server.setSendServerVersion(false);
        server.setStopAtShutdown(true);
        return new ServiceServer(server, handler);
    }

    private static ServletContextHandler createHandler() {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.setContextPath("/");
        return servletContextHandler;
    }


    private static Connector createConnector(final int port) {
        //TODO allow configuration
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setName("ServiceServerConnector");
        connector.setPort(port);
        connector.setMaxIdleTime(30000);
        connector.setAcceptors(Runtime.getRuntime().availableProcessors());
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("ServiceServerConnectorThreadPool");
        threadPool.setMinThreads(256);
        threadPool.setMaxThreads(256);
        threadPool.setDaemon(true);
        connector.setThreadPool(threadPool);
        return connector;
    }


    public void serveForever() {
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            throw new Error("Serving forever failed", e);
        }
    }


    public <T> void addService(String path, Class<? extends T> serviceInterface, T serviceInstance) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        handler.addServlet(new ServletHolder(ServiceServlet.create(path, serviceInterface, serviceInstance)), path + "*");
    }
}
