package game.pokemon;

import game.model.Char;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.Canvas;
import java.io.DataInputStream;
import network.Message;

/**
 * HỌC CHIÊU THEO LOÀI + ĐIỂM HUẤN LUYỆN (0.9.8, máy chủ real.HocChieuGame). Lệnh -122 (server chỉ gửi cho bản >= 908):
 *
 * <pre>
 * S->C byte 0 BẢNG CHIÊU: short dex hiện tại, byte dòng, byte số ô, byte n, n x {byte ô, short loài, UTF tên loài,
 *          UTF tên chiêu, byte hệ, byte nguồn (0 tự học theo cấp, 1 NPC Blaziken), byte cấp học, int giá xu}
 *      byte 1 ĐIỂM HUẤN LUYỆN: byte bật; nếu bật: short tổng, short đã dùng, byte n, n x {byte id, UTF tên, UTF mô tả,
 *          short điểm, short trần, short ngưỡng, short ‰ trước, short ‰ sau}, long giá tẩy, int giây chờ, byte miễn phí,
 *          byte điểm / cấp, byte điểm huy hiệu, byte trần huy hiệu
 * C->S byte 0 xin cả hai gói; 1 phân bổ: byte n, n x {byte id, short thêm}; 2 tẩy điểm
 * </pre>
 *
 * Cấp chiêu (Char.skillLevelLearnt, gói 3 như cũ): -1 = học ở NPC, 0 = chưa học (tự học khi đủ cấp), 1..9 = đã học.
 * Máy chủ cũ không gửi -122: mọi hàm trả "chưa có" và giao diện giữ luật cũ.
 */
public final class HocChieu {
    public static final int LENH = -122;
    public static final int NGUON_CAP = 0, NGUON_NPC = 1;

    /** đã nhận bảng chiêu theo loài */
    public static boolean coLearnset;
    /** máy chủ bật Điểm huấn luyện (gói 1, bật = 1) */
    public static boolean batHL;
    /** đã nhận gói 1 ít nhất một lần */
    public static boolean coGoiHL;

    public static int dex, dong = -1, soO;
    // bảng chiêu (cả họ)
    public static int n;
    public static byte[] o = new byte[0], he = new byte[0], nguon = new byte[0], capHoc = new byte[0];
    public static short[] loai = new short[0];
    public static String[] tenLoai = new String[0], tenChieu = new String[0];
    public static int[] gia = new int[0];
    // điểm huấn luyện
    public static int tong, daDung, soDong;
    public static int[] id = new int[0], diem = new int[0], tran = new int[0], nguong = new int[0], pn1 = new int[0], pn2 = new int[0];
    public static String[] ten = new String[0], moTa = new String[0];
    public static long giaTay;
    public static int giayCho;
    /** lúc nhận giayCho (đếm lùi cục bộ) */
    public static long lucCho;
    public static boolean mienPhi;
    public static int diemMoiCap = 2, huyHieu, tranThuong;

    private static long lucXin;
    /** đếm chẩn đoán / kiểm thử */
    public static int soNhan, soGui;

    private HocChieu() {
    }

    // ---------------------------------------------------------------- mạng

    /** Gói -122 (gọi ở đầu MessageHandler). true = đã xử lý. */
    public static boolean nhan(Message msg) {
        if (msg == null || msg.command != LENH) {
            return false;
        }
        try {
            docGoi(msg.reader());
        } catch (Throwable e) {
            // gói hỏng: bỏ qua
        }
        return true;
    }

