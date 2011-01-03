package se.grunka.jsonrpc.sample;

import se.grunka.jsonrpc.Name;

public interface TestInterface {

    public class SomethingException extends Exception {
        public SomethingException(String message) {
            super(message);
        }
    }

    String sayHello(@Name("to") String name);
    @Name("things") TestData doThings(@Name("input") TestData input);
    void unsupportedOperation();
    void somethingException() throws SomethingException;
}
