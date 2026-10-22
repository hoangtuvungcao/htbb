package game.pokemon;

import game.model.Actor;
import game.model.Monster;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.rms.RecordStore;
import network.Message;

/**
 * Lenh mang dot loai day du (1.0, client ban &gt;= 909; may chu chi gui cho client tu ban do tro len):
 *
 * -125 POKE_QUAI_CON (S-&gt;C): u8 n, n x (i16 idQuai, i16 dex, u8 shiny, u8 bac). Loai RIENG tung con quai (dex 0 =
 *      dung loai mac dinh cua template, lenh -91). Gui ngay sau lenh 4 them quai do, va thanh lo sau khi vao map.
 * -126 POKE_LOAI (hai chieu), byte dau = kieu:
 *      C-&gt;S 0 XIN_SPRITE  i16 dex, u8 shiny       S-&gt;C 0 KHUC_SPRITE i16 dex, u8 shiny, i32 tong, i32 viTri, u16 len, byte
 *      C-&gt;S 1 XIN_BANG    i32 bamHienCo           S-&gt;C 1 BANG_LOAI (BangLoai.docBang) / 2 BANG_KHONG_DOI i32 bam
 *      C-&gt;S 3 XIN_POKEDEX                         S-&gt;C 3 POKEDEX (BangLoai.docPokedex)
 */
public final class LoaiMang {
    public static final int POKE_QUAI_CON = -125;
    public static final int POKE_LOAI = -126;
    public static final int K_SPRITE = 0, K_BANG = 1, K_BANG_KHONG_DOI = 2, K_POKEDEX = 3;
    static final String KHO_BANG = "loaibang";

    /** Kiem tren JDK: khac null thi gui vao day thay vi may chu. */
    public static Vector ghiGui;

    /** idQuai -&gt; int[]{dex, shiny, bac} cho quai chua co tren man (lenh 4 den sau / ngoai tam). */
    static final Hashtable cho = new Hashtable();
    private static boolean daXinBang;
    private static boolean daNapRms;

    private LoaiMang() {
    }

