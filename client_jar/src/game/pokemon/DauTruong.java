package game.pokemon;

import game.networklogic.GameService;
import game.render.Canvas;
import java.io.DataInputStream;
import java.util.Vector;
import network.Message;

/**
 * ĐẤU TRƯỜNG BẢO BỐI 3v3 (gói -109 của máy chủ HTBB >= 0.9.8, real.DauTruongGame; bố cục com.htbb.pokemon.GoiDauTruong).
 * Đội 3 Pokémon từ Hộp PC, chuẩn hoá cấp 50; đấu đội phòng thủ của người khác (bất đồng bộ). Máy chủ mô phỏng trận
 * theo lượt, client chỉ chọn chiêu / đổi Pokémon mỗi lượt (hết giờ thì máy chọn) và hiện kết quả. Màn:
 * {@link ManDauTruong} (thách đấu + đội hình), {@link ManTranDauTruong} (trận + kết quả).
 */
public final class DauTruong {
    public static final int LENH = -109;
    public static final int S_TRANG_THAI = 0, S_DOI_THU = 1, S_BAT_DAU = 2, S_LUOT = 3, S_KET_QUA = 4, S_GHI_CHU = 5, S_BANG_TUAN = 6;
    public static final int C_MO = 0, C_LUU_DOI = 1, C_TIM = 2, C_THACH = 3, C_HANH_DONG = 4, C_TU_DONG = 5, C_DAU_HANG = 6, C_BO_QUA = 7, C_XEM_LAI = 8, C_BANG_TUAN = 9;
    public static final String[] TEN_TT = {"", "Bỏng", "Độc", "Tê liệt", "Ngủ", "Đóng băng"};
    public static final String[] TEN_LOAI = {"Vật lý", "Đặc biệt", "Hỗ trợ"};

    private DauTruong() {
    }

    // ---------------------------------------------------------------- trạng thái + kho

    /** Một Pokémon trong Hộp PC chọn được cho đội. */
    public static final class Kho {
        public int id, dex, cap, sao, ivPt, suc, he1, he2, camLy;
        public boolean shiny;
        public String ten = "";
    }

    public static boolean coTrangThai;
    public static int mua, ngayConLai, diem, bac, hang, thang, thua, luotCon, luotNgay, luongTuan, tranLuongTuan, lamMoiCon, giayLuot = 20;
    public static String tenBac = "", ghiChu = "";
    public static int[] cong = new int[0], thu = new int[0];
    public static int sucCong, sucThu;
    public static Vector KHO = new Vector();

