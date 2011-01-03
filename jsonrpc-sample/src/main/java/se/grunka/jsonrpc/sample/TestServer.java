package se.grunka.jsonrpc.sample;

import se.grunka.jsonrpc.ServiceServer;

public class TestServer {
    public static void main(String[] args) {
        ServiceServer server = ServiceServer.create(8888);
        server.addService("/api", TestInterface.class, new TestService());
        server.serveForever();
    }
}
