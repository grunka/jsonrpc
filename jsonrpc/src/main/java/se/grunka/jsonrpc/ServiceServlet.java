package se.grunka.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ServiceServlet<T> extends HttpServlet {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ServiceRequest.class, new ServiceRequestTypeAdapter(null))
            .create();
    private final Map<String, Map<Set<String>, Class<?>[]>> parameterOrdering;
    private final Object service;

    public ServiceServlet(Class<? extends T> serviceInterface, T service) {
        this.service = service;
        parameterOrdering = createParameterOrdering(serviceInterface);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //TODO way of instantiating with a service definition and implementation
        //TODO code cleanup
        //TODO error handling
        //TODO selective level of json encoding, i.e strings should just be json strings not strings in strings
        //TODO automated testing
        ServiceRequest serviceRequest = gson.fromJson(req.getReader(), ServiceRequest.class);
        String requestMethod = serviceRequest.method();
        Map<String, Object> requestParameters = serviceRequest.arguments();
        Set<String> requestParameterNames = requestParameters.keySet();
        for (Map.Entry<Set<String>, Class<?>[]> entry : parameterOrdering.get(requestMethod).entrySet()) {
            Set<String> parameterNames = entry.getKey();
            if (parameterNames.size() == requestParameterNames.size() && parameterNames.containsAll(requestParameterNames)) {
                try {
                    Method serviceMethod = service.getClass().getDeclaredMethod(requestMethod, entry.getValue());
                    int i = 0;
                    Object[] arguments = new Object[parameterNames.size()];
                    for (Object argumentValue : requestParameters.values()) {
                        arguments[i] = gson.fromJson(argumentValue.toString(), entry.getValue()[i]);
                        i++;
                    }
                    Object result = serviceMethod.invoke(service, arguments);
                    PrintWriter writer = resp.getWriter();
                    writer.println(gson.toJson(result));
                    writer.close();
                } catch (NoSuchMethodException e) {
                    throw new Error("Error handling not implemented", e);
                } catch (IllegalAccessException e) {
                    throw new Error("Error handling not implemented", e);
                } catch (InvocationTargetException e) {
                    throw new Error("Error handling not implemented", e);
                }
                break;
            }
        }
    }

    private Map<String, Map<Set<String>, Class<?>[]>> createParameterOrdering(Class<? extends T> serviceInterface) {
        Map<String, Map<Class<?>[], String[]>> parameterMapping = ServiceLookup.getParameterNamesMapping(serviceInterface);
        Map<String, Map<Set<String>, Class<?>[]>> parameterOrdering = new HashMap<String, Map<Set<String>, Class<?>[]>>();
        for (Map.Entry<String, Map<Class<?>[], String[]>> methodEntry : parameterMapping.entrySet()) {
            Map<Set<String>, Class<?>[]> methodParameters = new HashMap<Set<String>, Class<?>[]>();
            parameterOrdering.put(methodEntry.getKey(), methodParameters);
            for (Map.Entry<Class<?>[], String[]> parameterEntry : methodEntry.getValue().entrySet()) {
                Set<String> parameterNames = new HashSet<String>();
                parameterNames.addAll(Arrays.asList(parameterEntry.getValue()));
                methodParameters.put(parameterNames, parameterEntry.getKey());
            }
        }
        return parameterOrdering;
    }
}
