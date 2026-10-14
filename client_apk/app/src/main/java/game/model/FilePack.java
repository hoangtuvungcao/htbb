package game.model;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import com.teamobi.kpah.GameMidlet;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilePack {
    public static final String arrow = "/arrow.sh";
    public static final String cBody = "/body.c";
    public static final String cHat = "/hat.c";
    public static final String cHead = "/head.c";
    public static final String cLeg = "/leg.c";
    public static final String[] charAvatar = {"/c_leg_", "/c_body_", "/c_head_", "/c_hat_", "/c_coat_"};
    public static final String eff = "/eff.sh";
    public static final String effPublic = "/skillpublic.sh";
    public static final String font = "/font.sh";
    public static final String ground = "/g.sh";
    public static FilePack instance = null;
    public static final String main = "/main.sh";
    public static final String nation = "/nation";
    public static final String npc = "/npc.sh";
    public static final String tree = "/tree/";
    public static final String type = "/type.sh";
    public static final String wps = "/wpsplash_";
    private byte[] code;
    private int codeLen;
    private DataInputStream file;
    private int[] flen;
    private String[] fname;
    private int[] fpos;
    private byte[] fullData;
    private int hSize;
    private int nFile;
    private String name;

    private int encode(int i) {
        return i;
    }

    public static void reset() {
        FilePack filePack = instance;
        if (filePack != null) {
            filePack.close();
        }
        instance = null;
        System.gc();
    }

    public FilePack(String str, byte[] bArr) {
        byte[] bArr2 = {78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};
        this.code = bArr2;
        this.codeLen = bArr2.length;
        this.name = str;
        this.hSize = 0;
        if (bArr == null) {
            open();
        } else {
            openbyArray(bArr);
        }
        DataInputStream dataInputStream = this.file;
        if (dataInputStream == null) {
            instance = null;
            return;
        }
        try {
            int iEncode = encode(dataInputStream.readUnsignedByte());
            this.nFile = iEncode;
            this.hSize++;
            this.fname = new String[iEncode];
            this.fpos = new int[iEncode];
            this.flen = new int[iEncode];
            int i = 0;
            int i2 = 0;
            for (int i3 = 0; i3 < this.nFile; i3++) {
                int iEncode2 = encode(this.file.readByte());
                byte[] bArr3 = new byte[iEncode2];
                this.file.read(bArr3);
                encode(bArr3);
                this.fname[i3] = new String(bArr3);
                this.fpos[i3] = i2;
                this.flen[i3] = encode(this.file.readUnsignedShort());
                int i4 = this.flen[i3];
                i2 += i4;
                i += i4;
                this.hSize += iEncode2 + 3;
            }
            byte[] bArr4 = new byte[i];
            this.fullData = bArr4;
            this.file.readFully(bArr4);
            encode(this.fullData);
        } catch (Exception unused) {
            Cout.println("Error in fileback constructor");
        }
        close();
    }

    public FilePack(byte[] bArr) {
        byte[] bArr2 = {78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};
        this.code = bArr2;
        this.codeLen = bArr2.length;
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
        this.file = dataInputStream;
        this.hSize = 0;
        try {
            int iEncode = encode(dataInputStream.readUnsignedByte());
            this.nFile = iEncode;
            this.hSize++;
            this.fname = new String[iEncode];
            this.fpos = new int[iEncode];
            this.flen = new int[iEncode];
            int i = 0;
            int i2 = 0;
            for (int i3 = 0; i3 < this.nFile; i3++) {
                int iEncode2 = encode(this.file.readByte());
                byte[] bArr3 = new byte[iEncode2];
                this.file.read(bArr3);
                encode(bArr3);
                this.fname[i3] = new String(bArr3);
                this.fpos[i3] = i2;
                this.flen[i3] = encode(this.file.readUnsignedShort());
                int i4 = this.flen[i3];
                i2 += i4;
                i += i4;
                this.hSize += iEncode2 + 3;
            }
            byte[] bArr4 = new byte[i];
            this.fullData = bArr4;
            this.file.readFully(bArr4);
            encode(this.fullData);
        } catch (IOException unused) {
            Cout.println("loi filepack");
        }
        close();
    }

    public FilePack(String str) {
        byte[] bArr = {78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};
        this.code = bArr;
        this.codeLen = bArr.length;
        this.name = str;
        this.hSize = 0;
        open();
        try {
            int iEncode = encode(this.file.readUnsignedByte());
            this.nFile = iEncode;
            this.hSize++;
            this.fname = new String[iEncode];
            this.fpos = new int[iEncode];
            this.flen = new int[iEncode];
            int i = 0;
            int i2 = 0;
            for (int i3 = 0; i3 < this.nFile; i3++) {
                int iEncode2 = encode(this.file.readByte());
                byte[] bArr2 = new byte[iEncode2];
                this.file.read(bArr2);
                encode(bArr2);
                this.fname[i3] = new String(bArr2);
                this.fpos[i3] = i2;
                this.flen[i3] = encode(this.file.readUnsignedShort());
                int i4 = this.flen[i3];
                i2 += i4;
                i += i4;
                this.hSize += iEncode2 + 3;
            }
            byte[] bArr3 = new byte[i];
            this.fullData = bArr3;
            this.file.readFully(bArr3);
            encode(this.fullData);
        } catch (IOException unused) {
            Cout.println("loi ne");
        }
        close();
    }

    public FilePack(String str, AssetManager assetManager, String str2) {
        byte[] bArr = {78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};
        this.code = bArr;
        this.codeLen = bArr.length;
        this.name = str;
        this.hSize = 0;
        openFromAssit(str);
        try {
            int iEncode = encode(this.file.readUnsignedByte());
            this.nFile = iEncode;
            this.hSize++;
            this.fname = new String[iEncode];
            this.fpos = new int[iEncode];
            this.flen = new int[iEncode];
            int i = 0;
            int i2 = 0;
            for (int i3 = 0; i3 < this.nFile; i3++) {
                int iEncode2 = encode(this.file.readByte());
                byte[] bArr2 = new byte[iEncode2];
                this.file.read(bArr2);
                encode(bArr2);
                this.fname[i3] = new String(bArr2);
                this.fpos[i3] = i2;
                this.flen[i3] = encode(this.file.readUnsignedShort());
                int i4 = this.flen[i3];
                i2 += i4;
                i += i4;
                this.hSize += iEncode2 + 3;
            }
            byte[] bArr3 = new byte[i];
            this.fullData = bArr3;
            this.file.readFully(bArr3);
            encode(this.fullData);
        } catch (IOException unused) {
            Cout.println("loi ne Asset");
        }
        close();
    }

    public static Bitmap getImg(String str) {
        return instance.loadImage(str + ".png");
    }

    public static void init(String str) {
        instance = null;
        instance = new FilePack(str);
    }

    public static void initAsset(String str) {
        instance = null;
        instance = new FilePack(str, null, null);
    }

    private void encode(byte[] bArr) {
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            bArr[i] = (byte) (bArr[i] ^ this.code[i % this.codeLen]);
        }
    }

    public static String getLink(String str) {
        return str.charAt(0) == '/' ? str.substring(1) : str;
    }

    private void open() {
        try {
            this.file = new DataInputStream(GameMidlet.asset.open(getLink(this.name)));
        } catch (Exception unused) {
        }
    }

    private void openFromAssit(String str) {
        try {
            this.file = new DataInputStream(GameMidlet.asset.open(str));
        } catch (Exception e) {
            Cout.println("Loi Load Ass FilePack " + e.toString());
        }
    }

    void close() {
        try {
            DataInputStream dataInputStream = this.file;
            if (dataInputStream != null) {
                dataInputStream.close();
            }
        } catch (IOException unused) {
            Cout.println("Loi close file pack");
        }
    }

    public byte[] loadFile(String str) throws Exception {
        for (int i = 0; i < this.nFile; i++) {
            if (this.fname[i].compareTo(str) == 0) {
                int i2 = this.flen[i];
                byte[] bArr = new byte[i2];
                System.arraycopy(this.fullData, this.fpos[i], bArr, 0, i2);
                return bArr;
            }
        }
        throw new Exception("File '" + str + "' not found!");
    }

    public Bitmap loadImage(String str) {
        for (int i = 0; i < this.nFile; i++) {
            if (this.fname[i].compareTo(str) == 0) {
                return new BitmapDrawable(new ByteArrayInputStream(this.fullData, this.fpos[i], this.flen[i])).getBitmap();
            }
        }
        return null;
    }

    private void openbyArray(byte[] bArr) {
        this.file = new DataInputStream(new ByteArrayInputStream(bArr));
    }

    public InputStream loadData(String str) {
        for (int i = 0; i < this.nFile; i++) {
            if (this.fname[i].compareTo(str) == 0) {
                int i2 = this.flen[i];
                byte[] bArr = new byte[i2];
                System.arraycopy(this.fullData, this.fpos[i], bArr, 0, i2);
                return new ByteArrayInputStream(bArr);
            }
        }
        return null;
    }

    public byte[] getBinaryFile(String str) {
        for (int i = 0; i < this.nFile; i++) {
            if (this.fname[i].compareTo(str) == 0) {
                int i2 = this.flen[i];
                byte[] bArr = new byte[i2];
                System.arraycopy(this.fullData, this.fpos[i], bArr, 0, i2);
                return bArr;
            }
        }
        Cout.println("File '" + str + "' not found!");
        return null;
    }

    public static void initByArray(byte[] bArr) {
        instance = new FilePack("", bArr);
    }
}
