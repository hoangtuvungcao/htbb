package game.model;

import android.graphics.Bitmap;
import com.htbb.client.GameMidlet;
import effect.DataEffect;
import game.core.j2me.Graphics;
import game.networklogic.GameService;
import game.render.Res;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Vector;

public class MonsterTemplate {
    public static Hashtable ALL_DATE_NEW_MONSTER = new Hashtable();
    byte[] arrayDataMonsterNew;
    public Vector dataMonsterNew;
    public byte h;
    public byte he;
    byte height;
    public Bitmap image;
    public ImageObj imageObj;
    public boolean isLoad;
    public byte isNewMonster;
    public int maxhp;
    byte moveType;
    public String name;
    public byte palate;
    public byte spalate;
    byte speed;
    public long timePaint;
    public int type;
    public byte w;
    public byte xCenter;
    public byte xadd;
    public byte yCenter;
    public byte yadd;

    public MonsterTemplate() {
        this.type = -1;
        this.speed = (byte) 1;
        this.isNewMonster = (byte) 0;
        this.isLoad = false;
        this.dataMonsterNew = new Vector();
        this.arrayDataMonsterNew = null;
    }

    public MonsterTemplate(int i, String str, String str2, byte b, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8, byte b9) {
        this.type = -1;
        this.speed = (byte) 1;
        this.isNewMonster = (byte) 0;
        this.isLoad = false;
        this.dataMonsterNew = new Vector();
        this.arrayDataMonsterNew = null;
        this.type = i;
        this.name = str;
        this.moveType = b;
        this.speed = b2;
        this.height = b3;
        this.w = b4;
        this.h = b5;
        this.xCenter = b6;
        this.yCenter = b7;
        this.xadd = b8;
        this.yadd = b9;
    }

    public void setInfo(int i, String str, String str2, byte b, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8, byte b9) {
        this.type = i;
        this.name = str;
        this.moveType = b;
        this.speed = b2;
        this.height = b3;
        this.w = b4;
        this.h = b5;
        this.xCenter = b6;
        this.yCenter = b7;
        this.xadd = b8;
        this.yadd = b9;
    }

    public void paint(Graphics graphics, int i, int i2, int i3, int i4, int i5) {
        Bitmap bitmap = this.image;
        if (bitmap != null) {
            try {
                byte b = this.h;
                graphics.drawRegion(bitmap, 0, i5 * b, this.w, b, i3, i - this.xCenter, i2 - this.yCenter, i4);
            } catch (Exception unused) {
            }
        }
        this.timePaint = System.currentTimeMillis();
    }

    public void paint(Graphics graphics) {
        Bitmap bitmap = this.image;
        if (bitmap != null) {
            graphics.drawImage(bitmap, 10, 10, 0);
        }
    }

    public void resetImage() {
        if (System.currentTimeMillis() - this.timePaint > 60000) {
            this.image = null;
            this.imageObj = null;
            this.dataMonsterNew.removeAllElements();
            ALL_DATE_NEW_MONSTER.remove("" + ((int) this.spalate));
        }
    }

    public Vector getDataMonster() {
        try {
            if (this.type > -1) {
                Vector vector = (Vector) ALL_DATE_NEW_MONSTER.get("" + ((int) this.spalate));
                if (vector != null) {
                    this.dataMonsterNew = vector;
                } else {
                    DataInputStream dataInputStream = new DataInputStream(GameMidlet.asset.open("boss/" + ((int) this.spalate)));
                    byte b = dataInputStream.readByte();
                    for (int i = 0; i < b; i++) {
                        int i2 = dataInputStream.readShort();
                        byte[] bArr = new byte[i2];
                        dataInputStream.read(bArr, 0, i2);
                        this.dataMonsterNew.addElement(new DataEffect(bArr));
                    }
                    ALL_DATE_NEW_MONSTER.put("" + ((int) this.spalate), this.dataMonsterNew);
                }
            }
            this.timePaint = System.currentTimeMillis();
        } catch (Exception e) {
            System.out.println("bi loi tai getDataMonster " + e.toString());
        }
        return this.dataMonsterNew;
    }

    public void loadImage() {
        if (this.isLoad || this.type <= -1) {
            return;
        }
        GameService.gI().doRequestImageMonster(this.type, this.palate, this.spalate);
        this.isLoad = true;
    }

    public void onImage(boolean z, byte[] bArr) {
        try {
            byte b = this.palate;
            if ((b >= 70 && b <= 88) || b == 90 || b == 91 || b == 92 || this.isNewMonster == 1) {
                this.imageObj = new ImageObj(bArr);
            } else {
                FilePack.initByArray(bArr);
                byte b2 = this.palate;
                if (b2 == 39 || b2 == 40) {
                    this.imageObj = new ImageObj(this.palate, this.spalate);
                } else {
                    this.image = Res.createImgByHeader(FilePack.instance.loadFile(((int) this.spalate) + "_h"), FilePack.instance.loadFile("data"));
                }
                FilePack.reset();
            }
        } catch (Exception unused) {
        }
        this.isLoad = false;
    }

    public Bitmap getImage(int i) {
        ImageObj imageObj = this.imageObj;
        if (imageObj == null) {
            return null;
        }
        return imageObj.getImage(i);
    }

    public void doSetDataMonsterNew(byte[] bArr) {
        try {
            this.arrayDataMonsterNew = bArr;
            Vector vector = (Vector) ALL_DATE_NEW_MONSTER.get("" + ((int) this.spalate));
            if (vector != null) {
                this.dataMonsterNew = vector;
                return;
            }
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
            byte b = dataInputStream.readByte();
            for (int i = 0; i < b; i++) {
                int i2 = dataInputStream.readShort();
                byte[] bArr2 = new byte[i2];
                dataInputStream.read(bArr2, 0, i2);
                this.dataMonsterNew.addElement(new DataEffect(bArr2));
            }
            ALL_DATE_NEW_MONSTER.put("" + ((int) this.spalate), this.dataMonsterNew);
            dataInputStream.close();
        } catch (Exception unused) {
        }
    }
}
