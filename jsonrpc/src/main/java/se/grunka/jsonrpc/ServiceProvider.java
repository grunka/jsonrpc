package se.grunka.jsonrpc;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    @SuppressWarnings("unchecked")
    public <T> T createService(Class<? extends T> type, String endpointUrl) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { type }, createHandler(endpointUrl, type));
    }

    private <T> InvocationHandler createHandler(String endpointUrl, Class<? extends T> type) {
        final HttpClient client = new DefaultHttpClient();
        final Gson gson = new Gson();
        final URI uri = URI.create(endpointUrl);
        final Map<String, Map<Class<?>[], String[]>> parameterNamesMapping = ServiceDefinitionLookup.getParameterNamesMapping(type);

        return new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                HttpPost httpPost = new HttpPost(uri);
                String methodName = method.getName();
                String[] parameterNames = parameterNamesMapping.get(methodName).get(method.getParameterTypes());
                Map<String, String> arguments = new HashMap<String, String>();
                for (int i = 0; i < args.length; i++) {
                    arguments.put(parameterNames[i], gson.toJson(args[i]));
                }
                ServiceRequest serviceRequest = new ServiceRequest(methodName, arguments);
                String json = gson.toJson(serviceRequest);
                StringEntity stringEntity = new StringEntity(json, "UTF-8");
                httpPost.setEntity(stringEntity);
                HttpResponse httpResponse = client.execute(httpPost);
                InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent());
                return gson.fromJson(inputStreamReader, method.getReturnType());
            }
        };
    }


}
