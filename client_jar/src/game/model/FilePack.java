package game.model;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

public class FilePack {
    public static FilePack instance;
    public String[] fname;
    private int[] fpos;
    private int[] flen;
    private byte[] fullData;
    private int nFile;
    private int hSize;
    private String name;
    private byte[] code;
    private int codeLen;
    public static final String[] charAvatar = {"/c/leg/", "/c/body/", "/c/head/", "/c/hat/", "/c/coat/"};
    public static final String cBody = "/body.c";
    public static final String cHat = "/hat.c";
    public static final String cLeg = "/leg.c";
    public static final String cHead = "/head.c";
    public static final String main = "/main.sh";
    public static final String font = "/font.sh";
    public static final String npc = "/npc.sh";
    public static final String eff = "/eff.sh";
    public static final String tree = "/tree/";
    public static final String type = "/type.sh";
    public static final String wps = "/wpsplash/";
    public static final String arrow = "/arrow.sh";
    public static final String effPublic = "/skillpublic.sh";
    public static final String ground = "/g.sh";
    public static final String nation = "/nation";
    public static final String box = "/box.sh";
    private DataInputStream file;

    public FilePack() {
        this.code = new byte[]{78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};
        this.codeLen = this.code.length;
    }

    public static void reset() {
        if (instance != null) {
            instance.close();
        }
        instance = null;
        System.gc();
    }

    public FilePack(String name, byte[] array) {
        this.code = new byte[]{78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};
        this.codeLen = this.code.length;
        int pos = 0;
        int size = 0;
        this.name = name;
        this.hSize = 0;
        if (array == null) {
            open();
        } else {
            openbyArray(array);
        }
        if (this.file == null) {
            instance = null;
            return;
        }
        try {
            this.nFile = encode(this.file.readUnsignedByte());
            this.hSize++;
            this.fname = new String[this.nFile];
            this.fpos = new int[this.nFile];
            this.flen = new int[this.nFile];
            for (int i = 0; i < this.nFile; i++) {
                int lname = encode(this.file.readByte());
                byte[] data = new byte[lname];
                this.file.read(data);
                encode(data);
                this.fname[i] = new String(data);
                this.fpos[i] = pos;
                this.flen[i] = encode(this.file.readUnsignedShort());
                pos += this.flen[i];
                size += this.flen[i];
                this.hSize += lname + 3;
            }
            this.fullData = new byte[size];
            this.file.readFully(this.fullData);
            encode(this.fullData);
        } catch (Exception e) {
            Cout.println(new StringBuffer(String.valueOf(name)).append(" Error in fileback constructor > ").append(e.toString()).toString());
        }
        close();
    }

    public static Image getImg(String path) {
        return instance.loadImage(new StringBuffer(String.valueOf(path)).append(".png").toString());
    }

    public static void init(String path) {
        instance = new FilePack(path, null);
    }

    public static void initByArray(byte[] array) {
        instance = new FilePack("", array);
    }

    private int encode(int i) {
        return i;
    }

    private void encode(byte[] bytes) {
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) (bytes[i] ^ this.code[i % this.codeLen]);
        }
    }

    private void open() {
        this.file = new DataInputStream(getClass().getResourceAsStream(this.name));
    }

    private void openbyArray(byte[] array) {
        this.file = new DataInputStream(new ByteArrayInputStream(array));
    }

    public void close() {
        try {
            if (this.file != null) {
                this.file.close();
            }
        } catch (IOException e) {
        }
    }

    public byte[] loadFile(String fileName) throws Exception {
        for (int i = 0; i < this.nFile; i++) {
            if (this.fname[i].compareTo(fileName) == 0) {
                byte[] bytes = new byte[this.flen[i]];
                System.arraycopy(this.fullData, this.fpos[i], bytes, 0, this.flen[i]);
                return bytes;
            }
        }
        throw new Exception(new StringBuffer("File '").append(fileName).append("' not found!").toString());
    }

    public Image loadImage(String fileName) {
        for (int i = 0; i < this.nFile; i++) {
            if (this.fname[i].compareTo(fileName) == 0) {
                return Image.createImage(this.fullData, this.fpos[i], this.flen[i]);
            }
        }
        return null;
    }

    public InputStream loadData(String name) {
        for (int i = 0; i < this.nFile; i++) {
            if (this.fname[i].compareTo(name) == 0) {
                byte[] aa = new byte[this.flen[i]];
                System.arraycopy(this.fullData, this.fpos[i], aa, 0, this.flen[i]);
                return new ByteArrayInputStream(aa);
            }
        }
        return null;
    }

    public byte[] getBinaryFile(String name) {
        for (int i = 0; i < this.nFile; i++) {
            if (this.fname[i].compareTo(name) == 0) {
                byte[] array = new byte[this.flen[i]];
                System.arraycopy(this.fullData, this.fpos[i], array, 0, this.flen[i]);
                return array;
            }
        }
        Cout.println(new StringBuffer("File '").append(name).append("' not found!").toString());
        return null;
    }

    public Player createPlayer(String fileName, String type2) throws Exception {
        for (int i = 0; i < this.nFile; i++) {
            if (this.fname[i].compareTo(fileName) == 0) {
                return Manager.createPlayer(new ByteArrayInputStream(this.fullData, this.fpos[i], this.flen[i]), type2);
            }
        }
        throw new Exception(new StringBuffer("File '").append(fileName).append("' not found!").toString());
    }
}
