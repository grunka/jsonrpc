package se.grunka.jsonrpc;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ExperimentsTest {
    private Gson gson;

    @Before
    public void before() throws Exception {
        gson = new Gson();
    }

    @Test
    public void shouldDecodeRequestWithInternalJsonData() throws Exception {
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("string", gson.toJson("a normal string"));
        arguments.put("number", gson.toJson(42));
        Map<String, String> object = new HashMap<String, String>();
        object.put("key", "value");
        arguments.put("object", gson.toJson(object));
        ServiceRequest serviceRequest = new ServiceRequest("a_method", arguments);
        String json = gson.toJson(serviceRequest);
        ServiceRequest result = gson.fromJson(json, ServiceRequest.class);
        System.out.println("json = " + json);

        assertEquals("a_method", result.method());
        assertEquals("\"a normal string\"", result.arguments().get("string"));
        assertEquals("42", result.arguments().get("number"));
        assertEquals("{\"key\":\"value\"}", result.arguments().get("object"));
    }

    @Test
    public void shouldOutputAString() throws Exception {
        assertEquals("\"value\"", gson.toJson("value"));
    }

    @Test
    public void shouldEncodeDecode() throws Exception {
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("string", "hello world");
        arguments.put("number", String.valueOf(42));
        arguments.put("complex", gson.toJson(BigDecimal.valueOf(123)));
        ServiceRequest serviceRequest = new ServiceRequest("method_name", arguments);
        String json = gson.toJson(serviceRequest);
        System.out.println("json = " + json);
    }
}
