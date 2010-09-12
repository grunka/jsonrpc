package se.grunka.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceServletTest {
    private ServiceServlet target;
    private TypeLookup typeLookup;
    private Gson gson;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private ByteArrayOutputStream out;
    private PrintWriter printWriter;

    @Before
    public void before() throws Exception {
        target = new ServiceServlet(null, null);
        typeLookup = mock(TypeLookup.class);
        gson = new GsonBuilder()
                .registerTypeAdapter(ServiceRequest.class, new ServiceRequestTypeAdapter(typeLookup))
                .create();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);

        out = new ByteArrayOutputStream();
        printWriter = new PrintWriter(out);
        when(resp.getWriter()).thenReturn(printWriter);
    }

    @Test
    public void shouldCallServiceImplementationAndReturnResponse() throws Exception {
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("name", "world");
        when(typeLookup.lookup("sayHello", "name")).thenReturn(String.class);
        whenServiceRequest("sayHello", arguments);

        String result = getPostResponse();

        assertEquals("\"hello world\"", result);
    }

    private void whenServiceRequest(String method, Map<String, Object> arguments) throws IOException {
        String json = gson.toJson(new ServiceRequest(method, arguments));
        when(req.getReader()).thenReturn(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(json.getBytes()))));
    }

    private String getPostResponse() throws ServletException, IOException {
        target.doPost(req, resp);
        printWriter.flush();
        return out.toString();
    }
}
