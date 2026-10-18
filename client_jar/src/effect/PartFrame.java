package effect;

public class PartFrame {
    public short dx;
    public short dy;
    public byte idSmallImg;
    public byte flip;
    public byte onTop;
    public byte[] data;
    public short id;

    public PartFrame(short dx, short dy, byte idSmall) {
        this.idSmallImg = idSmall;
        this.dx = dx;
        this.dy = dy;
    }

    public PartFrame(int id, byte[] data) {
        this.id = (short) id;
        this.data = data;
    }
}
