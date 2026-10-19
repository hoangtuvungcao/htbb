package game.pokemon;

import game.model.Char;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.io.DataInputStream;
import java.util.Vector;
import network.Message;

/**
 * DANH HIỆU (0.9.7) - kho phía client: bảng danh hiệu của mình (máy chủ gửi, ManDanhHieu vẽ), nhãn danh hiệu đang
 * đeo của MỌI nhân vật (vẽ trên tên, màu theo độ hiếm), số danh hiệu mới chưa xem (ChamMoi.DANH_HIEU).
 *
 * <pre>
 * -110 S->C (HTBB com.htbb.pokemon.DanhHieuLogic):
 *   byte 0 bảng:  byte mở, short đang đeo, byte n độ hiếm + UTF tên, short n dòng: short id, UTF tên, UTF mô tả,
 *                 UTF gợi ý, byte độ hiếm, UTF hình, byte trạng thái (0 chưa / 1 có / 2 đeo), byte ẩn, int tiến độ,
 *                 int cần, int phút còn lại (-1 vĩnh viễn), UTF chỉ số, UTF nguồn, byte mới
 *   byte 1 nhãn:  short id nhân vật, short id danh hiệu (-1 = không), UTF tên, byte độ hiếm
 *   byte 2 số mới: short
 * -111 C->S: byte 0 xin bảng, 1 đeo (+short id), 2 tháo, 3 đã xem
 * </pre>
 * Không có dữ liệu hiển thị cứng ở client: tên / mô tả / điều kiện / độ hiếm đều từ máy chủ (bảng data_danh_hieu);
 * màu độ hiếm là token chủ đề (ChuDe.mauDanhHieu).
 */
public final class DanhHieuKho {
    public static final int LENH = -110, LENH_KHACH = -111;

    private DanhHieuKho() {
    }

    /** Một dòng bảng. */
    public static final class Dong {
        public int id;
        public String ten, moTa, goiY, hinh, chiSo, nguon;
        public int doHiem, trangThai, tienDo, can, phutConLai;
        public boolean an, moi;
        /** lúc nhận (ms) - đếm ngược phút còn lại tại client */
        public long luc;

        public boolean co() {
            return trangThai > 0;
        }

        public boolean deo() {
            return trangThai == 2;
        }

        /** Phút còn lại tính tới giờ (-1 vĩnh viễn). */
        public int conLai() {
            if (phutConLai < 0 || !co()) {
                return phutConLai;
            }
            long da = (System.currentTimeMillis() - luc) / 60000L;
            return (int) Math.max(0, phutConLai - da);
        }
    }

    /** Bảng gần nhất (null = chưa nhận). */
    public static Vector bang;
    public static int dangDeo = -1;
    public static String[] tenDoHiem = {};
    public static int soMoi;
    public static boolean choBang;
    /** Lần nhận bảng (kiểm thử). */
    public static int soLanNhan;

    // ------------------------------------------------------------------ mạng

    /** true = đã xử lý (lệnh -110). Gọi ở đầu MessageHandler.onMessage. */
    public static boolean nhan(Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        try {
            doc(msg.reader());
        } catch (Throwable e) {
            // gói hỏng: bỏ qua
        }
        return true;
    }

    /** Đọc một gói -110 (đã bỏ byte lệnh). */
    public static void doc(DataInputStream r) throws java.io.IOException {
        int loai = r.readByte();
        if (loai == 0) {
            boolean mo = r.readByte() != 0;
            dangDeo = r.readShort();
            int nh = r.readByte();
            String[] t = new String[nh];
            for (int i = 0; i < nh; i++) {
                t[i] = r.readUTF();
            }
            tenDoHiem = t;
            int n = r.readShort();
            Vector v = new Vector(n);
            long bay = System.currentTimeMillis();
            int moi = 0;
            for (int i = 0; i < n; i++) {
                Dong d = new Dong();
                d.id = r.readShort();
                d.ten = r.readUTF();
                d.moTa = r.readUTF();
                d.goiY = r.readUTF();
                d.doHiem = r.readByte();
                d.hinh = r.readUTF();
                d.trangThai = r.readByte();
                d.an = r.readByte() != 0;
                d.tienDo = r.readInt();
                d.can = r.readInt();
                d.phutConLai = r.readInt();
                d.chiSo = r.readUTF();
                d.nguon = r.readUTF();
                d.moi = r.readByte() != 0;
                d.luc = bay;
                if (d.moi) {
                    moi++;
                }
                v.addElement(d);
            }
            bang = v;
            soMoi = moi;
            choBang = false;
            soLanNhan++;
            if (mo) {
                ManDanhHieu.mo();
            }
        } else if (loai == 1) {
            short idChar = r.readShort();
            short id = r.readShort();
            String ten = r.readUTF();
            int hiem = r.readByte();
            Char c = timChar(idChar);
            if (c != null) {
                c.danhHieuId = id;
                c.danhHieuTen = id < 0 || ten.length() == 0 ? null : ten;
                c.danhHieuHiem = hiem;
            }
            if (id >= 0) {
                laDanhHieu[id & 0x7FFF & (laDanhHieu.length - 1)] = (short) id;
            }
        } else if (loai == 2) {
            soMoi = Math.max(0, r.readShort());
        }
    }

