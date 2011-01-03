package se.grunka.jsonrpc;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class ServiceMethodParser implements JsonDeserializer<ServiceMethod> {
    private final Object serviceInstance;
    private final MethodInformation methodInformation;


    public ServiceMethodParser(MethodInformation methodInformation, Object serviceInstance) {
        this.methodInformation = methodInformation;
        this.serviceInstance = serviceInstance;
    }


    @Override
    public ServiceMethod deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = object.entrySet();
        Map<String, Object> namedArguments = new HashMap<String, Object>();
        for (Map.Entry<String, JsonElement> entry : entries) {
            String name = entry.getKey();
            Class<?> type = methodInformation.getType(name);
            namedArguments.put(name, context.deserialize(entry.getValue(), type));
        }
        MethodWithNames method = methodInformation.getMethodWithNames(namedArguments.keySet());
        String[] parameterNames = method.getParameterNames();
        Object[] arguments = new Object[parameterNames.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = namedArguments.get(parameterNames[i]);
        }
        return new ServiceMethod(serviceInstance, method.getMethod(), arguments);
    }
}
