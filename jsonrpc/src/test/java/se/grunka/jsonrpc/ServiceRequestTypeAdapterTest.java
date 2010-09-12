package se.grunka.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceRequestTypeAdapterTest {
    private Gson gson;
    private TypeLookup typeLookup;

    @Before
    public void before() {
        GsonBuilder builder = new GsonBuilder();
        typeLookup = mock(TypeLookup.class);
        builder.registerTypeAdapter(ServiceRequest.class, new ServiceRequestTypeAdapter(typeLookup));
        gson = builder.create();
    }

    @Test
    public void shouldAddArgumentsIfArgumentsAreEmpty() {
        Map<String, Object> arguments = new HashMap<String, Object>();

        ServiceRequest serviceRequest = new ServiceRequest("method_name", arguments);

        assertEquals("{\"method\":\"method_name\"}", gson.toJson(serviceRequest));
    }

    @Test
    public void shouldDeserializeWithoutArgumentsButAddEmptyMap() throws Exception {
        String json = "{\"method\":\"method_name\"}";

        ServiceRequest result = gson.fromJson(json, ServiceRequest.class);

        assertEquals("method_name", result.method());
        assertEquals(0, result.arguments().size());
    }

    @Test
    public void shouldSerializeSimpleObjectsAsSimplyAsPossible() throws Exception {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("string", "text");
        arguments.put("number", 10);
        arguments.put("float", 1.3);
        ServiceRequest serviceRequest = new ServiceRequest("method_name", arguments);

        assertEquals("{\"method\":\"method_name\",\"arguments\":{\"string\":\"text\",\"number\":10,\"float\":1.3}}", gson.toJson(serviceRequest));
    }


    @Test
    public void shouldSerializeComplexObjects() throws Exception {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("complex", BigDecimal.valueOf(123.456));
        ServiceRequest serviceRequest = new ServiceRequest("method_name", arguments);

        assertEquals("{\"method\":\"method_name\",\"arguments\":{\"complex\":123.456}}", gson.toJson(serviceRequest));
    }

    @Test
    public void shouldDeserializeSimpleObjects() throws Exception {
        when(typeLookup.lookup("method_name", "string")).thenReturn(String.class);
        when(typeLookup.lookup("method_name", "integer")).thenReturn(Integer.class);
        when(typeLookup.lookup("method_name", "decimal")).thenReturn(Float.class);
        String json = "{\"method\":\"method_name\",\"arguments\":{\"string\":\"text\",\"integer\":10,\"decimal\":1.3}}";

        ServiceRequest result = gson.fromJson(json, ServiceRequest.class);

        assertEquals(String.class, result.arguments().get("string").getClass());
        assertEquals(Integer.class, result.arguments().get("integer").getClass());
        assertEquals(Float.class, result.arguments().get("decimal").getClass());
        assertEquals("text", result.arguments().get("string"));
        assertEquals(10, result.arguments().get("integer"));
        assertEquals(1.3f, result.arguments().get("decimal"));
    }


    @Test
    public void shouldDeserializeComplexObjects() throws Exception {
        String json = "{\"method\":\"method_name\",\"arguments\":{\"complex\":123.456}}";
        when(typeLookup.lookup("method_name", "complex")).thenReturn(BigDecimal.class);

        ServiceRequest result = gson.fromJson(json, ServiceRequest.class);

        assertEquals(BigDecimal.class, result.arguments().get("complex").getClass());
        assertEquals(BigDecimal.valueOf(123.456), result.arguments().get("complex"));
    }
}
