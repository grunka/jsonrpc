package se.grunka.jsonrpc.sample;

import se.grunka.jsonrpc.Service;

@Service
public interface HelloWorldService {
    String sayHello(String to);

    String sayHello(String to, String also);
}
