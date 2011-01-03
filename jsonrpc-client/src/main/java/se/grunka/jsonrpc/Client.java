package se.grunka.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ServiceError.class, new ServiceErrorAdapter()).create();
    private static final String UTF_8 = "UTF-8";
    private static final String MAX_CONNECTIONS_PROPERTY = "se.grunka.jsonrpc.client.maxConnections";


    private static HttpClient createClient(final int connections) {
        HttpParams httpParams = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(httpParams, connections);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRoute() {
            @Override
            public int getMaxForRoute(HttpRoute route) {
                return connections;
            }
        });
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ThreadSafeClientConnManager clientConnManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
        return new DefaultHttpClient(clientConnManager, httpParams);
    }


    private static String[] parameterNames(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        String[] parameterNames = new String[parameterAnnotations.length];
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (int j = 0; j < parameterAnnotations[i].length; j++) {
                if (parameterAnnotations[i][j] instanceof Name) {
                    parameterNames[i] = ((Name) parameterAnnotations[i][j]).value();
                }
                if (parameterNames[i] == null) {
                    throw new IllegalArgumentException("No @Name annotation found for " + method.getParameterTypes()[i] + " on " + method.getName());
                }
            }
        }
        return parameterNames;
    }


    private static String methodName(Method method) {
        Name annotation = method.getAnnotation(Name.class);
        if (annotation == null) {
            return method.getName();
        } else {
            return annotation.value();
        }
    }


    public static <T> T create(Class<? extends T> serviceInterface, String url) {
        int maxConnections = 100;
        String property = System.getProperty(MAX_CONNECTIONS_PROPERTY);
        if (property != null) {
            try {
                maxConnections = Integer.parseInt(property, 10);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(MAX_CONNECTIONS_PROPERTY + " is not a valid number", e);
            }
        }
        return create(serviceInterface, url, maxConnections);
    }


    @SuppressWarnings("unchecked")
    public static <T> T create(Class<? extends T> serviceInterface, String url, int maxConnections) {
        final HttpClient httpClient = createClient(maxConnections);
        final Map<Method, Names> methodNames = new HashMap<Method, Names>();
        if (!url.endsWith("/")) {
            url += "/";
        }
        for (Method method : serviceInterface.getMethods()) {
            methodNames.put(method, new Names(url + methodName(method), parameterNames(method)));
        }
        return (T) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class[]{serviceInterface}, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Names names = methodNames.get(method);
                HttpPost post = new HttpPost(names.getMethod());
                post.setEntity(new StringEntity(createJson(names.getParameters(), args), UTF_8));
                HttpResponse response = httpClient.execute(post);
                HttpEntity httpEntity = response.getEntity();
                Reader content = new InputStreamReader(httpEntity.getContent());
                try {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        ServiceError error = GSON.fromJson(content, ServiceError.class);
                        Constructor<? extends Throwable> constructor = error.getType().getConstructor(String.class);
                        throw constructor.newInstance(error.getMessage());
                    } else {
                        return GSON.fromJson(content, method.getReturnType());
                    }
                } catch (RuntimeException e) {
                    post.abort();
                    throw e;
                } finally {
                    content.close();
                }
            }
        });
    }


    private static class Names {
        private final String method;
        private final String[] parameters;


        public String getMethod() {
            return method;
        }


        public String[] getParameters() {
            return parameters;
        }


        public Names(String method, String[] parameters) {
            this.method = method;
            this.parameters = parameters;
        }
    }


    private static String createJson(String[] names, Object[] args) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        for (int i = 0; i < names.length; i++) {
            parameters.put(names[i], args[i]);
        }
        return GSON.toJson(parameters);
    }
}
