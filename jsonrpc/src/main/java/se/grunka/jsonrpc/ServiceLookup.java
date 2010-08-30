package se.grunka.jsonrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ServiceLookup {
    @SuppressWarnings("unchecked")
    public static Map<String, Map<Class<?>[], String[]>> getParameterNamesMapping(Class<?> type) {
        String className = type.getCanonicalName() + "ParameterNames";
        try {
            Class<?> typeParameterNames = Class.forName(className);
            Method getMappingMethod = typeParameterNames.getDeclaredMethod("getMapping");
            return (Map<String, Map<Class<?>[], String[]>>) getMappingMethod.invoke(null);
        } catch (ClassNotFoundException e) {
            throw new Error("Could not find the class " + className + " that should have been generated", e);
        } catch (NoSuchMethodException e) {
            throw new Error("The generated class " + className + " does not contain the method getMapping", e);
        } catch (SecurityException e) {
            throw new Error("Was not allowed to lookup getMapping on the class " + className, e);
        } catch (IllegalAccessException e) {
            throw new Error("Was not allowed to call getMapping on the class " + className, e);
        } catch (IllegalArgumentException e) {
            throw new Error("getMapping in class " + className + " did not have the expected signature", e);
        } catch (InvocationTargetException e) {
            throw new Error("getMapping in class " + className + " threw an exception", e);
        }
    }
}
