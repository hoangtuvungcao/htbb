package effect;

public class PartFrame {
    public byte[] data;
    public short dx;
    public short dy;
    public byte flip;
    public short id;
    public byte idSmallImg;
    public byte onTop;

    public PartFrame(short s, short s2, byte b) {
        this.idSmallImg = b;
        this.dx = s;
        this.dy = s2;
    }

    public PartFrame(int i, byte[] bArr) {
        this.id = (short) i;
        this.data = bArr;
    }
}
