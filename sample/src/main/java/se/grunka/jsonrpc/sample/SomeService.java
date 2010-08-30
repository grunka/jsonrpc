package se.grunka.jsonrpc.sample;

import se.grunka.jsonrpc.ServiceDefinition;

@ServiceDefinition
public interface SomeService {
    String sayHello(String to);

    String sayHello(String to, String also);
}
