package se.grunka.jsonrpc;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ServiceRequestTypeAdapter implements JsonSerializer<ServiceRequest>, JsonDeserializer<ServiceRequest> {
    private final TypeLookup typeLookup;

    public ServiceRequestTypeAdapter(TypeLookup typeLookup) {
        this.typeLookup = typeLookup;
    }

    @Override
    public ServiceRequest deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonElement methodElement = ((JsonObject) jsonElement).get("method");
        String methodName = methodElement.getAsString();
        JsonObject argumentsElement = (JsonObject) ((JsonObject) jsonElement).get("arguments");
        Map<String, Object> arguments = new HashMap<String, Object>();
        if (argumentsElement != null) {
            for (Map.Entry<String, JsonElement> argumentEntry : argumentsElement.entrySet()) {
                String argumentName = argumentEntry.getKey();
                Type argumentType = typeLookup.lookup(methodName, argumentName);
                Object value = jsonDeserializationContext.deserialize(argumentEntry.getValue(), argumentType);
                arguments.put(argumentName, value);
            }
        }
        return new ServiceRequest(methodName, arguments);
    }

    @Override
    public JsonElement serialize(ServiceRequest serviceRequest, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("method", new JsonPrimitive(serviceRequest.method()));
        if (!serviceRequest.arguments().isEmpty()) {
            JsonObject arguments = new JsonObject();
            for (Map.Entry<String, Object> argumentEntry : serviceRequest.arguments().entrySet()) {
                String name = argumentEntry.getKey();
                Object value = argumentEntry.getValue();
                arguments.add(name, jsonSerializationContext.serialize(value));
            }
            jsonObject.add("arguments", arguments);
        }
        return jsonObject;
    }
}