    static boolean gui(Message m) {
        if (ghiGui != null) {
            ghiGui.addElement(m);
            return true;
        }
        try {
            game.networklogic.GameService gs = game.networklogic.GameService.gI();
            if (gs.session == null || !gs.session.isConnected()) {
                return false;
            }
            gs.session.sendMessage(m);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    static boolean guiXinSprite(int dex) {
        return guiXinSprite(dex, false);
    }

    /** Xin goi sprite ban thuong / ban sac mau ("&lt;NNNN&gt;s.goi"). */
    static boolean guiXinSprite(int dex, boolean shiny) {
        try {
            Message m = new Message(POKE_LOAI);
            m.writer().writeByte(K_SPRITE);
            m.writer().writeShort(dex);
            m.writer().writeByte(shiny ? 1 : 0);
            return gui(m);
        } catch (Throwable e) {
            return false;
        }
    }

    /** Xin bang loai (gui bam dang co - may chu tra 2 neu khong doi). Moi phien 1 lan (tru khi ep). */
    public static void xinBang(boolean ep) {
        if (daXinBang && !ep) {
            return;
        }
        daXinBang = true;
        napBangRms();
        try {
            Message m = new Message(POKE_LOAI);
            m.writer().writeByte(K_BANG);
            m.writer().writeInt(BangLoai.coBang() ? BangLoai.bam : 0);
            gui(m);
        } catch (Throwable e) {
            // bo qua
        }
    }

    public static void xinPokedex() {
        try {
            Message m = new Message(POKE_LOAI);
            m.writer().writeByte(K_POKEDEX);
            gui(m);
        } catch (Throwable e) {
            // bo qua
        }
    }

    /** Bang loai da luu RMS (lan dau can). */
    static void napBangRms() {
        if (daNapRms || BangLoai.coBang()) {
            return;
        }
        daNapRms = true;
        try {
            RecordStore r = RecordStore.openRecordStore(KHO_BANG, false);
            byte[] b = r.getRecord(1);
            r.closeRecordStore();
            BangLoai.docBang(b);
        } catch (Throwable e) {
            // chua co
        }
    }

    static void luuBangRms(byte[] b) {
        try {
            RecordStore r = RecordStore.openRecordStore(KHO_BANG, true);
            if (r.getNumRecords() > 0) {
                r.setRecord(1, b, 0, b.length);
            } else {
                r.addRecord(b, 0, b.length);
            }
            r.closeRecordStore();
        } catch (Throwable e) {
            // day: lan sau xin lai ca bang
        }
    }

    /** Doi map / dang nhap lai: quen loai cho cua quai cu. */
    public static void doiMap() {
        cho.clear();
    }

    /** Dang xuat / mat ket noi: xin lai bang + pokedex o phien sau. */
    public static void datLaiPhien() {
        cho.clear();
        daXinBang = false;
        BangLoai.xoaPokedex();
        KhoSprite.datLaiPhien();
    }

    /** true = da xu ly (-125 / -126). */
    public static boolean xuLy(Message msg) {
        int lenh = msg.command;
        if (lenh != POKE_QUAI_CON && lenh != POKE_LOAI) {
            return false;
        }
        try {
            if (lenh == POKE_QUAI_CON) {
                docQuaiCon(msg.reader());
            } else {
                docLoai(msg.reader());
            }
        } catch (Throwable e) {
            // goi hong: bo qua
        }
        return true;
    }

    static void docQuaiCon(DataInputStream r) throws java.io.IOException {
        int n = r.readUnsignedByte();
        for (int i = 0; i < n; i++) {
            short id = r.readShort();
            int dex = r.readShort();
            int shiny = r.readUnsignedByte();
            int bac = r.readUnsignedByte();
            dat(id, dex, shiny != 0, bac);
            if (dex > 0) {
                taiTruoc(dex, shiny != 0);
            }
        }
        // co may chu moi: lay ten loai cho nhan quai
        xinBang(false);
    }

    /** Gan loai rieng cho quai id (tim tren man; chua co thi giu cho lenh 4). */
    public static void dat(short id, int dex, boolean shiny, int bac) {
        Monster m = timQuai(id);
        if (m != null) {
            m.datLoaiRieng(dex, shiny, bac);
            cho.remove(new Short(id));
        } else if (dex > 0) {
            cho.put(new Short(id), new int[]{dex, shiny ? 1 : 0, bac});
        } else {
            cho.remove(new Short(id));
        }
    }

    /** GameScr.onActorMove vua tao quai: ap loai cho (neu co). */
    public static void apDung(Monster m) {
        if (m == null || cho.isEmpty()) {
            return;
        }
        int[] v = (int[]) cho.remove(new Short(m.ID));
        if (v != null) {
            m.datLoaiRieng(v[0], v[1] != 0, v[2]);
        }
    }

    static Monster timQuai(short id) {
        GameScr gs = Canvas.gameScr;
        if (gs == null) {
            return null;
        }
        Vector a = gs.actors;
        for (int i = a.size() - 1; i >= 0; i--) {
            Object o = a.elementAt(i);
            if (o instanceof Monster && ((Actor) o).catagory == 1 && ((Actor) o).ID == id) {
                return (Monster) o;
            }
        }
        return null;
    }

    /** Loai khong co trong jar: xin truoc khi can ve (thu tu = thu tu goi -125). */
    static void taiTruoc(int dex) {
        taiTruoc(dex, false);
    }

    /** Quai sac mau: xin ca goi shiny (ban thuong van can cho luc dang tai / loai chua co shiny). */
    static void taiTruoc(int dex, boolean shiny) {
        if (!PokeBody.coTrongJar(dex)) {
            KhoSprite.xin(dex);
        }
        if (shiny && !PokeBody.coTrongJar(dex, true)) {
            KhoSprite.xin(dex, true);
        }
    }

    static void docLoai(DataInputStream r) throws java.io.IOException {
        int kieu = r.readUnsignedByte();
        switch (kieu) {
            case K_SPRITE: {
                int dex = r.readShort();
                boolean shiny = r.readUnsignedByte() != 0;
                int tong = r.readInt();
                int viTri = r.readInt();
                byte[] du = null;
                if (tong > 0) {
                    int len = r.readUnsignedShort();
                    du = new byte[len];
                    r.readFully(du);
                }
                int xong = KhoSprite.nhanKhuc(dex, shiny, tong, viTri, du);
                if (xong > 0) {
                    PokeBody.boLoai(xong);
                }
                break;
            }
            case K_BANG: {
                byte[] than = new byte[r.available()];
                r.readFully(than);
                if (BangLoai.docBang(than)) {
                    luuBangRms(than);
                }
                break;
            }
            case K_BANG_KHONG_DOI:
                napBangRms();
                break;
            case K_POKEDEX:
                BangLoai.docPokedex(r);
                break;
            default:
                break;
        }
    }

    /** Doc than goi tu mang byte (kiem tren JDK). */
    public static void docLoai(byte[] than) throws java.io.IOException {
        docLoai(new DataInputStream(new ByteArrayInputStream(than)));
    }
}
