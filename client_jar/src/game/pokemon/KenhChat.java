package game.pokemon;

import game.model.ChatInfo;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.io.DataInputStream;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import network.Message;

/**
 * KÊNH CHAT (0.9.8, bản 908) - kho phía client: lịch sử từng kênh (tối đa 50 dòng, trong bộ nhớ), số tin chưa đọc,
 * hội thoại riêng theo từng người, bạn bè + trạng thái online, danh sách chặn, cấu hình kênh của máy chủ, giãn cách
 * kênh Thế giới, và dải chat trên HUD (GameScr.chatHistory: các kênh đang bật "Hiện trên HUD").
 *
 * <pre>
 * -124 S->C (HTBB com.htbb.pokemon.KenhChatLogic):
 *   byte 0 cấu hình: byte phiên bản, byte n, n x (byte id, UTF tên, byte gửi được, short cấp, int xu, short giây chờ),
 *                    short độ dài tối đa
 *   byte 1 tin: byte kênh, byte cờ, long lúc, short id gửi, UTF tên gửi, UTF đối phương, UTF nội dung, UTF nhãn
 *   byte 2 bạn bè: short n, n x (UTF tên, byte online, short cấp)     byte 3 chặn: short n, n x UTF tên
 *   byte 4 kết quả: byte kênh, byte mã, short giây chờ, UTF lý do      byte 5 bạn online: UTF tên, byte online
 *   byte 6 id theo tên: byte việc, UTF tên, short id, byte cùng bản đồ
 * -124 C->S: byte 0 xin lại, 1 gửi (byte kênh, UTF đích, UTF nội dung), 2 chặn (byte bật, UTF tên), 3 xin bạn bè,
 *   6 tra id (byte việc, UTF tên)
 * </pre>
 * Máy chủ cũ (không gửi gói cấu hình): bảng chat vẫn chạy bằng gói cũ - bản đồ 27, bang hội -18, tin riêng -5; kênh
 * Thế giới / Đội báo "máy chủ chưa hỗ trợ".
 */
public final class KenhChat {
    public static final int LENH = -124;
    public static final int BAN_DO = 0, THE_GIOI = 1, BANG = 2, DOI = 3, RIENG = 4, HE_THONG = 5, SO_KENH = 6;
    public static final int CO_CUA_MINH = 1, CO_HOP_THU = 2, CO_LICH_SU = 4, CO_LOA = 8, CO_GM = 16;
    public static final int KQ_DA_GUI = 0, KQ_LOI = 1, KQ_CHO = 2, KQ_HOP_THU = 3;
    public static final int VIEC_MOI_DOI = 0, VIEC_KET_BAN = 1, VIEC_XEM = 2;
    /** tối đa dòng mỗi kênh / mỗi hội thoại */
    public static final int TOI_DA = 50;
    /** nhãn ngắn trên dải HUD */
    public static final String[] NHAN_HUD = {"[BĐ]", "[TG]", "[Đội]", "[Nhóm]", "[Mật]", "[HT]"};

    public static String[] tenKenh = {"Bản đồ", "Thế giới", "Đội", "Nhóm", "Bạn bè", "Hệ thống"};
    /** máy chủ có kênh chat 0.9.8 (đã nhận gói cấu hình) */
    public static boolean coMayChu;
    public static boolean[] guiDuoc = {true, false, true, false, true, false};
    public static int[] capKenh = new int[SO_KENH];
    public static int[] xuKenh = new int[SO_KENH];
    public static int[] giayKenh = new int[SO_KENH];
    public static int doDai = 80;

    /** Một dòng chat. */
    public static final class Dong {
        public int kenh, co;
        public long luc;
        public short id;
        public String ten, doiPhuong, noiDung, nhan, gio;

        public boolean cuaMinh() {
            return (co & CO_CUA_MINH) != 0;
        }
    }