    public static Kho kho(int id) {
        for (int i = 0; i < KHO.size(); i++) {
            Kho k = (Kho) KHO.elementAt(i);
            if (k.id == id) {
                return k;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- đối thủ

    public static final class DoiThu {
        public int id, diem, bac, suc, daGap;
        public String ten = "";
        public int[] dex = new int[3], sao = new int[3];
        public boolean[] shiny = new boolean[3];
    }

    public static Vector DOI_THU = new Vector();
    public static boolean coDoiThu, choDoiThu;
    public static String ghiDoiThu = "";

    // ---------------------------------------------------------------- trận

    public static final class Con {
        public int dex, he1, he2, hpMax, hp, tt;
        public boolean shiny;
        public String ten = "";
    }

    public static final class Chieu {
        public int he, loai, uyLuc, ppMax, pp, vai;
        /** Lượt hồi chiêu còn lại; lý do không dùng được (0 dùng được, 1 hết PP, 2 đang hồi, 3 đối thủ đã có trạng thái). */
        public int hoi, lyDo;
        public String ten = "";
    }

    public static int tranId, diemDoiThu, luotToiDa = 50, luot;
    public static String tenDoiThu = "";
    /** Sân đấu (res/ui/dau_truong_san_&lt;n&gt;.png) + tên hiện trên băng lượt. */
    public static int san;
    /** Cấp nhân vật mở tốc độ diễn ×2 / ×3 (máy chủ gửi; máy chủ cũng tự kiểm). */
    public static int capToc2 = 20, capToc3 = 40;
    /** Đang xem lại một trận cũ (thoát không tính thua). */
    public static boolean xemLai;
    /** Chuỗi thắng hiện tại (chỉ hình) + 5 trận gần nhất (xem lại): {id trận, thắng, điểm đổi}, tên đối thủ. */
    public static int chuoiThang;
    public static int[][] LICH_SU = new int[0][];
    public static String[] LICH_SU_TEN = new String[0];
    /** Bảng tuần trong game: {hạng, điểm tuần, thắng, trận} + tên; dòng của mình. */
    public static int[][] TUAN = new int[0][];
    public static String[] TUAN_TEN = new String[0];
    public static int[] tuanToi = new int[4];
    public static boolean coTuan, choTuan;
    public static String tenSan = "";
    public static final Con[][] DOI = new Con[2][3];
    public static final Chieu[][] CHIEU = new Chieu[3][4];
    public static final int[] dangRa = new int[2];
    public static boolean xong, tuDong, choMayChu;
    /** Hạn chọn lượt theo đồng hồ client (ms). */
    public static long han;
    public static final Vector NHAT_KY = new Vector();

    /** Một sự kiện của lượt (máy chủ đã quyết kết quả; ManTranDauTruong diễn lại). Kiểu = DauTruongTran.SK_*. */
    public static final class SuKien {
        public String chu = "", hieuUng = "";
        public int kieu, ben, so, he, loai, co;
    }

    public static final int SK_CHU = 0, SK_RA_SAN = 1, SK_DUNG = 2, SK_TRUNG = 3, SK_TRUOT = 4, SK_TRANG_THAI = 5, SK_BAC = 6,
            SK_HOI = 7, SK_MAT_MAU = 8, SK_GUC = 9, SK_BI_CHAN = 10;
    public static final int CO_CHI_MANG = 1, CO_SIEU = 2, CO_YEU = 4, CO_MIEN = 8, CO_THAY_GUC = 1, CO_MO_DAU = 2;
    /** Sự kiện chờ diễn (theo thứ tự). */
    public static final Vector HANG = new Vector();
    public static final int NHAT_KY_TOI_DA = 40;

    // ---------------------------------------------------------------- kết quả

    public static boolean coKetQua, kqThang;
    public static int kqDiemTruoc, kqDiemSau, kqThuDoi, kqLuong, kqHeSo, kqLuotCon;
    public static String kqChu = "";

    public static Con con(int ben, int i) {
        return DOI[ben][i];
    }

    public static Con dangRa(int ben) {
        return DOI[ben][dangRa[ben]];
    }

    public static Chieu chieu(int o) {
        return CHIEU[dangRa[0]][o];
    }

    /** Không còn ô chiêu nào dùng được (hết PP / đang hồi / bị khoá): bấm ô nào cũng là "Vùng vẫy". */
    public static boolean hetPp() {
        for (int o = 0; o < 4; o++) {
            Chieu c = chieu(o);
            if (c != null && c.lyDo == 0 && c.pp > 0) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- tốc độ diễn (máy luôn tự đánh)

    /** Tốc độ diễn ×1 / ×2 / ×3 (×2, ×3 mở theo cấp - {@link #capToc2}, {@link #capToc3}). */
    public static int toc = 1;
    /** Bỏ qua (chỉ dùng kiểm thử / máy chủ): diễn ngay tới kết quả. */
    public static boolean boQua;

    static final String RMS_TU_CHON = "htbbDTtc";
    static int tuChonNap = -1;

    /** Cài đặt Đấu trường (lưu máy): true = tự chọn chiêu mỗi lượt; false (MẶC ĐỊNH) = máy tự đánh. */
    public static boolean tuChon() {
        if (tuChonNap < 0) {
            try {
                tuChonNap = game.render.screen.RMS.loadRMSInt(RMS_TU_CHON) == 1 ? 1 : 0;
            } catch (Throwable e) {
                tuChonNap = 0;
            }
        }
        return tuChonNap == 1;
    }

    public static void datTuChon(boolean b) {
        tuChonNap = b ? 1 : 0;
        try {
            game.render.screen.RMS.saveRMSInt(RMS_TU_CHON, tuChonNap);
        } catch (Throwable e) {
            // RMS đầy: chỉ giữ trong phiên
        }
    }

    public static int capNhanVat() {
        try {
            return Canvas.gameScr.mainChar.level;
        } catch (Throwable e) {
            return 0;
        }
    }

    /** Tốc độ kế tiếp được mở (vòng ×1 -> ×2 -> ×3 -> ×1); trả -cấp cần nếu tốc độ kế bị khoá. */
    public static int tocKe() {
        int ke = toc >= 3 ? 1 : toc + 1;
        int can = ke == 2 ? capToc2 : ke == 3 ? capToc3 : 0;
        return capNhanVat() >= can ? ke : -can;
    }

    public static void datToc(int t) {
        toc = Math.max(1, Math.min(3, t));
        try {
            Message m = moi(C_TU_DONG);
            m.writer().writeInt(tranId);
            m.writer().writeByte(1);
            m.writer().writeByte(toc);
            gui(m);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    public static void xemLaiTran(int id) {
        try {
            Message m = moi(C_XEM_LAI);
            m.writer().writeInt(id);
            gui(m);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    public static void xinTuan() {
        try {
            choTuan = true;
            gui(moi(C_BANG_TUAN));
        } catch (Throwable e) {
            choTuan = false;
        }
    }

    public static void boQuaTran() {
        boQua = true;
        try {
            Message m = moi(C_BO_QUA);
            m.writer().writeInt(tranId);
            gui(m);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    public static int conSong(int ben) {
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (DOI[ben][i] != null && DOI[ben][i].hp > 0) {
                n++;
            }
        }
        return n;
    }

    public static int giayCon() {
        long d = han - System.currentTimeMillis();
        return d <= 0 ? 0 : (int) ((d + 999) / 1000);
    }

    // ---------------------------------------------------------------- nhận gói

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

    static int[] docIds(DataInputStream r) throws java.io.IOException {
        int n = r.readByte();
        int[] a = new int[Math.max(0, n)];
        for (int i = 0; i < a.length; i++) {
            a[i] = r.readInt();
        }
        return a;
    }

    public static void doc(DataInputStream r) throws java.io.IOException {
        int kieu = r.readByte();
        if (kieu == S_TRANG_THAI) {
            boolean mo = r.readByte() != 0;
            mua = r.readShort();
            ngayConLai = r.readShort();
            diem = r.readShort();
            bac = r.readUnsignedByte();
            tenBac = r.readUTF();
            hang = r.readInt();
            thang = r.readShort();
            thua = r.readShort();
            luotCon = r.readUnsignedByte();
            luotNgay = r.readUnsignedByte();
            luongTuan = r.readShort();
            tranLuongTuan = r.readShort();
            lamMoiCon = r.readUnsignedByte();
            giayLuot = r.readUnsignedByte();
            cong = docIds(r);
            thu = docIds(r);
            sucCong = r.readInt();
            sucThu = r.readInt();
            int n = r.readShort();
            Vector v = new Vector();
            for (int i = 0; i < n; i++) {
                Kho k = new Kho();
                k.id = r.readInt();
                k.dex = r.readShort();
                k.cap = r.readUnsignedByte();
                k.shiny = r.readByte() != 0;
                k.sao = r.readUnsignedByte();
                k.ivPt = r.readUnsignedByte();
                k.suc = r.readShort();
                k.he1 = r.readByte();
                k.he2 = r.readByte();
                k.camLy = r.readUnsignedByte();
                k.ten = r.readUTF();
                v.addElement(k);
            }
            KHO = v;
            ghiChu = r.readUTF();
            chuoiThang = r.readShort();
            int nl = r.readByte();
            int[][] ls = new int[Math.max(0, nl)][];
            String[] lt = new String[ls.length];
            for (int i = 0; i < ls.length; i++) {
                int id = r.readInt();
                lt[i] = r.readUTF();
                int th = r.readByte();
                int dd = r.readShort();
                ls[i] = new int[]{id, th, dd};
            }
            LICH_SU = ls;
            LICH_SU_TEN = lt;
            coTrangThai = true;
            if (ghiChu.length() > 0) {
                ThongBao.hien(ghiChu, ThongBao.TIN);
            }
            if (mo) {
                ManDauTruong.moTuMayChu();
            }
        } else if (kieu == S_DOI_THU) {
            int n = r.readByte();
            Vector v = new Vector();
            for (int i = 0; i < n; i++) {
                DoiThu d = new DoiThu();
                d.id = r.readInt();
                d.ten = r.readUTF();
                d.diem = r.readShort();
                d.bac = r.readUnsignedByte();
                d.suc = r.readInt();
                d.daGap = r.readUnsignedByte();
                for (int k = 0; k < 3; k++) {
                    d.dex[k] = r.readShort();
                    d.shiny[k] = r.readByte() != 0;
                    d.sao[k] = r.readUnsignedByte();
                }
                v.addElement(d);
            }
            DOI_THU = v;
            lamMoiCon = r.readUnsignedByte();
            ghiDoiThu = r.readUTF();
            coDoiThu = true;
            choDoiThu = false;
        } else if (kieu == S_BAT_DAU) {
            tranId = r.readInt();
            tenDoiThu = r.readUTF();
            diemDoiThu = r.readShort();
            giayLuot = r.readUnsignedByte();
            luotToiDa = r.readUnsignedByte();
            san = r.readUnsignedByte();
            tenSan = r.readUTF();
            capToc2 = r.readUnsignedByte();
            capToc3 = r.readUnsignedByte();
            xemLai = r.readByte() != 0;
            for (int ben = 0; ben < 2; ben++) {
                for (int i = 0; i < 3; i++) {
                    Con c = new Con();
                    c.dex = r.readShort();
                    c.shiny = r.readByte() != 0;
                    c.ten = r.readUTF();
                    c.he1 = r.readByte();
                    c.he2 = r.readByte();
                    c.hpMax = r.readShort();
                    c.hp = c.hpMax;
                    DOI[ben][i] = c;
                }
            }
            for (int i = 0; i < 3; i++) {
                for (int o = 0; o < 4; o++) {
                    Chieu c = new Chieu();
                    c.ten = r.readUTF();
                    c.he = r.readByte();
                    c.loai = r.readByte();
                    c.uyLuc = r.readUnsignedByte();
                    c.ppMax = r.readUnsignedByte();
                    c.pp = c.ppMax;
                    c.vai = r.readByte();
                    CHIEU[i][o] = c;
                }
            }
            luot = 0;
            NHAT_KY.removeAllElements();
            HANG.removeAllElements();
            ghiNhatKy("Trận đấu với " + tenDoiThu + " bắt đầu! Chọn chiêu trong " + giayLuot + " giây mỗi lượt.");
            docKhoi(r);
            coKetQua = false;
            choMayChu = false;
            boQua = false;
            toc = 1;
            ManTranDauTruong.batDauCanh();
            ManTranDauTruong.mo();
        } else if (kieu == S_LUOT) {
            int id = r.readInt();
            if (id != tranId) {
                return;
            }
            luot = r.readShort();
            int n = r.readByte();
            for (int i = 0; i < n; i++) {
                SuKien e = new SuKien();
                e.chu = r.readUTF();
                e.kieu = r.readByte();
                e.ben = r.readByte();
                e.so = r.readShort();
                e.he = r.readByte();
                e.loai = r.readByte();
                e.co = r.readByte();
                e.hieuUng = r.readUTF();
                ghiNhatKy(e.chu);
                HANG.addElement(e);
            }
            docKhoi(r);
            choMayChu = false;
        } else if (kieu == S_KET_QUA) {
            int id = r.readInt();
            if (id != tranId) {
                return;
            }
            kqThang = r.readByte() != 0;
            kqDiemTruoc = r.readShort();
            kqDiemSau = r.readShort();
            kqThuDoi = r.readShort();
            kqLuong = r.readShort();
            kqHeSo = r.readShort();
            kqLuotCon = r.readUnsignedByte();
            kqChu = r.readUTF();
            if (!xemLai) {
                luotCon = kqLuotCon;
                diem = kqDiemSau;
            }
            xong = true;
            coKetQua = true;
            choMayChu = false;
            AmThanh.phat(kqThang ? AmThanh.XAC_NHAN : AmThanh.MO);
        } else if (kieu == S_BANG_TUAN) {
            int n = r.readByte();
            int[][] v = new int[Math.max(0, n)][];
            String[] t = new String[v.length];
            for (int i = 0; i < v.length; i++) {
                int[] d = new int[4];
                d[0] = r.readShort();
                t[i] = r.readUTF();
                d[1] = r.readShort();
                d[2] = r.readShort();
                d[3] = r.readShort();
                v[i] = d;
            }
            tuanToi[0] = r.readShort();
            r.readUTF();
            tuanToi[1] = r.readShort();
            tuanToi[2] = r.readShort();
            tuanToi[3] = r.readShort();
            TUAN = v;
            TUAN_TEN = t;
            coTuan = true;
            choTuan = false;
        } else if (kieu == S_GHI_CHU) {
            String s = r.readUTF();
            ghiDoiThu = s;
            choDoiThu = false;
            choMayChu = false;
            ThongBao.hien(s, ThongBao.TIN);
        }
    }

    static void docKhoi(DataInputStream r) throws java.io.IOException {
        for (int ben = 0; ben < 2; ben++) {
            dangRa[ben] = Math.max(0, Math.min(2, (int) r.readByte()));
            for (int i = 0; i < 3; i++) {
                int hp = r.readShort();
                int tt = r.readByte();
                if (DOI[ben][i] != null) {
                    DOI[ben][i].hp = hp;
                    DOI[ben][i].tt = tt;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int o = 0; o < 4; o++) {
                int pp = r.readUnsignedByte();
                if (CHIEU[i][o] != null) {
                    CHIEU[i][o].pp = pp;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int o = 0; o < 4; o++) {
                int h = r.readUnsignedByte();
                if (CHIEU[i][o] != null) {
                    CHIEU[i][o].hoi = h;
                }
            }
        }
        for (int o = 0; o < 4; o++) {
            int ld = r.readByte();
            Chieu c = CHIEU[Math.max(0, Math.min(2, dangRa[0]))][o];
            if (c != null) {
                c.lyDo = ld;
            }
        }
        xong = r.readByte() != 0;
        int giay = r.readUnsignedByte();
        tuDong = r.readByte() != 0;
        han = System.currentTimeMillis() + giay * 1000L;
    }

    static void ghiNhatKy(String s) {
        NHAT_KY.addElement(s);
        while (NHAT_KY.size() > NHAT_KY_TOI_DA) {
            NHAT_KY.removeElementAt(0);
        }
    }

    // ---------------------------------------------------------------- gửi

    static Message moi(int kieu) throws java.io.IOException {
        Message m = new Message(LENH);
        m.writer().writeByte(kieu);
        return m;
    }

    static void gui(Message m) {
        try {
            GameService.gI().session.sendMessage(m);
        } catch (Throwable e) {
            // mất kết nối: bỏ qua
        }
    }

    public static void xinMo() {
        try {
            gui(moi(C_MO));
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    public static void luuDoi(int loai, int[] ids) {
        try {
            Message m = moi(C_LUU_DOI);
            m.writer().writeByte(loai);
            m.writer().writeByte(ids.length);
            for (int i = 0; i < ids.length; i++) {
                m.writer().writeInt(ids[i]);
            }
            gui(m);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    public static void tim(boolean lamMoi) {
        try {
            Message m = moi(C_TIM);
            m.writer().writeByte(lamMoi ? 1 : 0);
            choDoiThu = true;
            gui(m);
        } catch (Throwable e) {
            choDoiThu = false;
        }
    }

    public static void thach(int idChar) {
        try {
            Message m = moi(C_THACH);
            m.writer().writeInt(idChar);
            m.writer().writeByte(tuChon() ? 0 : 1);   // 1 = máy tự đánh (mặc định), 0 = tự chọn chiêu
            gui(m);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    public static void hanhDong(int loai, int o) {
        if (xong || choMayChu) {
            return;
        }
        try {
            Message m = moi(C_HANH_DONG);
            m.writer().writeInt(tranId);
            m.writer().writeByte(loai);
            m.writer().writeByte(o);
            choMayChu = true;
            gui(m);
        } catch (Throwable e) {
            choMayChu = false;
        }
    }

    public static void datTuDong(boolean bat) {
        tuDong = bat;
        try {
            Message m = moi(C_TU_DONG);
            m.writer().writeInt(tranId);
            m.writer().writeByte(bat ? 1 : 0);
            gui(m);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    public static void dauHang() {
        try {
            Message m = moi(C_DAU_HANG);
            m.writer().writeInt(tranId);
            gui(m);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    /** Trận đang đánh (chưa có kết quả) - đóng màn trận thì hỏi đầu hàng. */
    public static boolean dangDanh() {
        return tranId > 0 && !xong && !coKetQua && !xemLai && DOI[0][0] != null && Canvas.gameScr != null;
    }
}
