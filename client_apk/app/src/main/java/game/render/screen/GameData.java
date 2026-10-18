package game.render.screen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import game.core.j2me.Graphics;
import game.core.j2me.Image;
import game.model.FrameImage;
import game.model.ImageIcon;
import game.networklogic.GameService;
import game.render.GCanvas;
import game.render.Res;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class GameData {
    public static FrameImage imgSkillIcon;
    public static Bitmap imgWait;
    static Bitmap imgskill;
    private static GameData me;
    private boolean loadData = false;
    public static Vector imgHorse = new Vector();
    public static Hashtable listImgIcon = new Hashtable();
    public static byte idWait = 0;

    public static GameData gI() {
        GameData gameData = me;
        if (gameData != null) {
            return gameData;
        }
        GameData gameData2 = new GameData();
        me = gameData2;
        return gameData2;
    }

    static {
        try {
            if (imgWait == null) {
                imgWait = Image.loadImageFromAsset("waiting.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getImage() {
        if (this.loadData) {
            return;
        }
        loadImgPotion();
        loadImgGemItem();
        this.loadData = true;
    }

    public static void loadImgPotion() {
        try {
            RMS.deleteRMS("nqshImgPotion");
        } catch (Exception unused) {
        }
        byte[] bArrLoadRMS = RMS.loadRMS("nqshImgPotionNew");
        byte b = 0;
        if (bArrLoadRMS != null) {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArrLoadRMS));
            try {
                dataInputStream.read(new byte[dataInputStream.readShort()]);
                byte b2 = dataInputStream.readByte();
                for (int i = 0; i < b2; i++) {
                    byte[] bArr = new byte[dataInputStream.readShort()];
                    dataInputStream.read(bArr);
                    int i2 = dataInputStream.readByte();
                    byte[][] bArr2 = new byte[i2][];
                    for (int i3 = 0; i3 < i2; i3++) {
                        byte[] bArr3 = new byte[dataInputStream.readShort()];
                        bArr2[i3] = bArr3;
                        dataInputStream.read(bArr3);
                    }
                    createImgHorse(bArr, bArr2);
                }
                b = dataInputStream.readByte();
                dataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GameService.gI().doGetImage((byte) 2, b);
    }

    private static void createImgHorse(byte[] bArr, byte[][] bArr2) {
        for (byte[] bArr3 : bArr2) {
            imgHorse.addElement(Res.createImgByHeader(bArr3, bArr));
        }
    }

    public static void saveImgPotion(byte b, byte[] bArr, byte[] bArr2, byte[][] bArr3, byte[][][] bArr4) {
        byte[][] bArr5;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeShort(bArr.length);
            dataOutputStream.write(bArr);
            dataOutputStream.writeByte(bArr3.length);
            for (int i = 0; i < bArr3.length; i++) {
                dataOutputStream.writeShort(bArr3[i].length);
                dataOutputStream.write(bArr3[i]);
                dataOutputStream.writeByte(bArr4[i].length);
                int i2 = 0;
                while (true) {
                    bArr5 = bArr4[i];
                    if (i2 < bArr5.length) {
                        dataOutputStream.writeShort(bArr5[i2].length);
                        dataOutputStream.write(bArr4[i][i2]);
                        i2++;
                    } else {
                        break;
                    }
                }
                createImgHorse(bArr3[i], bArr5);
            }
            dataOutputStream.writeByte(b);
            RMS.saveRMS("nqshImgPotionNew", byteArrayOutputStream.toByteArray());
            dataOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveImgSkill(int i, byte[] bArr) {
        imgskill = BitmapFactory.decodeByteArray(bArr, 0, bArr.length);
        imgSkillIcon = new FrameImage(imgskill, 20, 20);
    }

    public static void saveImgGem(byte b, byte[] bArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeByte(b);
            dataOutputStream.writeShort(bArr.length);
            dataOutputStream.write(bArr);
            RMS.saveRMS("gemItem", byteArrayOutputStream.toByteArray());
            dataOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadImgGemItem() {
        byte[] bArrLoadRMS = RMS.loadRMS("gemItem");
        byte b = 0;
        if (bArrLoadRMS != null) {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArrLoadRMS));
            try {
                b = dataInputStream.readByte();
                dataInputStream.read(new byte[dataInputStream.readShort()]);
                dataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GameService.gI().doGetImage((byte) 3, b);
    }

    public static void setIndexWait() {
        if (GCanvas.gameTick % 5 == 0) {
            idWait = (byte) ((idWait + 1) % (imgWait.getHeight() / 18));
        }
    }

    public static void paintIcon(Graphics graphics, short s, int i, int i2) {
        int i3;
        int i4;
        ImageIcon imgIcon = getImgIcon(s);
        if (imgIcon != null) {
            if (!imgIcon.isLoad) {
                int width = imgIcon.img.getWidth();
                int height = imgIcon.img.getHeight();
                if (height > 25) {
                    i4 = 3;
                    i3 = height / 3;
                } else {
                    i3 = height;
                    i4 = 1;
                }
                if (GCanvas.gameTick % 5 == 0) {
                    imgIcon.indexFrame = (byte) ((imgIcon.indexFrame + 1) % i4);
                }
                graphics.drawRegion(imgIcon.img, 0, imgIcon.indexFrame * i3, width, i3, 0, i, i2, 3);
                return;
            }
            graphics.drawRegion(imgWait, 0, idWait * 18, 18, 18, 0, i, i2, 3);
            setIndexWait();
        }
    }

    public static void paintIconCayThan(Graphics graphics, short s, int i, int i2) {
        ImageIcon imgIcon = getImgIcon(s);
        if (imgIcon != null) {
            if (!imgIcon.isLoad) {
                graphics.drawImage(imgIcon.img, i, i2, 3);
            } else {
                graphics.drawRegion(imgWait, 0, idWait * 18, 18, 18, 0, i, i2, 3);
                setIndexWait();
            }
        }
    }

    public static void paintIcon(Graphics graphics, short s, int i, int i2, int i3) {
        int i4;
        int i5;
        ImageIcon imgIcon = getImgIcon(s);
        if (imgIcon != null) {
            if (!imgIcon.isLoad) {
                int width = imgIcon.img.getWidth();
                int height = imgIcon.img.getHeight();
                if (height > 25) {
                    i5 = 3;
                    i4 = height / 3;
                } else {
                    i4 = height;
                    i5 = 1;
                }
                if (GCanvas.gameTick % 5 == 0) {
                    imgIcon.indexFrame = (byte) ((imgIcon.indexFrame + 1) % i5);
                }
                graphics.drawRegion(imgIcon.img, 0, imgIcon.indexFrame * i4, width, i4, 0, i, i2, i3);
                return;
            }
            graphics.drawRegion(imgWait, 0, idWait * 18, 18, 18, 0, i, i2, 3);
            setIndexWait();
        }
    }

    public static ImageIcon getImgIcon(short s) {
        ImageIcon imageIcon = (ImageIcon) listImgIcon.get(((int) s) + "");
        if (imageIcon == null) {
            ImageIcon imageIcon2 = new ImageIcon();
            imageIcon2.id = s;
            imageIcon2.isLoad = true;
            listImgIcon.put("" + ((int) s), imageIcon2);
            GameService.gI().doGetImgIcon(s);
            imageIcon2.timeGetBack = (int) (System.currentTimeMillis() / 1000);
            return imageIcon2;
        }
        if (imageIcon.img == null && (System.currentTimeMillis() / 1000) - imageIcon.timeGetBack > 30) {
            GameService.gI().doGetImgIcon(s);
            imageIcon.timeGetBack = (int) (System.currentTimeMillis() / 1000);
        }
        imageIcon.timeRemove = (int) (System.currentTimeMillis() / 1000);
        return imageIcon;
    }

    public static void paintIconTuBinh(Graphics graphics, short s, int i, int i2, int i3) {
        ImageIcon imgIcon = getImgIcon(s);
        if (imgIcon != null) {
            if (!imgIcon.isLoad) {
                graphics.drawImage(imgIcon.img, i, i2, i3);
            } else {
                graphics.drawRegion(imgWait, 0, idWait * 18, 18, 18, 0, i, i2, 3);
                setIndexWait();
            }
        }
    }
}