    /** Một người trong tab Bạn bè (bạn bè + người từng nhắn). */
    public static final class Ban {
        public String ten;
        public boolean online, laBan;
        public int cap;
        public int chuaDoc;
        public Vector tin = new Vector();
        public long lucCuoi;
    }

    static final Vector[] KENH = new Vector[SO_KENH];
    public static final int[] chuaDoc = new int[SO_KENH];
    /** khoá tên thường -> Ban */
    static final Hashtable BAN = new Hashtable();
    /** thứ tự tab Bạn bè (Ban) */
    public static final Vector dsBan = new Vector();
    /** tên chặn (chữ thường) */
    public static final Vector chan = new Vector();

    /** kênh đang hiện trên HUD */
    public static final boolean[] hienHud = {true, true, true, true, true, true};
    /** tab mở lần cuối + người đang nhắn (tab Bạn bè) */
    public static int tabCuoi = BAN_DO;
    public static String dangNhan;
    /** bảng chat đang mở tab nào (-1 = đóng) - tin tới tab đang xem không tính chưa đọc */
    public static int dangXem = -1;
    /** Thế giới: hết chờ lúc (ms) */
    public static long choTheGioiDen;
    /** kết quả gửi gần nhất (hiện dưới ô nhập) */
    public static String baoCuoi;
    public static long lucBao;
    public static boolean baoLoi;
    /** việc chờ id theo tên (tra id) */
    static int viecCho = -1;
    static String tenCho;
    /** số lần nhận gói (kiểm thử) */
    public static int soGoi;
    /** đếm thay đổi (màn vẽ lại / vân tay) */
    public static int phienBan;

    static {
        for (int i = 0; i < SO_KENH; i++) {
            KENH[i] = new Vector();
        }
        napCaiDat();
    }

    private KenhChat() {
    }

    // ------------------------------------------------------------------ dữ liệu cho màn

    public static Vector dong(int kenh) {
        return kenh >= 0 && kenh < SO_KENH ? KENH[kenh] : new Vector();
    }

    public static Ban ban(String ten) {
        return ten == null ? null : (Ban) BAN.get(ten.toLowerCase());
    }

    static Ban banTao(String ten) {
        String k = ten.toLowerCase();
        Ban b = (Ban) BAN.get(k);
        if (b == null) {
            b = new Ban();
            b.ten = ten;
            BAN.put(k, b);
            dsBan.addElement(b);
        }
        return b;
    }

    public static boolean daChan(String ten) {
        return ten != null && chan.contains(ten.toLowerCase());
    }

    /** Tổng chưa đọc tin riêng (mọi người). */
    public static int chuaDocRieng() {
        int n = 0;
        for (int i = 0; i < dsBan.size(); i++) {
            n += ((Ban) dsBan.elementAt(i)).chuaDoc;
        }
        return n;
    }

    /** Chưa đọc của tab (tab Bạn bè = tổng tin riêng). */
    public static int chuaDocTab(int t) {
        return t == RIENG ? chuaDocRieng() : (t >= 0 && t < SO_KENH ? chuaDoc[t] : 0);
    }

    /** Chấm đỏ nút chat HUD: tin riêng / bang hội / đội chưa đọc (bản đồ, thế giới, hệ thống không chấm). */
    public static int chuaDocQuanTrong() {
        return chuaDocRieng() + chuaDoc[BANG] + chuaDoc[DOI];
    }

    public static void daDoc(int t) {
        if (t >= 0 && t < SO_KENH && chuaDoc[t] != 0) {
            chuaDoc[t] = 0;
            phienBan++;
        }
    }

    public static void daDocRieng(String ten) {
        Ban b = ban(ten);
        if (b != null && b.chuaDoc != 0) {
            b.chuaDoc = 0;
            phienBan++;
        }
    }

    /** Dsách Bạn bè xếp: chưa đọc trước, rồi online, rồi tin mới nhất. */
    public static Vector banXep() {
        Vector v = new Vector();
        for (int i = 0; i < dsBan.size(); i++) {
            Ban b = (Ban) dsBan.elementAt(i);
            int j = 0;
            while (j < v.size() && !truoc(b, (Ban) v.elementAt(j))) {
                j++;
            }
            v.insertElementAt(b, j);
        }
        return v;
    }

