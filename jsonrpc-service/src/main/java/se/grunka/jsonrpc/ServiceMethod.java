package se.grunka.jsonrpc;

import java.lang.reflect.Method;

class ServiceMethod {
    private final Object serviceInstance;
    private final Method method;
    private final Object[] arguments;


    public Object getServiceInstance() {
        return serviceInstance;
    }


    public Method getMethod() {
        return method;
    }


    public Object[] getArguments() {
        return arguments;
    }


    public ServiceMethod(Object serviceInstance, Method method, Object[] arguments) {
        this.serviceInstance = serviceInstance;
        this.method = method;
        this.arguments = arguments;
    }
}
