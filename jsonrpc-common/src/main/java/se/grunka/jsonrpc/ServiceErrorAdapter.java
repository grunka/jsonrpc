package se.grunka.jsonrpc;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

class ServiceErrorAdapter implements JsonDeserializer<ServiceError>, JsonSerializer<ServiceError> {
    @SuppressWarnings("unchecked")
    @Override
    public ServiceError deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        Class<? extends Exception> type;
        String typeName = object.get("type").getAsString();
        try {
            type = (Class<? extends Exception>) Class.forName(typeName);
        } catch (ClassCastException e) {
            throw new JsonParseException(typeName + " is not an exception type", e);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Could not instantiate the exception class", e);
        }
        JsonElement messageElement = object.get("message");
        String message = messageElement != null ? messageElement.getAsString() : null;
        return new ServiceError(type, message);
    }


    @Override
    public JsonElement serialize(ServiceError src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", src.getType().getName());
        object.addProperty("message", src.getMessage());
        return object;
    }
}