    static boolean truoc(Ban a, Ban b) {
        if ((a.chuaDoc > 0) != (b.chuaDoc > 0)) {
            return a.chuaDoc > 0;
        }
        if (a.online != b.online) {
            return a.online;
        }
        return a.lucCuoi > b.lucCuoi;
    }

    /** Giây còn chờ kênh Thế giới. */
    public static int giayCho() {
        long c = choTheGioiDen - System.currentTimeMillis();
        return c <= 0 ? 0 : (int) ((c + 999) / 1000);
    }

    static String tenMinh() {
        GameScr gs = Canvas.gameScr;
        return gs == null || gs.mainChar == null || gs.mainChar.name == null ? "" : gs.mainChar.name;
    }

    public static String gio(long luc) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new java.util.Date(luc));
            int h = c.get(Calendar.HOUR_OF_DAY), m = c.get(Calendar.MINUTE);
            return (h < 10 ? "0" : "") + h + ":" + (m < 10 ? "0" : "") + m;
        } catch (Throwable e) {
            return "";
        }
    }

    // ------------------------------------------------------------------ thêm dòng

    /** Thêm một dòng vào kênh (và hội thoại riêng), đếm chưa đọc, đẩy lên dải HUD. */
    public static void them(Dong d) {
        if (d == null || d.noiDung == null) {
            return;
        }
        if (d.gio == null) {
            d.gio = gio(d.luc);
        }
        if (!d.cuaMinh() && daChan(d.ten)) {
            return;
        }
        boolean cuDuLieu = (d.co & CO_LICH_SU) != 0;
        if (d.kenh == RIENG) {
            String nguoi = d.doiPhuong != null && d.doiPhuong.length() > 0 ? d.doiPhuong : d.ten;
            if (nguoi == null || nguoi.length() == 0) {
                return;
            }
            Ban b = banTao(nguoi);
            b.tin.addElement(d);
            while (b.tin.size() > TOI_DA) {
                b.tin.removeElementAt(0);
            }
            b.lucCuoi = Math.max(b.lucCuoi, d.luc);
            if (!d.cuaMinh() && !cuDuLieu && !(dangXem == RIENG && nguoi.equalsIgnoreCase(dangNhan))) {
                b.chuaDoc++;
            }
        } else if (d.kenh >= 0 && d.kenh < SO_KENH) {
            Vector v = KENH[d.kenh];
            v.addElement(d);
            while (v.size() > TOI_DA) {
                v.removeElementAt(0);
            }
            if (!d.cuaMinh() && !cuDuLieu && dangXem != d.kenh) {
                chuaDoc[d.kenh]++;
            }
        } else {
            return;
        }
        phienBan++;
        if (!cuDuLieu) {
            lenHud(d);
        }
    }

    public static Dong taoDong(int kenh, String ten, String noiDung, int co) {
        Dong d = new Dong();
        d.kenh = kenh;
        d.co = co;
        d.luc = System.currentTimeMillis();
        d.id = -1;
        d.ten = ten == null ? "" : ten;
        d.doiPhuong = "";
        d.noiDung = noiDung == null ? "" : noiDung;
        d.nhan = "";
        return d;
    }

    /** Dòng hệ thống (thông báo của máy chủ / game). */
    public static void heThong(String s) {
        if (s == null || s.trim().length() == 0) {
            return;
        }
        Dong d = taoDong(HE_THONG, "", s.trim(), 0);
        dangHud = true;
        try {
            them(d);
        } finally {
            dangHud = false;
        }
    }

    /** đang tự đẩy lên HUD (GameScr.addChat gọi ngược tuHud: không thêm lần hai) */
    static boolean dangHud;

    /** Đẩy dòng lên dải chat HUD (GameScr.chatHistory) nếu kênh đang bật. */
    static void lenHud(Dong d) {
        if (d.kenh < 0 || d.kenh >= SO_KENH || !hienHud[d.kenh]) {
            return;
        }
        GameScr gs = Canvas.gameScr;
        if (gs == null) {
            return;
        }
        ChatInfo ci = new ChatInfo();
        String ten = d.kenh == HE_THONG ? "" : (d.kenh == RIENG && d.cuaMinh() ? "tới " + d.doiPhuong : d.ten);
        ci.name = d.kenh == HE_THONG ? "" : NHAN_HUD[d.kenh] + " " + ten;
        ci.content = d.noiDung;
        ci.ID = MA_KENH;
        boolean cu = dangHud;
        dangHud = true;
        try {
            gs.addChat(ci);
        } finally {
            dangHud = cu;
        }
    }

    /** ChatInfo.ID của dòng do kênh chat đẩy lên HUD. */
    public static final int MA_KENH = 9124;

    /**
     * GameScr.addChat (dòng thông báo cục bộ: "Nhặt được...", "Không đủ MP"...): ghi vào kênh Hệ thống. Trả false = KHÔNG
     * hiện trên HUD (người chơi tắt Hệ thống trên HUD).
     */
    public static boolean tuHud(ChatInfo ci) {
        if (ci == null || dangHud || ci.ID == MA_KENH) {
            return true;
        }
        if (ci.name == null || ci.name.length() == 0) {
            dangHud = true;
            try {
                them(taoDong(HE_THONG, "", ci.content, 0));
            } finally {
                dangHud = false;
            }
            return hienHud[HE_THONG];
        }
        return true;
    }

    // ------------------------------------------------------------------ nguồn cũ (gói cũ)

    /** Tin bản đồ (gói 27) của người khác. */
    public static void banDo(short id, String ten, String noiDung) {
        Dong d = taoDong(BAN_DO, ten, noiDung, 0);
        d.id = id;
        them(d);
    }

    /** Tin bản đồ của mình (đã gửi gói 27). */
    public static void banDoMinh(String noiDung) {
        them(taoDong(BAN_DO, tenMinh(), noiDung, CO_CUA_MINH));
    }

    /** Gói -60 (loa / chữ chạy). Máy chủ mới gửi kèm -124 nên chỉ ghi khi máy chủ cũ. */
    public static void loaCu(String s) {
        if (coMayChu || s == null) {
            return;
        }
        int i = s.indexOf(": ");
        them(i > 0 ? taoDong(THE_GIOI, s.substring(0, i), s.substring(i + 2), CO_LOA) : taoDong(HE_THONG, "", s, 0));
    }

    /** Gói -18 (máy chủ cũ / client cũ): "(BC)tên: nội dung". */
    public static void bangCu(String s) {
        if (s == null) {
            return;
        }
        int i = s.indexOf(": ");
        String ten = i > 0 ? s.substring(0, i) : "";
        String nhan = "";
        if (ten.startsWith("(") && ten.indexOf(')') > 0) {
            nhan = ten.substring(0, ten.indexOf(')') + 1);
            ten = ten.substring(ten.indexOf(')') + 1);
        }
        Dong d = taoDong(BANG, ten, i > 0 ? s.substring(i + 2) : s, ten.equalsIgnoreCase(tenMinh()) ? CO_CUA_MINH : 0);
        d.nhan = nhan;
        them(d);
    }

    /** Gói -5 (máy chủ cũ): tin riêng từ ten. "Nhóm" = chat đội kiểu cũ. */
    public static void riengCu(String ten, String noiDung) {
        if ("Nhóm".equals(ten)) {
            int i = noiDung.indexOf(": ");
            them(taoDong(DOI, i > 0 ? noiDung.substring(0, i) : "", i > 0 ? noiDung.substring(i + 2) : noiDung, 0));
            return;
        }
        Dong d = taoDong(RIENG, ten, noiDung, 0);
        d.doiPhuong = ten;
        them(d);
    }

    // ------------------------------------------------------------------ mạng

    /** true = đã xử lý (lệnh -124). Gọi ở đầu MessageHandler.onMessage. */
    public static boolean nhan(Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        try {
            doc(msg.reader());
        } catch (Throwable e) {
            // gói hỏng
        }
        return true;
    }

    public static void doc(DataInputStream r) throws java.io.IOException {
        soGoi++;
        int loai = r.readByte();
        switch (loai) {
            case 0: {
                r.readByte();
                int n = r.readByte();
                for (int i = 0; i < n; i++) {
                    int id = r.readByte();
                    String ten = r.readUTF();
                    boolean gui = r.readByte() != 0;
                    int cap = r.readShort();
                    int xu = r.readInt();
                    int giay = r.readShort();
                    if (id >= 0 && id < SO_KENH) {
                        tenKenh[id] = ten;
                        guiDuoc[id] = gui;
                        capKenh[id] = cap;
                        xuKenh[id] = xu;
                        giayKenh[id] = giay;
                    }
                }
                doDai = r.readShort();
                coMayChu = true;
                break;
            }
            case 1: {
                Dong d = new Dong();
                d.kenh = r.readByte();
                d.co = r.readByte();
                d.luc = r.readLong();
                d.id = r.readShort();
                d.ten = r.readUTF();
                d.doiPhuong = r.readUTF();
                d.noiDung = r.readUTF();
                d.nhan = r.readUTF();
                if (d.kenh == THE_GIOI && d.cuaMinh() && giayKenh[THE_GIOI] > 0 && (d.co & CO_LICH_SU) == 0) {
                    choTheGioiDen = Math.max(choTheGioiDen, System.currentTimeMillis() + giayKenh[THE_GIOI] * 1000L);
                }
                them(d);
                break;
            }
            case 2: {
                int n = r.readShort();
                for (int i = 0; i < dsBan.size(); i++) {
                    ((Ban) dsBan.elementAt(i)).laBan = false;
                }
                for (int i = 0; i < n; i++) {
                    Ban b = banTao(r.readUTF());
                    b.laBan = true;
                    b.online = r.readByte() != 0;
                    b.cap = r.readShort();
                }
                phienBan++;
                break;
            }
            case 3: {
                int n = r.readShort();
                chan.removeAllElements();
                for (int i = 0; i < n; i++) {
                    chan.addElement(r.readUTF().toLowerCase());
                }
                phienBan++;
                break;
            }
            case 4: {
                int kenh = r.readByte();
                int ma = r.readByte();
                int giay = r.readShort();
                String lyDo = r.readUTF();
                if (ma == KQ_CHO || (kenh == THE_GIOI && ma == KQ_DA_GUI && giay > 0)) {
                    choTheGioiDen = System.currentTimeMillis() + giay * 1000L;
                }
                if (lyDo.length() > 0) {
                    bao(lyDo, ma == KQ_LOI || ma == KQ_CHO);
                }
                break;
            }
            case 5: {
                String ten = r.readUTF();
                boolean on = r.readByte() != 0;
                Ban b = ban(ten);
                if (b != null) {
                    b.online = on;
                    phienBan++;
                }
                break;
            }
            case 6: {
                int viec = r.readByte();
                String ten = r.readUTF();
                short id = r.readShort();
                boolean cung = r.readByte() != 0;
                lamViec(viec, ten, id, cung);
                break;
            }
            case 7: {
                tenDoi = r.readUTF();
                doiOnline = r.readShort();
                doiTong = r.readShort();
                coTinDoi = true;
                if (tenDoi.length() == 0) {
                    KENH[BANG].removeAllElements();   // rời đội: không giữ tin đội cũ
                    chuaDoc[BANG] = 0;
                }
                phienBan++;
                break;
            }
            default:
                break;
        }
    }

    /** Đội (bang hội) của mình theo gói -124 loại 7: tên ("" = chưa vào đội), số đang online / tổng. */
    public static String tenDoi = "";
    public static int doiOnline, doiTong;
    public static boolean coTinDoi;

    /** Có đang ở trong đội (gói loại 7 nếu máy chủ đã gửi, không thì theo idClan). */
    public static boolean trongDoi() {
        if (coTinDoi) {
            return tenDoi != null && tenDoi.length() > 0;
        }
        return Canvas.gameScr != null && Canvas.gameScr.mainChar != null && Canvas.gameScr.mainChar.idClan != -1;
    }

    /** Nút "Xoá" của tab: xoá ngay tin đang hiện của tab (hội thoại riêng đang mở / mọi hội thoại ở danh sách Bạn bè). */
    public static void xoaTab(int t) {
        if (t == RIENG) {
            Ban mo = dangNhan == null ? null : ban(dangNhan);
            for (int i = 0; i < dsBan.size(); i++) {
                Ban b = (Ban) dsBan.elementAt(i);
                if (mo == null || b == mo) {
                    b.tin.removeAllElements();
                    b.chuaDoc = 0;
                }
            }
        } else if (t >= 0 && t < SO_KENH) {
            KENH[t].removeAllElements();
            chuaDoc[t] = 0;
        }
        phienBan++;
    }

    static void bao(String s, boolean loi) {
        baoCuoi = s;
        baoLoi = loi;
        lucBao = System.currentTimeMillis();
        if (Canvas.currentScreen != ManChat.me || ManChat.me == null) {
            if (loi) {
                DungDo.loi(s);
            } else {
                DungDo.bao(s, DungDo.TIN);
            }
        }
    }

    static void guiGoi(int op, int kenh, String a, String b) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(op);
            if (op == 1) {
                m.writer().writeByte(kenh);
                m.writer().writeUTF(a);
                m.writer().writeUTF(b);
            } else if (op == 2 || op == 6) {
                m.writer().writeByte(kenh);
                m.writer().writeUTF(a);
            }
            GameService.gI().session.sendMessage(m);
            m.cleanup();
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    /**
     * Gửi một tin trên kênh (bản đồ = gói 27 như ô chat cũ: lệnh admin, đáp án nhiệm vụ... vẫn chạy). Trả null = đã
     * gửi; khác = lý do không gửi (hiện dưới ô nhập).
     */
    public static String gui(int kenh, String dich, String text) {
        String t = GameService.chuChat(text);
        if (t == null) {
            return "";
        }
        if (t.length() > doDai) {
            t = t.substring(0, doDai);
        }
        switch (kenh) {
            case BAN_DO: {
                GameScr gs = Canvas.gameScr;
                if (gs != null) {
                    gs.chatTuBang(t);
                } else {
                    GameService.gI().chat(t);
                }
                return null;
            }
            case HE_THONG:
                return "Kênh Hệ thống chỉ để đọc.";
            case THE_GIOI:
                if (!coMayChu) {
                    return "Máy chủ chưa hỗ trợ kênh Thế giới (dùng Loa trong hành trang).";
                }
                if (giayCho() > 0) {
                    return "Chờ " + giayCho() + " giây nữa.";
                }
                guiGoi(1, kenh, "", t);
                return null;
            case DOI:
                if (game.model.Char.party == null || game.model.Char.party.size() == 0) {
                    return "Bạn chưa vào nhóm.";
                }
                if (!coMayChu) {
                    return "Máy chủ chưa hỗ trợ chat Nhóm.";
                }
                guiGoi(1, kenh, "", t);
                return null;
            case BANG:
                if (!coMayChu) {
                    GameService.gI().chatToClan(t);
                    return null;
                }
                guiGoi(1, kenh, "", t);
                return null;
            case RIENG:
                if (dich == null || dich.length() == 0) {
                    return "Chọn người để nhắn.";
                }
                if (!coMayChu) {
                    GameService.gI().sendMsgPrivate(dich, t);
                    Dong d = taoDong(RIENG, tenMinh(), t, CO_CUA_MINH);
                    d.doiPhuong = dich;
                    them(d);
                    return null;
                }
                banTao(dich);
                guiGoi(1, kenh, dich, t);
                return null;
            default:
                return "";
        }
    }

    public static void datChan(String ten, boolean bat) {
        if (ten == null || ten.length() == 0) {
            return;
        }
        String k = ten.toLowerCase();
        if (bat && !chan.contains(k)) {
            chan.addElement(k);
        } else if (!bat) {
            chan.removeElement(k);
        }
        phienBan++;
        if (coMayChu) {
            guiGoi(2, bat ? 1 : 0, ten, null);
        }
    }

    public static void xinBan() {
        if (coMayChu) {
            guiGoi(3, 0, null, null);
        }
    }

    /**
     * Việc theo tên người gửi (menu tên): có id trong dòng và người đó đang thấy trên bản đồ thì làm ngay; không thì
     * hỏi máy chủ id hiện tại (gói 6) rồi làm.
     */
    public static void viecTheoTen(int viec, String ten, short id) {
        if (coMayChu) {
            viecCho = viec;
            tenCho = ten;
            guiGoi(6, viec, ten, null);
            return;
        }
        lamViec(viec, ten, id, true);
    }

    static void lamViec(int viec, String ten, short id, boolean cungBanDo) {
        viecCho = -1;
        if (id < 0) {
            bao(ten + " đang offline.", true);
            return;
        }
        try {
            if (viec == VIEC_MOI_DOI) {
                if (!cungBanDo) {
                    bao("Chỉ mời được người cùng bản đồ.", true);
                    return;
                }
                GameService.gI().invite2Party(id, 0);
                bao("Đã gửi lời mời vào nhóm tới " + ten + ".", false);
            } else if (viec == VIEC_KET_BAN) {
                GameService.gI().sendInfoAddFriend(id, 0);
                bao("Đã gửi lời mời kết bạn tới " + ten + ".", false);
            } else if (viec == VIEC_XEM) {
                Canvas.startWaitDlg();
                if (Canvas.gameScr != null) {
                    Canvas.gameScr.saveIDViewInfoAnimal = id;
                }
                GameService.gI().requestSomeOneInfo(id, (byte) 0);
            }
        } catch (Throwable e) {
            // mất kết nối
        }
    }

    // ------------------------------------------------------------------ cài đặt (RMS)

    static final String RMS = "htbbChat";

    public static void luuCaiDat() {
        try {
            byte[] b = new byte[SO_KENH + 1];
            for (int i = 0; i < SO_KENH; i++) {
                b[i] = (byte) (hienHud[i] ? 1 : 0);
            }
            b[SO_KENH] = (byte) tabCuoi;
            game.render.screen.RMS.saveRMS(RMS, b);
        } catch (Throwable e) {
            // máy không có RMS
        }
    }

    static void napCaiDat() {
        try {
            byte[] b = game.render.screen.RMS.loadRMS(RMS);
            if (b != null && b.length >= SO_KENH) {
                for (int i = 0; i < SO_KENH; i++) {
                    hienHud[i] = b[i] != 0;
                }
                if (b.length > SO_KENH && b[SO_KENH] >= 0 && b[SO_KENH] < SO_KENH) {
                    tabCuoi = b[SO_KENH];
                }
            }
        } catch (Throwable e) {
            // mặc định
        }
    }

    /** Vào game (GameService.selectChar) / kiểm thử: xoá mọi lịch sử - chat chỉ trong phiên (chủ game, kiểu Làng Lá). */
    public static void xoaHet() {
        for (int i = 0; i < SO_KENH; i++) {
            KENH[i].removeAllElements();
            chuaDoc[i] = 0;
        }
        BAN.clear();
        dsBan.removeAllElements();
        chan.removeAllElements();
        dangNhan = null;
        choTheGioiDen = 0;
        baoCuoi = null;
        tenDoi = "";
        doiOnline = doiTong = 0;
        coTinDoi = false;
        phienBan++;
    }
}
