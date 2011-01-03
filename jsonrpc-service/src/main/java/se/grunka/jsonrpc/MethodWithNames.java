package se.grunka.jsonrpc;

import java.lang.reflect.Method;

class MethodWithNames {
    private final Method method;
    private final String[] parameterNames;

    public Method getMethod() {
        return method;
    }

    public String[] getParameterNames() {
        return parameterNames;
    }

    public MethodWithNames(Method method, String[] parameterNames) {
        this.method = method;
        this.parameterNames = parameterNames;
    }
}
