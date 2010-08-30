package se.grunka.jsonrpc;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExperimentsTest {
    private Gson gson;


    @Before
    public void before() throws Exception {
        gson = new Gson();
    }


    @Ignore
    @Test
    public void shouldDecodeRequestWithInternalJsonData() throws Exception {
        ServiceRequest serviceRequest = null;//new ServiceRequest("a_method", Arrays.asList("normal string", "42", "{ \"key\": \"value\" }"));
        String json = gson.toJson(serviceRequest);
        ServiceRequest result = gson.fromJson(json, ServiceRequest.class);
        System.out.println("json = " + json);

        assertEquals("a_method", result.method());
        assertEquals("normal string", result.arguments().get(0));
        assertEquals("42", result.arguments().get(1));
        assertEquals("{ \"key\": \"value\" }", result.arguments().get(2));
    }


    @Ignore
    @Test
    public void shouldReadParameterNames() throws Exception {

    }


    @Test
    public void shouldOutputAString() throws Exception {
        assertEquals("\"value\"", gson.toJson("value"));
    }


}
