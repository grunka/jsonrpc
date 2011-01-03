package se.grunka.jsonrpc.sample;

public class TestService implements TestInterface {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }


    @Override
    public TestData doThings(TestData input) {
        return new TestData(
                input.i5(),
                input.i5() + input.i4(),
                input.i5() + input.i4() + input.i3(),
                input.i5() + input.i4() + input.i3() + input.i2(),
                input.i5() + input.i4() + input.i3() + input.i2() + input.i1(),
                input.str().substring(1) + input.str().substring(0, 1)
        );
    }


    @Override
    public void unsupportedOperation() {
        throw new UnsupportedOperationException("message");
    }


    @Override
    public void somethingException() throws SomethingException {
        throw new SomethingException("something message");
    }
}
