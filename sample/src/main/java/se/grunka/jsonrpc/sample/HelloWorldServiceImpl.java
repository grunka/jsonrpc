package se.grunka.jsonrpc.sample;

public class HelloWorldServiceImpl implements HelloWorldService {
    @Override
    public String sayHello(String to) {
        return "Hello " + to + "!";
    }

    @Override
    public String sayHello(String to, String also) {
        return "Hello " + to + " and " + also + "!";
    }
}
