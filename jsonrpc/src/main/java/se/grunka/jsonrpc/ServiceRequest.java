package se.grunka.jsonrpc;

import java.util.Map;

public class ServiceRequest {
    private final String method;
    private final Map<String, String> arguments;

    @SuppressWarnings("unused")
    public ServiceRequest() {
        method = null;
        arguments = null;
    }

    public ServiceRequest(String method, Map<String, String> arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    public String method() {
        return method;
    }

    public Map<String, String> arguments() {
        return arguments;
    }
}
