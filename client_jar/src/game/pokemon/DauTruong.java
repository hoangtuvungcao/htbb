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
    public static final int S_TRANG_THAI = 0, S_DOI_THU = 1, S_BAT_DAU = 2, S_LUOT = 3, S_KET_QUA = 4, S_GHI_CHU = 5;
    public static final int C_MO = 0, C_LUU_DOI = 1, C_TIM = 2, C_THACH = 3, C_HANH_DONG = 4, C_TU_DONG = 5, C_DAU_HANG = 6;
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
        public int he, loai, uyLuc, ppMax, pp;
        public String ten = "";
    }

    public static int tranId, diemDoiThu, luotToiDa = 50, luot;
    public static String tenDoiThu = "";
    public static final Con[][] DOI = new Con[2][3];
    public static final Chieu[][] CHIEU = new Chieu[3][4];
    public static final int[] dangRa = new int[2];
    public static boolean xong, tuDong, choMayChu;
    /** Hạn chọn lượt theo đồng hồ client (ms). */
    public static long han;
    public static final Vector NHAT_KY = new Vector();
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

    public static boolean hetPp() {
        for (int o = 0; o < 4; o++) {
            Chieu c = chieu(o);
            if (c != null && c.pp > 0) {
                return false;
            }
        }
        return true;
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
                    CHIEU[i][o] = c;
                }
            }
            luot = 0;
            NHAT_KY.removeAllElements();
            ghiNhatKy("Trận đấu với " + tenDoiThu + " bắt đầu! Chọn chiêu trong " + giayLuot + " giây mỗi lượt.");
            docKhoi(r);
            coKetQua = false;
            choMayChu = false;
            ManTranDauTruong.mo();
        } else if (kieu == S_LUOT) {
            int id = r.readInt();
            if (id != tranId) {
                return;
            }
            luot = r.readShort();
            int n = r.readByte();
            for (int i = 0; i < n; i++) {
                ghiNhatKy(r.readUTF());
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
            luotCon = kqLuotCon;
            diem = kqDiemSau;
            xong = true;
            coKetQua = true;
            choMayChu = false;
            AmThanh.phat(kqThang ? AmThanh.XAC_NHAN : AmThanh.MO);
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
        return tranId > 0 && !xong && !coKetQua && DOI[0][0] != null && Canvas.gameScr != null;
    }
}
