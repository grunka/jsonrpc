package se.grunka.jsonrpc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class MethodInformation {
    private final Map<String, Class<?>> typeLookup = new HashMap<String, Class<?>>();
    private final Map<Set<String>, MethodWithNames> methodWithNamesLookup = new HashMap<Set<String>, MethodWithNames>();


    public Class<?> getType(String name) {
        return typeLookup.get(name);
    }

    public MethodWithNames getMethodWithNames(final Set<String> names) {
        //TODO maybe optimize for the case where there is only one entry
        return methodWithNamesLookup.get(names);
    }

    public MethodInformation merge(final MethodInformation other) {
        MethodInformation result = new MethodInformation();
        result.typeLookup.putAll(this.typeLookup);
        for (Map.Entry<String, Class<?>> entry : other.typeLookup.entrySet()) {
            Class<?> existingType = result.typeLookup.get(entry.getKey());
            if (existingType != null && !existingType.equals(entry.getValue())) {
                throw new Error("Duplicate parameter name found '" + entry.getKey() + "'");
            }
        }
        result.typeLookup.putAll(other.typeLookup);
        result.methodWithNamesLookup.putAll(this.methodWithNamesLookup);
        for (Map.Entry<Set<String>, MethodWithNames> entry : other.methodWithNamesLookup.entrySet()) {
            MethodWithNames existingMethod = result.methodWithNamesLookup.get(entry.getKey());
            MethodWithNames method = entry.getValue();
            if (existingMethod != null && !existingMethod.equals(method)) {
                throw new Error("Duplicate method found " + method.getMethod().getName() + " with the parameters " + Arrays.toString(method.getParameterNames()));
            }
        }
        result.methodWithNamesLookup.putAll(other.methodWithNamesLookup);
        return result;
    }

    public static MethodInformation create(Method method) {
        MethodInformation result = new MethodInformation();
        Class<?>[] types = method.getParameterTypes();
        String[] names = parameterNames(method);
        result.methodWithNamesLookup.put(new HashSet<String>(Arrays.asList(names)), new MethodWithNames(method, names));
        for (int i = 0; i < types.length; i++) {
            if (result.typeLookup.get(names[i]) != null) {
                throw new Error("Duplicate parameter name found '" + names[i] + "' in " + method.getName());
            }
            result.typeLookup.put(names[i], types[i]);
        }
        return result;
    }

    private static String[] parameterNames(Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        String[] names = new String[annotations.length];
        for (int i = 0; i < annotations.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Name) {
                    names[i] = ((Name) annotation).value();
                    break;
                }
            }
            if (names[i] == null) {
                throw new IllegalArgumentException("The arguments of " + method.getName() + " are not annotated properly");
            }
        }
        return names;
    }

    private MethodInformation(){}
}
