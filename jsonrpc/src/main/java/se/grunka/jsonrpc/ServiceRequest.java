package se.grunka.jsonrpc;

import java.util.Map;

public class ServiceRequest {
    private final String method;
    private final Map<String, Object> arguments;

    public ServiceRequest(String method, Map<String, Object> arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    public String method() {
        return method;
    }

    public Map<String, Object> arguments() {
        return arguments;
    }
}
