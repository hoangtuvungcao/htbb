package game.model;

public class Position {
    public int anchor;
    public byte follow;
    public short indexColor;
    public int x;
    public int y;
    public byte count = 0;
    public byte dir = 1;
    public short index = -1;
    public int id = -1000;

    public Position(int i, int i2) {
        this.x = i;
        this.y = i2;
    }

    public Position(int i, int i2, int i3) {
        this.x = i;
        this.y = i2;
        this.follow = (byte) i3;
    }

    public Position() {
    }
}
