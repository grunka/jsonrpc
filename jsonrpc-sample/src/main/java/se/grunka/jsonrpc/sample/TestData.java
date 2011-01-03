package se.grunka.jsonrpc.sample;

public class TestData {
    private final int i1;
    private final int i2;
    private final int i3;
    private final int i4;
    private final int i5;
    private final String str;

    @SuppressWarnings("unused")
    public TestData() {
        i1 = i2 = i3 = i4 = i5 = 0;
        str = null;
    }

    public TestData(int i1, int i2, int i3, int i4, int i5, String str) {
        this.i1 = i1;
        this.i2 = i2;
        this.i3 = i3;
        this.i4 = i4;
        this.i5 = i5;
        this.str = str;
    }


    public int i1() {
        return i1;
    }


    public int i2() {
        return i2;
    }


    public int i3() {
        return i3;
    }


    public int i4() {
        return i4;
    }


    public int i5() {
        return i5;
    }


    public String str() {
        return str;
    }


    @Override
    public String toString() {
        return "TestData{" +
                "i1=" + i1 +
                ", i2=" + i2 +
                ", i3=" + i3 +
                ", i4=" + i4 +
                ", i5=" + i5 +
                ", str='" + str + '\'' +
                '}';
    }
}
