package game.render.screen;

import game.model.FrameImage;
import game.model.ImageIcon;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStore;

public class GameData {
    private static GameData me;
    public static FrameImage imgSkillIcon;
    private boolean loadData = false;
    public static Vector imgHorse = new Vector();
    public static Hashtable listImgIcon = new Hashtable();

    public static GameData gI() {
        if (me != null) {
            return me;
        }
        GameData gameData = new GameData();
        me = gameData;
        return gameData;
    }


    public void getImage() {
        if (!this.loadData) {
            loadImgPotion();
            loadImgGemItem();
            this.loadData = true;
        }
    }

    public static void loadImgPotion() {
        try {
            RecordStore.deleteRecordStore(RMS.KHO_ANH_THUOC_CU);
        } catch (Exception e) {
        }
        byte[] array = RMS.loadRMS(RMS.KHO_ANH_THUOC);
        int ver = 0;
        if (array == null) {
            ver = 0;
        } else {
            ByteArrayInputStream bi = new ByteArrayInputStream(array);
            DataInputStream dis = new DataInputStream(bi);
            try {
                byte[] potion = new byte[dis.readShort()];
                dis.read(potion);
                byte sizePet = dis.readByte();
                for (int a = 0; a < sizePet; a++) {
                    byte[] dataHorse = new byte[dis.readShort()];
                    dis.read(dataHorse);
                    int i = dis.readByte();
                    byte[][] headHorse = new byte[i][];
                    for (int i2 = 0; i2 < i; i2++) {
                        headHorse[i2] = new byte[dis.readShort()];
                        dis.read(headHorse[i2]);
                    }
                    createImgHorse(dataHorse, headHorse);
                }
                ver = dis.readByte();
                dis.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        GameService.gI().doGetImage((byte) 2, ver);
    }

    private static void createImgHorse(byte[] data, byte[][] header) {
        for (byte[] bArr : header) {
            imgHorse.addElement(Res.createImgByHeader(bArr, data));
        }
    }

    public static void saveImgPotion(byte ver, byte[] arrayPotion, byte[] arrayItem, byte[][] dataHorse, byte[][][] headHorse) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bo);
        try {
            dos.writeShort(arrayPotion.length);
            dos.write(arrayPotion);
            dos.writeByte(dataHorse.length);
            for (int a = 0; a < dataHorse.length; a++) {
                dos.writeShort(dataHorse[a].length);
                dos.write(dataHorse[a]);
                dos.writeByte(headHorse[a].length);
                for (int i = 0; i < headHorse[a].length; i++) {
                    dos.writeShort(headHorse[a][i].length);
                    dos.write(headHorse[a][i]);
                }
                createImgHorse(dataHorse[a], headHorse[a]);
            }
            dos.writeByte(ver);
            RMS.saveRMS(RMS.KHO_ANH_THUOC, bo.toByteArray());
            dos.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void saveImgSkill(int ver, byte[] array) {
        imgSkillIcon = new FrameImage(Image.createImage(array, 0, array.length), 20, 20);
    }

    public static void saveImgGem(byte ver, byte[] array) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bo);
        try {
            dos.writeByte(ver);
            dos.writeShort(array.length);
            dos.write(array);
            RMS.saveRMS("gemItem", bo.toByteArray());
            dos.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void loadImgGemItem() {
        byte[] array = RMS.loadRMS("gemItem");
        int ver = 0;
        if (array == null) {
            ver = 0;
        } else {
            ByteArrayInputStream bi = new ByteArrayInputStream(array);
            DataInputStream dis = new DataInputStream(bi);
            try {
                ver = dis.readByte();
                byte[] man = new byte[dis.readShort()];
                dis.read(man);
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GameService.gI().doGetImage((byte) 3, ver);
    }

    /** pokemon 0.9.5: icon đang tải = ô sáng nhấp nháy nhẹ (không còn Poké Ball quay chung chung), tâm (x, y). */
    public static void veCho(Graphics g, int x, int y) {
        int a = (Canvas.gameTick % 20) < 10 ? 0x20 : 0x38;
        game.pokemon.KieuO.phu(g, x - 7, y - 7, 14, 14, a << 24 | 0x8090B0);
    }

    public static void paintIcon(Graphics g, short id, int x, int y) {
        if (game.pokemon.IconVatPham.veGoiId(g, id, x, y)) {
            return;   // 0.9.5: icon PMD trong gói client (không chờ máy chủ)
        }
        ImageIcon img = getImgIcon(id);
        if (img != null) {
            if (!img.isLoad) {
                int w = img.img.getWidth();
                int h = img.img.getHeight();
                int frame = 1;
                if (h > 25) {
                    h /= 3;
                    frame = 3;
                }
                if (Canvas.gameTick % 5 == 0) {
                    img.indexFrame = (byte) ((img.indexFrame + 1) % frame);
                }
                g.drawRegion(img.img, 0, img.indexFrame * h, w, h, 0, x, y, 3);
                return;
            }
            veCho(g, x, y);
        }
    }

    public static void paintIcon(Graphics g, short id, int x, int y, int align) {
        if (align == 3 && game.pokemon.IconVatPham.veGoiId(g, id, x, y)) {
            return;
        }
        ImageIcon img = getImgIcon(id);
        if (img != null) {
            if (!img.isLoad) {
                int w = img.img.getWidth();
                int h = img.img.getHeight();
                int frame = 1;
                if (h > 25) {
                    h /= 3;
                    frame = 3;
                }
                if (Canvas.gameTick % 5 == 0) {
                    img.indexFrame = (byte) ((img.indexFrame + 1) % frame);
                }
                g.drawRegion(img.img, 0, img.indexFrame * h, w, h, 0, x, y, align);
                return;
            }
            veCho(g, x, y);
        }
    }

    public static void paintIconCayThan(Graphics g, short id, int x, int y) {
        ImageIcon img = getImgIcon(id);
        if (img != null) {
            if (!img.isLoad) {
                g.drawImage(img.img, x, y, 3);
            } else {
                veCho(g, x, y);
            }
        }
    }

    public static void paintIconTuBinh(Graphics g, short id, int x, int y, int align) {
        ImageIcon img = getImgIcon(id);
        if (img != null) {
            if (!img.isLoad) {
                g.drawImage(img.img, x, y, align);
            } else {
                veCho(g, x, y);
            }
        }
    }

    /** 0.9.5: bảng icon theo id (không tạo chuỗi khoá mỗi lần vẽ như Hashtable cũ); id ngoài dải dùng bảng băm. */
    static final ImageIcon[] BANG = new ImageIcon[16384];

    /** Ô icon id đã có (không tạo, không hỏi máy chủ). */
    public static ImageIcon coIcon(short id) {
        if (id >= 0 && id < BANG.length) {
            return BANG[id];
        }
        return (ImageIcon) listImgIcon.get(String.valueOf((int) id));
    }

    public static ImageIcon getImgIcon(short id) {
        ImageIcon img = coIcon(id);
        int now = (int) (System.currentTimeMillis() / 1000);
        if (img == null) {
            img = new ImageIcon();
            img.id = id;
            img.isLoad = true;
            img.timeGetBack = now;
            img.timeRemove = now;
            if (id >= 0 && id < BANG.length) {
                BANG[id] = img;
            } else {
                listImgIcon.put(String.valueOf((int) id), img);
            }
            game.pokemon.IconVatPham.tai(img);   // đĩa (luồng nền) rồi mới hỏi máy chủ
        } else {
            if (img.img == null && now - img.timeGetBack > 30) {
                GameService.gI().doGetImgIcon(id);
                img.timeGetBack = now;
            }
            img.timeRemove = now;
        }
        return img;
    }

    /** Bỏ icon lâu không dùng (GameScr, mỗi ~10 giây). */
    public static void donIcon(long nowGiay) {
        for (int i = 0; i < BANG.length; i++) {
            ImageIcon img = BANG[i];
            if (img != null && !img.isLoad && nowGiay - img.timeRemove > 120) {
                BANG[i] = null;
            }
        }
    }
}
