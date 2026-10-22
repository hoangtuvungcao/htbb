package game.pokemon;

import game.model.Actor;
import game.model.Char;
import game.model.LiveActor;
import game.model.Monster;
import game.model.MonsterTemplate;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * 0.9.8 THU PHỤC POKÉMON (gói -121 của máy chủ HTBB, real.BatPokemonGame). Người chơi đánh một Pokémon hoang dã xuống
 * dưới ngưỡng HP (máy chủ gửi, mặc định 25%) thì dưới khung mục tiêu hiện nút "Ném Poké Ball ×n" (chạm "Đổi" để đổi loại
 * bóng). Bấm: bóng bay theo đường cong tới Pokémon, Pokémon "vào bóng" (ẩn), bóng lắc 0..3 lần theo kết quả máy chủ rồi
 * sáng sao (bắt được) / bật mở (thoát) / khói (bỏ chạy). Máy chủ quyết định mọi thứ (tỉ lệ, điều kiện, trừ bóng); client
 * chỉ hiện. Tự động đánh KHÔNG bao giờ ném bóng (không có đường nào gọi {@link #nem} ngoài nút).
 *
 * <p>Cũng nhận: trạng thái (bóng còn, bắt hôm nay, Hộp PC), Pokédex ("Đã bắt" theo loài cho ManPokedex), bạn đồng hành
 * (Pokémon trong Hộp PC đi theo - chỉ để nhìn; vẽ ở {@link #veBanDongHanh} khi người chơi không có pet).
 */
public final class BatPokemon {
    public static final int LENH = -121;
    public static final int C_TRANG_THAI = 0, C_NEM = 1, C_POKEDEX = 2, C_XIN_HOP = 3, C_HANH_DONG = 4, C_XIN_LOAI = 5, C_TRAO_DOI = 6;
    public static final int S_TRANG_THAI = 0, S_KET_QUA = 1, S_POKEDEX = 2, S_DONG_HANH = 3, S_HOP_PC = 4, S_LOAI = 5, S_TRAO_DOI = 6;
    /** Hành động trên một con (gói kiểu 4). */
    public static final int H_THEO = 0, H_LUYEN = 1, H_TIEN_HOA = 2, H_THA = 3, H_CHUYEN = 4;
    /** Trao đổi (gói kiểu 6): lệnh con. */
    public static final int T_MOI = 0, T_TRA_LOI = 1, T_DAT = 2, T_KHOA = 3, T_XAC_NHAN = 4, T_HUY = 5, T_XIN = 6;

    private BatPokemon() {
    }

    // ---------------------------------------------------------------- trạng thái

    public static boolean coTrangThai;
    static long xinLuc;
    public static int nguongHp = 25;
    /** bóng: thứ tự, tên, số còn, trần (phần nghìn) */
    public static int[] bongThuTu = new int[0];
    public static String[] bongTen = new String[0];
    public static int[] bongSo = new int[0];
    public static int batNgay, tranNgay, soCon, sucChua;
    /** chỉ số bóng đang chọn trong mảng */
    public static int bongChon;

    /** Pokédex: dex -&gt; cờ (bit0 đã hạ, bit1 đã bắt, bit2 bắt shiny). */
    public static final Hashtable DEX = new Hashtable();
    public static int soDaBat;
    public static boolean coDex;

    public static int coDex(int dex) {
        Object o = DEX.get(new Integer(dex));
        return o == null ? 0 : ((Integer) o).intValue();
    }

    public static boolean daBat(int dex) {
        return (coDex(dex) & 2) != 0;
    }

    static void xinTrangThai() {
        long bay = System.currentTimeMillis();
        if (bay - xinLuc < 8000) {
            return;
        }
        xinLuc = bay;
        gui(C_TRANG_THAI, -1, -1);
    }

    public static void xinPokedex() {
        gui(C_POKEDEX, -1, -1);
    }

    static void gui(int kieu, int idQuai, int thuTu) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(kieu);
            if (kieu == C_NEM) {
                m.writer().writeShort(idQuai);
                m.writer().writeByte(thuTu);
            } else if (kieu == C_TRANG_THAI) {
                m.writer().writeByte(1);   // 0.9.8: client có màn Hộp PC / trao đổi Pokémon
            }
            GameService.gI().session.sendMessage(m);
        } catch (Throwable e) {
            // mất kết nối: bỏ qua
        }
    }

    // ---------------------------------------------------------------- ném bóng: hiệu ứng

    /** Một lần ném đang diễn. */
    static final class Nem {
        short charId, idQuai;
        int thuTu;
        long t0;
        /** -1 chờ máy chủ; 0 trượt, 1 bắt được, 2 bỏ chạy, 3 bị từ chối (không trừ bóng). */
        int kq = -1;
        int lac;
        long tKq;
        int dex;
        boolean shiny;
        boolean cuaToi;
        int x0, y0, x1, y1;
    }

    static final Vector NEM = new Vector();
    /** 5 tia sao (cos / sin x1024 ở 90, 162, 234, 306, 18 độ). */
    static final int[] SAO_X = {0, -974, -602, 602, 974}, SAO_Y = {1024, 316, -828, -828, 316};
    static final long BAY_MS = 450, LAC_MS = 380, KET_MS = 900, CHO_TOI_DA_MS = 4000;

    /** Quái đang "ở trong bóng" (ẩn khi vẽ) - Monster.paintPokemon hỏi. */
    public static boolean anQuai(short id) {
        for (int i = 0; i < NEM.size(); i++) {
            Nem n = (Nem) NEM.elementAt(i);
            if (n.idQuai == id && dangTrongBong(n, System.currentTimeMillis())) {
                return true;
            }
        }
        return false;
    }

    static boolean dangTrongBong(Nem n, long bay) {
        if (bay - n.t0 < BAY_MS) {
            return false;
        }
        if (n.kq < 0) {
            return true;
        }
        if (n.kq == 3) {
            return false;
        }
        long d = bay - Math.max(n.tKq, n.t0 + BAY_MS);
        return d < n.lac * LAC_MS || n.kq == 1;
    }

    static Actor quai(short id) {
        try {
            Actor a = Canvas.gameScr.findActorByID_Cat(id, (byte) 1);
            return a;
        } catch (Throwable e) {
            return null;
        }
    }

    static Actor nguoi(short id) {
        try {
            if (Canvas.gameScr.mainChar != null && Canvas.gameScr.mainChar.ID == id) {
                return Canvas.gameScr.mainChar;
            }
            return Canvas.gameScr.findCharByID(id);
        } catch (Throwable e) {
            return null;
        }
    }

    static Nem batDau(short charId, short idQuai, int thuTu, boolean cuaToi) {
        Nem n = new Nem();
        n.charId = charId;
        n.idQuai = idQuai;
        n.thuTu = thuTu;
        n.t0 = System.currentTimeMillis();
        n.cuaToi = cuaToi;
        Actor a = nguoi(charId), q = quai(idQuai);
        n.x0 = a != null ? a.x : 0;
        n.y0 = a != null ? a.y - 20 : 0;
        n.x1 = q != null ? q.x : n.x0;
        n.y1 = q != null ? q.y - 6 : n.y0;
        if (NEM.size() > 6) {
            NEM.removeElementAt(0);
        }
        NEM.addElement(n);
        AmThanh.phat(AmThanh.NHAT);
        return n;
    }

    /** Nút "Ném bóng": bắt đầu hiệu ứng ngay (máy chủ trả kết quả sau). */
    public static void nem(LiveActor quai) {
        if (quai == null || bongThuTu.length == 0) {
            return;
        }
        int i = Math.max(0, Math.min(bongThuTu.length - 1, bongChon));
        if (bongSo[i] <= 0) {
            ThongBao.hien("Hết " + bongTen[i] + ". Mua ở Y tá Chansey (Trung tâm Pokémon).", ThongBao.LOI);
            return;
        }
        for (int k = 0; k < NEM.size(); k++) {
            Nem n = (Nem) NEM.elementAt(k);
            if (n.cuaToi && n.kq < 0 && System.currentTimeMillis() - n.t0 < CHO_TOI_DA_MS) {
                return;   // đang chờ kết quả lần trước
            }
        }
        short me = Canvas.gameScr.mainChar.ID;
        batDau(me, quai.ID, bongThuTu[i], true);
        gui(C_NEM, quai.ID, bongThuTu[i]);
    }

    // ---------------------------------------------------------------- mạng

    public static boolean nhan(Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            int kieu = r.readByte();
            switch (kieu) {
                case S_TRANG_THAI: {
                    nguongHp = r.readUnsignedByte();
                    int n = r.readUnsignedByte();
                    int[] tt = new int[n];
                    String[] ten = new String[n];
                    int[] so = new int[n];
                    for (int i = 0; i < n; i++) {
                        tt[i] = r.readUnsignedByte();
                        ten[i] = r.readUTF();
                        so[i] = r.readShort();
                        r.readShort();
                    }
                    batNgay = r.readShort();
                    tranNgay = r.readShort();
                    soCon = r.readShort();
                    sucChua = r.readShort();
                    datBong(tt, ten, so);
                    coTrangThai = true;
                    break;
                }
                case S_KET_QUA: {
                    short charId = r.readShort();
                    short idQuai = r.readShort();
                    int thuTu = r.readUnsignedByte();
                    int kq = r.readUnsignedByte();
                    int lac = r.readUnsignedByte();
                    int dex = r.readShort();
                    boolean shiny = r.readByte() != 0;
                    r.readShort();
                    r.readUTF();
                    ketQua(charId, idQuai, thuTu, kq, lac, dex, shiny);
                    break;
                }
                case S_POKEDEX: {
                    int n = r.readShort();
                    DEX.clear();
                    for (int i = 0; i < n; i++) {
                        int dex = r.readShort();
                        int co = r.readUnsignedByte();
                        DEX.put(new Integer(dex), new Integer(co));
                    }
                    soDaBat = r.readShort();
                    coDex = true;
                    break;
                }
                case S_DONG_HANH: {
                    short id = r.readShort();
                    short dex = r.readShort();
                    boolean shiny = r.readByte() != 0;
                    Actor a = nguoi(id);
                    if (a instanceof Char) {
                        ((Char) a).pokeBanDex = dex;
                        ((Char) a).pokeBanShiny = shiny;
                    }
                    break;
                }
                case S_HOP_PC:
                    docHop(r);
                    break;
                case S_LOAI:
                    docLoai(r);
                    break;
                case S_TRAO_DOI:
                    docTraoDoi(r);
                    break;
                default:
                    break;
            }
        } catch (Throwable e) {
            // gói hỏng: bỏ qua
        }
        return true;
    }

    // ---------------------------------------------------------------- 0.9.8 Hộp PC (gói kiểu 4)

    /** Một Pokémon trong Hộp PC / trong trao đổi (máy chủ tính mọi số). */
    public static final class Con {
        public int id, dex, cap, sao, ivPt, sucManh, tinhCach, doHiem, keo, keoLuyen, dexSau, capTienHoa, keoTienHoa, co;
        public boolean shiny;
        public int[] iv = new int[6];
        public String ten = "", chuTinhCach = "", tenKeo = "", chuChuyen = "";

        public boolean trungLoai() {
            return (co & 1) != 0;
        }

        public boolean khoaDoi() {
            return (co & 2) != 0;
        }

        public boolean nhanQuaDoi() {
            return (co & 4) != 0;
        }

        public boolean tienHoaKhiDoi() {
            return (co & 8) != 0;
        }

        public boolean dangTheo() {
            return (co & 16) != 0;
        }
    }

    public static Vector HOP = new Vector();
    public static int hopSoCon, hopSucChua, hopTheo, hopGiu;
    public static boolean coHop;
    static final String[] TEN_CHI_SO = {"HP", "Tấn công", "Phòng thủ", "Tấn công ĐB", "Phòng thủ ĐB", "Tốc độ"};
    public static final String[] TEN_DO_HIEM = {"Thường", "Ít gặp", "Hiếm", "Rất hiếm"};

    public static String tenDoHiem(int h) {
        return TEN_DO_HIEM[Math.max(0, Math.min(3, h))];
    }

    static void docHop(DataInputStream r) throws java.io.IOException {
        int soCon = r.readShort();
        int suc = r.readShort();
        int theo = r.readInt();
        int giu = r.readShort();
        int n = r.readShort();
        Vector v = new Vector();
        for (int i = 0; i < n; i++) {
            Con c = new Con();
            c.id = r.readInt();
            c.dex = r.readShort();
            c.cap = r.readUnsignedByte();
            c.shiny = r.readByte() != 0;
            c.sao = r.readUnsignedByte();
            c.ivPt = r.readUnsignedByte();
            c.sucManh = r.readShort();
            c.tinhCach = r.readUnsignedByte();
            c.doHiem = r.readUnsignedByte();
            c.keo = r.readShort();
            c.keoLuyen = r.readShort();
            c.dexSau = r.readShort();
            c.capTienHoa = r.readUnsignedByte();
            c.keoTienHoa = r.readShort();
            c.co = r.readUnsignedByte();
            for (int k = 0; k < 6; k++) {
                c.iv[k] = r.readUnsignedByte();
            }
            c.ten = r.readUTF();
            c.chuTinhCach = r.readUTF();
            c.tenKeo = r.readUTF();
            c.chuChuyen = r.readUTF();
            v.addElement(c);
        }
        datHop(soCon, suc, theo, giu, v);
        if (Canvas.currentScreen != ManHopPc.me || ManHopPc.me == null) {
            if (ManTraoDoiPokemon.me == null || Canvas.currentScreen != ManTraoDoiPokemon.me) {
                ManHopPc.mo(false);
            }
        }
    }

    public static void datHop(int soCon, int suc, int theo, int giu, Vector v) {
        HOP = v;
        hopSoCon = soCon;
        hopSucChua = suc;
        hopTheo = theo;
        hopGiu = giu;
        BatPokemon.soCon = soCon;
        BatPokemon.sucChua = suc;
        coHop = true;
    }

    public static Con conHop(int id) {
        for (int i = 0; i < HOP.size(); i++) {
            Con c = (Con) HOP.elementAt(i);
            if (c.id == id) {
                return c;
            }
        }
        return null;
    }

    public static void xinHop() {
        gui(C_XIN_HOP, -1, -1);
    }

    public static void hanhDong(int hd, int id) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(C_HANH_DONG);
            m.writer().writeByte(hd);
            m.writer().writeInt(id);
            GameService.gI().session.sendMessage(m);
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    // ---------------------------------------------------------------- 0.9.8 thông tin loài (Pokédex, gói kiểu 5)

    public static final class Loai {
        public int dex, doHiem, tiLe, truoc, sau, capTienHoa, soBat, soHa;
        public boolean coDong, tienHoaKhiDoi;
        public String ten = "", tenDoHiem = "", tenTruoc = "", tenSau = "";
        public String[] noiSong = new String[0];
        public int[] goc;
    }

    public static final Hashtable LOAI = new Hashtable();

    public static Loai loai(int dex) {
        return (Loai) LOAI.get(new Integer(dex));
    }

    public static void xinLoai(int dex) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(C_XIN_LOAI);
            m.writer().writeShort(dex);
            GameService.gI().session.sendMessage(m);
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    static void docLoai(DataInputStream r) throws java.io.IOException {
        Loai l = new Loai();
        l.dex = r.readShort();
        l.ten = r.readUTF();
        l.coDong = r.readByte() != 0;
        l.doHiem = r.readUnsignedByte();
        l.tenDoHiem = r.readUTF();
        l.tiLe = r.readShort();
        l.truoc = r.readShort();
        l.sau = r.readShort();
        l.capTienHoa = r.readUnsignedByte();
        l.tienHoaKhiDoi = r.readByte() != 0;
        l.tenTruoc = r.readUTF();
        l.tenSau = r.readUTF();
        l.soBat = r.readShort();
        l.soHa = r.readShort();
        int n = r.readUnsignedByte();
        l.noiSong = new String[n];
        for (int i = 0; i < n; i++) {
            l.noiSong[i] = r.readUTF();
        }
        int ng = r.readUnsignedByte();
        if (ng == 6) {
            l.goc = new int[6];
            for (int i = 0; i < 6; i++) {
                l.goc[i] = r.readUnsignedByte();
            }
        }
        LOAI.put(new Integer(l.dex), l);
    }

    // ---------------------------------------------------------------- 0.9.8 trao đổi Pokémon (gói kiểu 6)

    /** 0 không có, 2 đang trao đổi, 3 vừa xong. */
    public static int tdTrangThai;
    public static String tdDoiTen = "", tdGhiChu = "", tdXong = "";
    public static int tdBuocToi, tdBuocDoi, tdPhi;
    public static Vector tdToi = new Vector(), tdDoi = new Vector(), tdNhan = new Vector();

    public static void traoDoi(int lenh, int[] so) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(C_TRAO_DOI);
            m.writer().writeByte(lenh);
            if (lenh == T_MOI) {
                m.writer().writeShort(so[0]);
            } else if (lenh == T_TRA_LOI) {
                m.writer().writeByte(so[0]);
            } else if (lenh == T_DAT) {
                m.writer().writeByte(so.length);
                for (int i = 0; i < so.length; i++) {
                    m.writer().writeInt(so[i]);
                }
            }
            GameService.gI().session.sendMessage(m);
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    /** Menu người chơi "Trao đổi Pokémon" (chỉ hiện khi máy chủ có thu phục: đã nhận trạng thái). */
    public static boolean coTraoDoi() {
        return coTrangThai;
    }

    public static void moiTraoDoi(short idNguoi) {
        traoDoi(T_MOI, new int[]{idNguoi});
    }

    static Vector docDs(DataInputStream r) throws java.io.IOException {
        int n = r.readUnsignedByte();
        Vector v = new Vector();
        for (int i = 0; i < n; i++) {
            Con c = new Con();
            c.id = r.readInt();
            c.dex = r.readShort();
            c.cap = r.readUnsignedByte();
            c.shiny = r.readByte() != 0;
            c.sao = r.readUnsignedByte();
            c.ivPt = r.readUnsignedByte();
            c.sucManh = r.readShort();
            c.doHiem = r.readUnsignedByte();
            c.co = r.readByte() != 0 ? 8 : 0;
            c.ten = r.readUTF();
            v.addElement(c);
        }
        return v;
    }

    static void docTraoDoi(DataInputStream r) throws java.io.IOException {
        int tt = r.readUnsignedByte();
        if (tt == 0) {
            String lyDo = r.readUTF();
            tdTrangThai = 0;
            if (ManTraoDoiPokemon.me != null && Canvas.currentScreen == ManTraoDoiPokemon.me) {
                ManTraoDoiPokemon.me.dongMan();
            }
            if (ManHopPc.me != null && ManHopPc.me.chonDoi) {
                ManHopPc.me.chonDoi = false;
            }
            if (lyDo.length() > 0) {
                ThongBao.hien(lyDo, ThongBao.LOI);
            }
        } else if (tt == 1) {
            final short id = r.readShort();
            String ten = r.readUTF();
            r.readUnsignedByte();
            HopXacNhan.hoi("Trao đổi Pokémon", ten + " muốn trao đổi Pokémon với bạn. Hai bên chọn Pokémon trong Hộp PC, khoá rồi xác nhận.",
                    "Đồng ý", new IAction() {
                public void perform() {
                    traoDoi(T_TRA_LOI, new int[]{1});
                }
            }, new IAction() {
                public void perform() {
                    traoDoi(T_TRA_LOI, new int[]{0});
                }
            });
        } else if (tt == 2) {
            tdDoiTen = r.readUTF();
            tdBuocToi = r.readUnsignedByte();
            tdBuocDoi = r.readUnsignedByte();
            tdToi = docDs(r);
            tdDoi = docDs(r);
            tdPhi = r.readInt();
            tdGhiChu = r.readUTF();
            tdTrangThai = 2;
            ManTraoDoiPokemon.mo();
        } else if (tt == 3) {
            tdXong = r.readUTF();
            tdNhan = docDs(r);
            tdTrangThai = 3;
            ManTraoDoiPokemon.mo();
        }
    }

    static void datBong(int[] tt, String[] ten, int[] so) {
        int cu = bongChon < bongThuTu.length ? bongThuTu[bongChon] : -1;
        bongThuTu = tt;
        bongTen = ten;
        bongSo = so;
        bongChon = 0;
        for (int i = 0; i < tt.length; i++) {
            if (tt[i] == cu) {
                bongChon = i;
            }
        }
        // bóng đang chọn hết thì tự chuyển sang loại còn
        if (bongChon < so.length && so[bongChon] <= 0) {
            for (int i = 0; i < so.length; i++) {
                if (so[i] > 0) {
                    bongChon = i;
                    break;
                }
            }
        }
    }

    static void ketQua(short charId, short idQuai, int thuTu, int kq, int lac, int dex, boolean shiny) {
        Nem n = null;
        for (int i = NEM.size() - 1; i >= 0; i--) {
            Nem x = (Nem) NEM.elementAt(i);
            if (x.charId == charId && x.idQuai == idQuai && x.kq < 0) {
                n = x;
                break;
            }
        }
        if (n == null) {
            if (kq == 3) {
                return;   // bị từ chối, không có hiệu ứng để huỷ
            }
            n = batDau(charId, idQuai, thuTu, Canvas.gameScr != null && Canvas.gameScr.mainChar != null && Canvas.gameScr.mainChar.ID == charId);
        }
        n.kq = kq;
        n.lac = kq == 3 ? 0 : lac;
        n.dex = dex;
        n.shiny = shiny;
        n.tKq = System.currentTimeMillis();
        if (kq == 1 && dex > 0) {
            Integer k = new Integer(dex);
            Object o = DEX.get(k);
            int co = (o == null ? 0 : ((Integer) o).intValue()) | 2 | (shiny ? 4 : 0);
            if ((o == null || (((Integer) o).intValue() & 2) == 0) && n.cuaToi) {
                soDaBat++;
            }
            if (n.cuaToi) {
                DEX.put(k, new Integer(co));
            }
        }
    }

    // ---------------------------------------------------------------- vẽ: nút dưới khung mục tiêu

    /** Pokémon hoang dã ném bóng được (theo dữ liệu client; máy chủ kiểm lại). */
    public static boolean nemDuoc(Actor a) {
        if (!(a instanceof Monster) || a.catagory != Actor.CAT_MONSTER) {
            return false;
        }
        Monster m = (Monster) a;
        if (m.hp <= 0 || m.maxhp <= 0 || m.maxhp >= 10000000 || m.idBoss != -1) {
            return false;
        }
        if (!MonsterTemplate.laPokemon(m.monster_type) || HePoke.laBoss(m.monster_type)) {
            return false;
        }
        return (long) m.hp * 100L <= (long) m.maxhp * nguongHp;
    }

    /** Vùng nút vừa vẽ {x, y, w, h} (kiểm thử). */
    public static final int[] NUT = new int[4];
    public static int demVeNut;

    /**
     * GameScr.paintFocusActorInfo: dưới khung mục tiêu (x0, cao H, rộng W, đã translate). Không vẽ gì khi mục tiêu
     * không ném được.
     */
    public static void veNut(Graphics g, Actor muc, int x0, int H, int W) {
        if (!nemDuoc(muc)) {
            return;
        }
        if (!coTrangThai) {
            xinTrangThai();
            return;
        }
        if (bongThuTu.length == 0) {
            return;
        }
        final LiveActor quai = (LiveActor) muc;
        int i = Math.max(0, Math.min(bongThuTu.length - 1, bongChon));
        boolean con = bongSo[i] > 0;
        int h = Math.max(Bang.co(26), VungCham.MIN > 0 ? Math.min(VungCham.MIN, 40) : 0);
        int r = h / 2 - 3;
        int wDoi = bongThuTu.length > 1 ? Math.max(h, ChuDe.rong("Đổi", ChuDe.PHU) + 14) : 0;
        String chu = con ? "Ném " + bongTen[i] + " ×" + bongSo[i] : "Hết " + bongTen[i];
        int w = Math.min(W, ChuDe.rong(chu, ChuDe.NHAN) + 2 * r + 22 + wDoi);
        int x = x0 + W - w, y = H + 3;
        boolean an = Bang.dangAn(g, x, y, w - wDoi, h);
        GiaoDien.khoi(g, x, y + 1, w, h, h / 2, ChuDe.BONG);
        GiaoDien.khoiDoc(g, x, y, w, h, h / 2, con ? (an ? ChuDe.DO_TOI : 0xF7735F) : 0xCDD5DC, con ? ChuDe.DO : 0xC3CCD4);
        GiaoDien.pokeBall(g, x + 6 + r, y + h / 2, r, (int) ((System.currentTimeMillis() / 6) % 40) - 20);
        ChuDe.chuGiua(g, chu, x + 2 * r + 12, y, h, ChuDe.NHAN, 0xFFFFFF, 0);
        VungCham.them(g, x, y, w - wDoi, h, new IAction() {
            public void perform() {
                nem(quai);
            }
        }, "nem-bong");
        if (wDoi > 0) {
            int xd = x + w - wDoi;
            g.setColor(0xFFFFFF);
            g.drawLine(xd, y + 4, xd, y + h - 4);
            ChuDe.chuGiua(g, "Đổi", xd + wDoi / 2, y, h, ChuDe.PHU, 0xFFFFFF, 2);
            VungCham.them(g, xd, y, wDoi, h, new IAction() {
                public void perform() {
                    bongChon = (bongChon + 1) % Math.max(1, bongThuTu.length);
                    AmThanh.phat(AmThanh.CLICK);
                }
            }, "doi-bong");
        }
        NUT[0] = x + g.getTranslateX();
        NUT[1] = y + g.getTranslateY();
        NUT[2] = w;
        NUT[3] = h;
        demVeNut++;
    }

    // ---------------------------------------------------------------- vẽ: hiệu ứng ném (lớp giao diện, toạ độ thế giới -> màn)

    /** GameScr.paint (sau lớp thế giới). */
    public static void veHieuUng(Graphics g) {
        if (NEM.size() == 0) {
            return;
        }
        NhanTheGioi.tinhTiLe();
        long bay = System.currentTimeMillis();
        int z = NhanTheGioi.z();
        int rBong = Math.max(4, 5 * z / 1024);
        for (int k = NEM.size() - 1; k >= 0; k--) {
            Nem n = (Nem) NEM.elementAt(k);
            long t = bay - n.t0;
            // vị trí đích theo quái (nếu còn)
            Actor q = quai(n.idQuai);
            if (q != null && t < BAY_MS) {
                n.x1 = q.x;
                n.y1 = q.y - 6;
            }
            if (n.kq == 3 || (n.kq < 0 && t > CHO_TOI_DA_MS)) {
                NEM.removeElementAt(k);   // bị từ chối / mất gói: bỏ hiệu ứng
                continue;
            }
            int sx0 = NhanTheGioi.sx(n.x0), sy0 = NhanTheGioi.sy(n.y0);
            int sx1 = NhanTheGioi.sx(n.x1), sy1 = NhanTheGioi.sy(n.y1);
            if (t < BAY_MS) {
                // bay theo đường cong
                int p = (int) (t * 1024 / BAY_MS);
                int bx = sx0 + (sx1 - sx0) * p / 1024;
                int by = sy0 + (sy1 - sy0) * p / 1024 - (int) ((long) 4 * 30 * z / 1024 * p / 1024 * (1024 - p) / 1024);
                GiaoDien.pokeBall(g, bx, by, rBong, (int) (t / 2 % 360));
                continue;
            }
            long tSau = n.kq < 0 ? 0 : bay - Math.max(n.tKq, n.t0 + BAY_MS);
            int goc = 0;
            if (n.kq < 0) {
                goc = (int) (((bay / 90) % 2) * 6 - 3);   // chờ máy chủ: rung nhẹ
            } else if (tSau < n.lac * LAC_MS) {
                long pha = tSau % LAC_MS;
                goc = pha < LAC_MS / 2 ? (int) (pha * 50 / LAC_MS) - 12 : (int) ((LAC_MS - pha) * 50 / LAC_MS) - 12;
            } else {
                long d = tSau - n.lac * LAC_MS;
                if (d > KET_MS) {
                    NEM.removeElementAt(k);
                    continue;
                }
                if (n.kq == 1) {
                    // bắt được: bóng đứng yên + sao xoay + chữ
                    GiaoDien.pokeBall(g, sx1, sy1, rBong + 1, 0);
                    int rs = (int) (rBong * 2 + d * rBong / 300);
                    g.setColor(ChuDe.VANG);
                    for (int a = 0; a < 5; a++) {
                        int cx = sx1 + SAO_X[a] * rs / 1024, cy = sy1 - SAO_Y[a] * rs / 1024;
                        g.fillArc(cx - 2, cy - 2, 5, 5, 0, 360);
                    }
                    if (d < 40 && n.cuaToi) {
                        AmThanh.phat(AmThanh.LEN_CAP);
                    }
                } else {
                    // thoát / bỏ chạy: hai nửa bóng văng ra
                    int dd = (int) (d * rBong * 3 / KET_MS);
                    g.setColor(ChuDe.DO);
                    g.fillArc(sx1 - rBong - dd, sy1 - rBong - dd / 2, 2 * rBong, 2 * rBong, 0, 180);
                    g.setColor(0xFFFFFF);
                    g.fillArc(sx1 - rBong + dd, sy1 - rBong + dd / 2, 2 * rBong, 2 * rBong, 180, 180);
                    if (d < 40 && n.cuaToi) {
                        AmThanh.phat(AmThanh.TRUOT);
                    }
                    if (n.kq == 2) {
                        g.setColor(0xD8DEE6);
                        int rk = rBong + dd;
                        g.fillArc(sx1 - rk, sy1 - rk - dd, 2 * rk, 2 * rk, 0, 360);
                    }
                }
                continue;
            }
            GiaoDien.pokeBall(g, sx1, sy1, rBong, goc);
        }
    }

    // ---------------------------------------------------------------- bạn đồng hành (chỉ để nhìn)

    /**
     * Char.paint: Pokémon trong Hộp PC đang đi theo, vẽ phía sau người chơi, đuổi theo mượt (không có chỉ số). Bỏ qua khi
     * người chơi có pet thật (pet đã đi theo) hoặc thiếu hình.
     */
    public static void veBanDongHanh(Graphics g, Char c, int tick) {
        if (c == null || c.pokeBanDex <= 0 || c.petCanh != null || c.state == 3 || !PokeBody.coSprite(c.pokeBanDex, c.pokeBanShiny)) {
            return;
        }
        int lech = c.dir == 2 || c.dir == 3 ? 26 : 22;
        int dx = c.dir == Char.LEFT ? lech : c.dir == Char.RIGHT ? -lech : 0;
        int dy = c.dir == Char.UP ? 18 : c.dir == Char.DOWN ? -14 : -4;
        int mx = c.x + dx, my = c.y + dy;
        if (c.banX == Short.MIN_VALUE || Math.abs(c.banX - mx) > 200 || Math.abs(c.banY - my) > 200) {
            c.banX = (short) mx;
            c.banY = (short) my;
        }
        int ox = c.banX, oy = c.banY;
        c.banX = (short) (c.banX + (mx - c.banX) / 4);
        c.banY = (short) (c.banY + (my - c.banY) / 4);
        boolean di = Math.abs(ox - c.banX) + Math.abs(oy - c.banY) > 1;
        g.drawImage(game.render.Res.imgShadowPet != null ? game.render.Res.imgShadowPet : game.render.Res.imgShadow, c.banX, c.banY, 3);
        PokeBody.paint(g, c.pokeBanDex, c.pokeBanShiny, c.banX, c.banY, c.dir, di ? PokeDuLieu.CHAY : PokeDuLieu.DUNG, tick, 60);
    }

    /** Gọi khi vào bản đồ mới: xoá hiệu ứng cũ, xin lại trạng thái lần tới cần. */
    public static void doiMap() {
        NEM.removeAllElements();
        xinLuc = 0;
        xinTrangThai();   // 0.9.8: báo máy chủ client có Hộp PC / trao đổi (menu người chơi hiện "Trao đổi Pokémon")
    }
}
