package se.grunka.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ServiceServlet extends HttpServlet {
    //TODO better error messages, possibly with the request / response logged
    //TODO some unit testing :)
    private static final Log LOG = LogFactory.getLog(ServiceServlet.class);

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ServiceError.class, new ServiceErrorAdapter()).create();
    private final Map<String, Gson> parsers;


    private ServiceServlet(Map<String, Gson> parsers) {
        this.parsers = parsers;
    }


    public static ServiceServlet create(String path, Class<?> serviceInterface, final Object serviceInstance) {
        Map<String, MethodInformation> methodInformationMap = new HashMap<String, MethodInformation>();
        for (Method method : serviceInterface.getDeclaredMethods()) {
            String fullName = path + methodName(method);
            MethodInformation methodInformation = MethodInformation.create(method);
            MethodInformation existingMethodInformation = methodInformationMap.get(fullName);
            if (existingMethodInformation != null) {
                methodInformation = existingMethodInformation.merge(methodInformation);
            }
            methodInformationMap.put(fullName, methodInformation);
        }
        Map<String, Gson> parsers = new HashMap<String, Gson>();
        for (Map.Entry<String, MethodInformation> entry : methodInformationMap.entrySet()) {
            final MethodInformation methodInformation = entry.getValue();

            Gson parser = new GsonBuilder()
                    .registerTypeAdapter(ServiceMethod.class, new ServiceMethodParser(methodInformation, serviceInstance))
                    .create();
            parsers.put(entry.getKey(), parser);
        }
        return new ServiceServlet(parsers);
    }


    private static String methodName(Method method) {
        Name annotation = method.getAnnotation(Name.class);
        if (annotation == null) {
            return method.getName();
        } else {
            return annotation.value();
        }
    }




    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object result;
        try {
            Reader reader = req.getReader();
            result = invoke(req.getRequestURI(), reader);
            reader.close();
        } catch (Throwable e) {
            LOG.error("Failed to invoke service", e);
            resp.setStatus(500);
            result = new ServiceError(e.getClass(), e.getMessage());
        }
        PrintWriter writer = resp.getWriter();
        GSON.toJson(result, writer);
        writer.close();
    }


    private Object invoke(String methodName, Reader input) throws Throwable {
        Gson parser = parsers.get(methodName);
        ServiceMethod serviceMethod;
        try {
            serviceMethod = parser.fromJson(input, ServiceMethod.class);
        } catch (JsonParseException e) {
            LOG.error("Failed to parse request", e);
            throw new RuntimeException("Invalid request");
        }
        try {
            return serviceMethod.getMethod().invoke(serviceMethod.getServiceInstance(), serviceMethod.getArguments());
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException("Method not supported", e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
