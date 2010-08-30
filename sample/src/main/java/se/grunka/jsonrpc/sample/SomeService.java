package se.grunka.jsonrpc.sample;

import se.grunka.jsonrpc.Service;

@Service
public interface SomeService {
    String sayHello(String to);

    String sayHello(String to, String also);
}
