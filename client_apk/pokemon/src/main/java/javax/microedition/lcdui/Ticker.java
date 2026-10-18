package javax.microedition.lcdui;

public class Ticker {
    private String s;

    public Ticker(String s) {
        this.s = s;
    }

    public String getString() {
        return s;
    }

    public void setString(String s) {
        this.s = s;
    }
}