    /** Đọc thân gói (tách riêng để kiểm thử dựng gói giả). */
    public static void docGoi(DataInputStream r) throws java.io.IOException {
        soNhan++;
        int k = r.readByte();
        if (k == 0) {
            int d = r.readShort();
            int dg = r.readByte();
            int so = r.readUnsignedByte();
            int m = r.readUnsignedByte();
            byte[] o2 = new byte[m], he2 = new byte[m], ng2 = new byte[m], cap2 = new byte[m];
            short[] l2 = new short[m];
            String[] tl2 = new String[m], tc2 = new String[m];
            int[] g2 = new int[m];
            for (int i = 0; i < m; i++) {
                o2[i] = r.readByte();
                l2[i] = r.readShort();
                tl2[i] = r.readUTF();
                tc2[i] = r.readUTF();
                he2[i] = r.readByte();
                ng2[i] = r.readByte();
                cap2[i] = r.readByte();
                g2[i] = r.readInt();
            }
            o = o2;
            he = he2;
            nguon = ng2;
            capHoc = cap2;
            loai = l2;
            tenLoai = tl2;
            tenChieu = tc2;
            gia = g2;
            n = m;
            dex = d;
            dong = dg;
            soO = so;
            coLearnset = true;
        } else if (k == 1) {
            coGoiHL = true;
            boolean b = r.readByte() != 0;
            if (!b) {
                batHL = false;
                return;
            }
            int t = r.readShort();
            int dd = r.readShort();
            int m = r.readUnsignedByte();
            int[] id2 = new int[m], di2 = new int[m], tr2 = new int[m], ng2 = new int[m], a2 = new int[m], b2 = new int[m];
            String[] te2 = new String[m], mt2 = new String[m];
            for (int i = 0; i < m; i++) {
                id2[i] = r.readByte();
                te2[i] = r.readUTF();
                mt2[i] = r.readUTF();
                di2[i] = r.readShort();
                tr2[i] = r.readShort();
                ng2[i] = r.readShort();
                a2[i] = r.readShort();
                b2[i] = r.readShort();
            }
            long gt = r.readLong();
            int gc = r.readInt();
            boolean mp = r.readByte() != 0;
            int dmc = r.readByte();
            int hh = r.readByte();
            int tt = r.readByte();
            id = id2;
            diem = di2;
            tran = tr2;
            nguong = ng2;
            pn1 = a2;
            pn2 = b2;
            ten = te2;
            moTa = mt2;
            soDong = m;
            tong = t;
            daDung = dd;
            giaTay = gt;
            giayCho = gc;
            lucCho = System.currentTimeMillis();
            mienPhi = mp;
            diemMoiCap = dmc;
            huyHieu = hh;
            tranThuong = tt;
            batHL = true;
            BangHuanLuyen.khiNhan();
        }
    }

    static void gui(int k, int[] ids, int[] them) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(k);
            if (k == 1) {
                int c = 0;
                for (int i = 0; i < them.length; i++) {
                    if (them[i] > 0) {
                        c++;
                    }
                }
                m.writer().writeByte(c);
                for (int i = 0; i < them.length; i++) {
                    if (them[i] > 0) {
                        m.writer().writeByte(ids[i]);
                        m.writer().writeShort(them[i]);
                    }
                }
            }
            GameService.gI().session.sendMessage(m);
            m.cleanup();
            soGui++;
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    /** Xin cả hai gói (mở tab Kỹ năng / bảng Huấn luyện, vào thế giới). Chặn gửi dồn 3 giây. */
    public static void xin() {
        long bay = System.currentTimeMillis();
        if (bay - lucXin < 3000L) {
            return;
        }
        lucXin = bay;
        gui(0, null, null);
    }

    private static int lanTuXin;

    /** Xin khi chưa có dữ liệu (rẻ, gọi mỗi khung được): tối đa 3 lần mỗi lần vào game - máy chủ cũ không trả lời. */
    public static void xinNeuChua() {
        if ((!coLearnset || !coGoiHL) && lanTuXin < 3) {
            long truoc = lucXin;
            xin();
            if (lucXin != truoc) {
                lanTuXin++;
            }
        }
    }

    public static void phanBo(int[] them) {
        gui(1, id, them);
    }

    public static void tay() {
        gui(2, null, null);
    }

    // ---------------------------------------------------------------- điểm huấn luyện

    public static int conLai() {
        return batHL ? Math.max(0, tong - daDung) : 0;
    }

    /** ‰ cộng thêm của dòng i khi có d điểm (giảm dần sau ngưỡng). */
    public static int phanNghin(int i, int d) {
        if (i < 0 || i >= soDong || d <= 0) {
            return 0;
        }
        int x = Math.min(d, tran[i]);
        return Math.min(x, nguong[i]) * pn1[i] + Math.max(0, x - nguong[i]) * pn2[i];
    }

    /** "12,5%" */
    public static String phanTram(int pn) {
        int a = Math.abs(pn);
        return (pn < 0 ? "-" : "") + a / 10 + "," + a % 10 + "%";
    }

    /** Giây còn chờ tẩy (đếm lùi từ lúc nhận). */
    public static int choCon() {
        if (giayCho <= 0) {
            return 0;
        }
        long qua = (System.currentTimeMillis() - lucCho) / 1000L;
        return (int) Math.max(0L, giayCho - qua);
    }

    // ---------------------------------------------------------------- bảng chiêu theo loài

    static MainChar mc() {
        return Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
    }

    /** Có bảng chiêu dùng được cho dòng của người chơi. */
    public static boolean dung(int lop) {
        return coLearnset && lop == dong;
    }

    /** Dòng của loài hiện tại cho ô oo, -1 nếu loài chưa học được. */
    public static int hangLoai(int oo) {
        for (int i = 0; i < n; i++) {
            if (o[i] == oo && loai[i] == dex) {
                return i;
            }
        }
        return -1;
    }

