package se.grunka.jsonrpc;

import java.lang.reflect.Type;

public interface TypeLookup {
    Type lookup(String methodName, String argumentName);
}