    /** id hiệu ứng đã biết là danh hiệu (nhãn chữ) - Char.getPartEff không xin dữ liệu hiệu ứng cho id này. */
    private static final short[] laDanhHieu = new short[256];

    public static boolean laDanhHieu(int id) {
        return id >= 0 && laDanhHieu[id & 0x7FFF & (laDanhHieu.length - 1)] == id;
    }

    static Char timChar(short id) {
        GameScr gs = Canvas.gameScr;
        if (gs == null) {
            return null;
        }
        if (gs.mainChar != null && gs.mainChar.ID == id) {
            return gs.mainChar;
        }
        Object a = gs.findCharByID(id);
        return a instanceof Char ? (Char) a : null;
    }

    static void gui(int loai, int id) {
        try {
            Message m = new Message(LENH_KHACH);
            m.writer().writeByte(loai);
            if (loai == 1) {
                m.writer().writeShort(id);
            }
            GameService.gI().session.sendMessage(m);
            m.cleanup();
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    public static void xin() {
        choBang = true;
        gui(0, 0);
    }

    public static void deo(int id) {
        gui(1, id);
    }

    public static void thao() {
        gui(2, 0);
    }

    /** Đã mở bảng: máy chủ xoá "mới" (chấm đỏ tắt). */
    public static void daXem() {
        if (soMoi > 0) {
            gui(3, 0);
        }
        soMoi = 0;
    }

    // ------------------------------------------------------------------ dữ liệu cho màn

    /** Tên độ hiếm (từ máy chủ). */
    public static String tenHiem(int h) {
        return h >= 0 && h < tenDoHiem.length ? tenDoHiem[h] : "";
    }

    /** Dòng đang đeo trong bảng (null). */
    public static Dong dongDeo() {
        Vector v = bang;
        for (int i = 0; v != null && i < v.size(); i++) {
            Dong d = (Dong) v.elementAt(i);
            if (d.deo()) {
                return d;
            }
        }
        return null;
    }

    /** Lọc bảng: 0 Đã có, 1 Chưa có (không ẩn), 2 Ẩn (chưa mở). */
    public static Vector loc(int tab) {
        Vector r = new Vector();
        Vector v = bang;
        for (int i = 0; v != null && i < v.size(); i++) {
            Dong d = (Dong) v.elementAt(i);
            boolean vao = tab == 0 ? d.co() : tab == 1 ? !d.co() && !d.an : !d.co() && d.an;
            if (vao) {
                r.addElement(d);
            }
        }
        return r;
    }

    /** Đếm theo tab (nhãn "Đã có (n)"). */
    public static int dem(int tab) {
        return loc(tab).size();
    }

    /** Hình (tên máy chủ gửi) -> KieuO.HINH_*. */
    public static int hinh(String s) {
        if (s == null) {
            return KieuO.HINH_SAO;
        }
        String[] ten = {"sao", "la", "lua", "giot", "set", "khien", "tim", "loc", "thep", "cong", "nhanh", "ma", "mat_troi",
            "ngu", "song", "no", "tang", "nanh", "vuot", "bong"};
        byte[] h = {KieuO.HINH_SAO, KieuO.HINH_LA, KieuO.HINH_LUA, KieuO.HINH_GIOT, KieuO.HINH_SET, KieuO.HINH_KHIEN,
            KieuO.HINH_TIM, KieuO.HINH_LOC, KieuO.HINH_THEP, KieuO.HINH_CONG, KieuO.HINH_NHANH, KieuO.HINH_MA,
            KieuO.HINH_MAT_TROI, KieuO.HINH_NGU, KieuO.HINH_SONG, KieuO.HINH_NO, KieuO.HINH_TANG, KieuO.HINH_NANH,
            KieuO.HINH_VUOT, KieuO.HINH_BONG};
        for (int i = 0; i < ten.length; i++) {
            if (ten[i].equals(s)) {
                return h[i];
            }
        }
        return KieuO.HINH_SAO;
    }
}