    /** Dòng loài khác (tiến hoá) học được ô oo sớm nhất, -1 nếu không có. */
    public static int hangKhac(int oo) {
        int t = -1;
        for (int i = 0; i < n; i++) {
            if (o[i] == oo && loai[i] != dex && (t < 0 || capHoc[i] < capHoc[t])) {
                t = i;
            }
        }
        return t;
    }

    static int hangBatKy(int oo) {
        int h = hangLoai(oo);
        return h >= 0 ? h : hangKhac(oo);
    }

    /** Tên chiêu theo loài (nhánh Eevee đổi tên ô 2/8/10). */
    public static String tenChieu(int lop, int oo) {
        if (dung(lop)) {
            int h = hangBatKy(oo);
            if (h >= 0 && tenChieu[h] != null && tenChieu[h].length() > 0) {
                return tenChieu[h];
            }
        }
        return PokeChieu.ten(lop, oo);
    }

    public static int heChieu(int lop, int oo) {
        if (dung(lop)) {
            int h = hangBatKy(oo);
            if (h >= 0) {
                return he[h];
            }
        }
        return PokeChieu.he(lop, oo);
    }

    public static boolean laNpc(int lop, int oo) {
        if (!dung(lop)) {
            return false;
        }
        int h = hangBatKy(oo);
        return h >= 0 && nguon[h] == NGUON_NPC;
    }

    /** Cấp học của loài hiện tại (0 nếu loài chưa học được). */
    public static int capHocLoai(int oo) {
        int h = hangLoai(oo);
        return h < 0 ? 0 : capHoc[h];
    }

    /** NPC dạy ô oo: loài hiện tại học được ngay (đủ cấp). */
    public static boolean npcHocDuocNgay(int oo, int capNguoi) {
        int h = hangLoai(oo);
        return h >= 0 && nguon[h] == NGUON_NPC && capNguoi >= capHoc[h];
    }

    /** Lý do NPC chưa dạy được (null = dạy được ngay). */
    public static String lyDoNpc(int oo, int capNguoi) {
        int h = hangLoai(oo);
        if (h >= 0) {
            return capNguoi >= capHoc[h] ? null : "Cần cấp " + capHoc[h];
        }
        int k = hangKhac(oo);
        return k < 0 ? "Loài này không học được" : "Khi tiến hoá: " + tenLoai[k] + " cấp " + capHoc[k];
    }

    /** Trạng thái ngắn (viên phải hàng chiêu). */
    public static String trangThai(int oo, int cap, int capNguoi) {
        if (cap > 0) {
            return cap >= PokeChieu.CAP_TOI_DA ? "Tối đa" : "Cấp " + cap;
        }
        int h = hangLoai(oo);
        if (h >= 0) {
            if (nguon[h] == NGUON_NPC) {
                return "NPC · cấp " + capHoc[h];
            }
            return "Tự học cấp " + capHoc[h];
        }
        int k = hangKhac(oo);
        return k < 0 ? "Chưa học được" : "Tiến hoá: cấp " + capHoc[k];
    }

    /** Dòng mô tả trạng thái (chi tiết / dòng phụ hàng chiêu). */
    public static String moTaTrangThai(int oo, int cap, int capNguoi) {
        int h = hangLoai(oo);
        if (cap > 0) {
            return "Đã học · cấp " + cap + "/" + PokeChieu.CAP_TOI_DA;
        }
        if (h >= 0) {
            if (nguon[h] == NGUON_NPC) {
                return "Học ở NPC Blaziken từ cấp " + capHoc[h] + ": " + TienTe.chu(TienTe.XU, gia[h]);
            }
            return capNguoi >= capHoc[h] ? "Tự học ngay (không tốn điểm)" : "Tự học khi đạt cấp " + capHoc[h] + " (không tốn điểm)";
        }
        int k = hangKhac(oo);
        return k < 0 ? "Loài này không học được chiêu này" : "Khi tiến hoá thành " + tenLoai[k] + " (cấp " + capHoc[k] + ")";
    }

    /** Số chiêu NPC loài hiện tại học được NGAY (đủ cấp) mà chưa học. */
    public static int demNpcNgay(int capNguoi) {
        int c = 0;
        for (int i = 0; i < n; i++) {
            if (loai[i] == dex && nguon[i] == NGUON_NPC && capNguoi >= capHoc[i] && capChieu(o[i]) < 1) {
                c++;
            }
        }
        return c;
    }

    static int capChieu(int i) {
        try {
            return Char.skillLevelLearnt != null && i >= 0 && i < Char.skillLevelLearnt.length ? Char.skillLevelLearnt[i] : -1;
        } catch (Throwable e) {
            return -1;
        }
    }

    /** Đặt lại (đổi nhân vật / kiểm thử). */
    public static void xoa() {
        coLearnset = false;
        batHL = false;
        coGoiHL = false;
        n = 0;
        soDong = 0;
        lucXin = 0L;
        lanTuXin = 0;
    }
}
