package game.render.screen;

import game.model.Actor;
import game.model.Char;
import game.model.Dropable;
import game.model.LiveActor;
import game.model.MainChar;
import game.model.Tilemap;
import game.model.skill.SkillManager;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Util;
import java.io.DataInputStream;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * TỰ ĐỘNG ĐÁNH chính thức (thay mod NQSH: Auto up / PK / Vận may / Lên thành / Giao hàng tự động đã bỏ).
 * MÁY CHỦ quyết định: client chỉ chạy khi server trả "đang bật" (lệnh -100, HTBB com.htbb.pokemon.TuDong /
 * real.TuDongGame); server đếm giờ mỗi ngày, chặn nhịp thao tác như người thật, dừng khi PvP / sàn đấu / Gym /
 * boss / đổi map / ra khỏi vùng / hết giờ. Server cũ không trả lời -100 thì không bao giờ chạy.
 *
 * Mọi thao tác đi qua ĐÚNG hàm của game như người chơi bấm tay: GameScr.doUseSkillToFocusActor (tự đi tới tầm,
 * kiểm MP / hồi chiêu, có hoạt ảnh), GameScr.doUsePotion, GameService.getDropableFromGround. Không lướt, không
 * bỏ hoạt ảnh, không đổi tầm chiêu.
 *
 * Lệnh -100 client -> server: byte 0 xin trạng thái / 1 bật (+ short bán kính px) / 2 tắt / 3 nhịp (20 giây).
 * Server -> client: byte 0, byte bật, int giây còn lại hôm nay, int giây tối đa hôm nay, short bán kính tối đa,
 * byte cờ (1 tính năng bật, 2 cho tự uống thuốc, 4 dừng khi PvP, 8 chỉ bãi luyện tập), UTF thông báo.
 *
 * Bảng cài đặt (giao diện) ở Nen.moBangTuDong: bản jar/APK = BangTuDong (kiểu Pokémon), bản khác = menu.
 */
public final class TuDong {
    public static final byte LENH = -100;
    static final int CO_TINH_NANG = 1, CO_THUOC = 2, CO_TAT_PVP = 4, CO_CHI_MAP = 8;
    private static final String RMS_TEN = "htbbtd";

    /** Bình HP / MP mạnh -> yếu (id loại thuốc của game, y GameScr.doUsePotion). */
    private static final int[] BINH_HP = new int[]{94, 93, 22, 21, 3, 2, 1};
    private static final int[] BINH_MP = new int[]{96, 95, 24, 23, 6, 5, 4};
    /** Loại skill buff theo dòng (không dùng để đánh). */
    private static final byte[][] SKILL_BUFF = new byte[][]{{4, 5}, {4, 5}, {4, 5, 6, 7}, {4, 5}, {4, 5}};

    // ---------------------------------------------------------------- trạng thái máy chủ
    /** Server đã trả lời ít nhất một lần (server có hỗ trợ -100). */
    static boolean coServer;
    static boolean svBat;
    static int svConLai;
    static int svToiDa;
    static int svBanKinh = 240;
    static int svCo = CO_TINH_NANG | CO_THUOC | CO_TAT_PVP | CO_CHI_MAP;
    static long svLuc;
    /** Câu server gửi kèm gần nhất (lý do dừng / từ chối). */
    static String thongBao = "";
    static long lucXin;

    // ---------------------------------------------------------------- đang chạy
    /** Client đang chạy (chỉ true khi server xác nhận). */
    public static boolean dang;
    /** Đã xin bật, chờ server. */
    static boolean choBat;
    static long lucChoBat;
    private static int neoX, neoY, neoMap = -1;
    private static Actor mucTieu, monNhat;
    private static long lucDanh, lucBinh, lucNhip, lucNhuong, lucKet, lucDoi;
    private static int ketX, ketY, lanKet;
    private static boolean giuPhim, hoiPhuc;
    static String trangThai = "";
    static int soHa;

    // ---------------------------------------------------------------- cài đặt (RMS)
    /** Thứ tự chiêu (loại skill, -1 = trống); trống hết = mọi chiêu đánh đã học (mạnh trước). */
    static final byte[] thuTu = new byte[]{-1, -1, -1, -1};
    /** Uống thuốc khi HP / MP dưới (%); 0 = tắt. */
    static int nguongHp = 50;
    static int nguongMp = 30;
    /** Nhặt đồ: 0 không, 1 thuốc + xu, 2 thêm đá / nguyên liệu, 3 tất cả. */
    static int nhat = 1;
    static final String[] TEN_NHAT = new String[]{"Không nhặt", "Thuốc + xu", "Thuốc, xu, đá", "Tất cả"};
    /** Hết thuốc mà HP thấp: về điểm bắt đầu đứng hồi phục tới 80% rồi đánh tiếp. */
    static boolean veHoiPhuc = true;
    /** Vùng đánh quanh điểm bắt đầu (ô, 1 ô = 16 px). */
    static int vungO = 10;
    static final int[] VUNG_O = new int[]{4, 7, 10, 15, 20};
    /**
     * Gục khi đang tự động: bật = cho máy chủ (hệ thống hồi sinh) tự dùng vật phẩm hồi sinh tại chỗ, tối đa
     * tu_dong.hoi_sinh_moi_gio lần / giờ, rồi đánh tiếp; tắt (mặc định) = dừng, về Trung tâm Pokémon như thường.
     */
    static boolean tuHoiSinh;
    private static long lucChet;
    private static boolean daNap;

    private TuDong() {
    }

    // ================================================================ mạng

    private static void gui(int loai, int banKinh) {
        try {
            Message m = new Message(LENH);
            m.writer().writeByte(loai);
            if (loai == 1) {
                m.writer().writeShort(banKinh);
                m.writer().writeByte(tuHoiSinh ? 1 : 0);
            }
            GameService.gI().session.sendMessage(m);
            m.cleanup();
        } catch (Throwable e) {
            // mất kết nối: lần sau
        }
    }

    /** Xin trạng thái (mở bảng / vào game). */
    public static void xinTrangThai() {
        lucXin = System.currentTimeMillis();
        gui(0, 0);
    }

    /** Gói -100 từ server (gọi ở đầu MessageHandler qua AutoGuard.goiGuard). true = đã xử lý. */
    public static boolean nhan(Message msg) {
        if (msg == null || msg.command != LENH) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            r.readByte();
            boolean bat = r.readByte() != 0;
            svConLai = r.readInt();
            svToiDa = r.readInt();
            svBanKinh = r.readShort();
            svCo = r.readByte();
            String tb = r.readUTF();
            coServer = true;
            svBat = bat;
            svLuc = System.currentTimeMillis();
            if (tb != null && tb.length() > 0) {
                thongBao = tb;
            }
            if (bat && choBat) {
                choBat = false;
                batThat();
            } else if (!bat) {
                boolean dangChay = dang || choBat;
                choBat = false;
                if (dang) {
                    dungTaiCho();
                }
                if (dangChay && tb != null && tb.length() > 0) {
                    game.render.Canvas.startOKDlg(tb);
                }
            }
        } catch (Throwable e) {
            // gói hỏng: bỏ
        }
        return true;
    }

    /** Giây còn lại hôm nay (ước theo lần server báo gần nhất). */
    static int conLai() {
        if (!coServer) {
            return 0;
        }
        int g = svConLai;
        if (dang) {
            g -= (int) ((System.currentTimeMillis() - svLuc) / 1000L);
        }
        return g < 0 ? 0 : g;
    }

    static String gio(int giay) {
        int h = giay / 3600;
        int p = giay % 3600 / 60;
        if (h > 0) {
            return new StringBuffer().append(h).append(" giờ ").append(p < 10 ? "0" : "").append(p).append(" phút").toString();
        }
        return new StringBuffer().append(p).append(" phút").toString();
    }

    static boolean choThuoc() {
        return (svCo & CO_THUOC) != 0;
    }

    // ================================================================ bật / tắt

    /** Nút Bắt đầu / Dừng. */
    public static void batTat() {
        if (dang || choBat) {
            tat(true);
            return;
        }
        bat();
    }

    static void bat() {
        nap();
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return;
        }
        if (gs.mainChar.hp <= 0) {
            thongBao = "Pokémon đang gục, hồi sinh rồi bật lại.";
            return;
        }
        if (coServer && (svCo & CO_TINH_NANG) == 0) {
            thongBao = "Tự động đánh đang tạm khoá trên máy chủ.";
            return;
        }
        thongBao = "";
        choBat = true;
        lucChoBat = System.currentTimeMillis();
        gui(1, banKinh());
    }

    /** Bán kính (px) xin server: cài đặt của người chơi, không quá mức server cho. */
    static int banKinh() {
        int px = vungO * 16;
        return svBanKinh > 0 && px > svBanKinh ? svBanKinh : px;
    }

    /** Server đã cho: bắt đầu tại chỗ đứng hiện tại. */
    private static void batThat() {
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return;
        }
        dang = true;
        neoMap = gs.map;
        neoX = gs.mainChar.x;
        neoY = gs.mainChar.y;
        mucTieu = null;
        monNhat = null;
        hoiPhuc = false;
        soHa = 0;
        lucNhip = System.currentTimeMillis() + 20000L;
        trangThai = "bắt đầu";
        // auto gốc của game (BangAuto) không chạy song song
        GameScr.autoFight = false;
        GameScr.saveAutoFight = false;
        GameScr.autoBomHMP = false;
    }

    /** Dừng (báo server nếu {@code baoServer}). */
    public static void tat(boolean baoServer) {
        boolean co = dang || choBat;
        choBat = false;
        dungTaiCho();
        if (co && baoServer) {
            gui(2, 0);
        }
    }

    private static void dungTaiCho() {
        dang = false;
        nha();
        mucTieu = null;
        monNhat = null;
        hoiPhuc = false;
        trangThai = "";
    }

    // ================================================================ vòng chính (mỗi khung hình)

    public static void tick() {
        long l = System.currentTimeMillis();
        if (choBat && l - lucChoBat > 6000L) {
            choBat = false;
            thongBao = coServer ? "Máy chủ không phản hồi, thử lại." : "Máy chủ chưa hỗ trợ Tự động đánh.";
        }
        if (!dang) {
            return;
        }
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return;
        }
        MainChar mc = gs.mainChar;
        if (l >= lucNhip) {
            lucNhip = l + 20000L;
            gui(3, 0);
        }
        if (mc.hp <= 0) {
            // tự hồi sinh: máy chủ quyết (luật hồi sinh + trần mỗi giờ); không thì nó tự dừng và báo
            if (tuHoiSinh) {
                if (lucChet == 0L) {
                    lucChet = l;
                }
                if (l - lucChet < 35000L) {
                    nha();
                    mucTieu = null;
                    trangThai = "chờ hồi sinh";
                    return;
                }
            }
            thongBao = "Pokémon đã gục - Tự động đánh dừng. Hồi sinh rồi bật lại.";
            tat(true);
            return;
        }
        lucChet = 0L;
        if (gs.map != neoMap) {
            thongBao = "Đã rời bãi luyện tập - Tự động đánh dừng.";
            tat(true);
            return;
        }
        if (Canvas.currentScreen != gs || Canvas.currentDialog != null || Canvas.menu.showMenu || gs.chatMode) {
            nha();
            trangThai = "tạm dừng";
            return;
        }
        if (nguoiBam()) {
            lucNhuong = l + 3000L;
            nha();
        }
        if (l < lucNhuong) {
            trangThai = "chờ bạn";
            return;
        }
        binh(gs, mc, l);
        if (hoiPhuc(gs, mc, l)) {
            return;
        }
        if (nhatDo(gs, mc, l)) {
            return;
        }
        danh(gs, mc, l);
    }

    /** Người chơi tự bấm phím di chuyển / chạm màn: nhường 3 giây. */
    private static boolean nguoiBam() {
        if (Canvas.keyPressed[2] || Canvas.keyPressed[4] || Canvas.keyPressed[6] || Canvas.keyPressed[8]) {
            return true;
        }
        return Canvas.isPointerDown && !Nen.trongNutNhanh(Canvas.px, Canvas.py);
    }

    // ---------------------------------------------------------------- thuốc

    private static void binh(GameScr gs, MainChar mc, long l) {
        if (l < lucBinh || !choThuoc()) {
            return;
        }
        if (nguongHp > 0 && mc.maxhp > 0 && (long) mc.hp * 100L < (long) mc.maxhp * nguongHp && uong(gs, mc, BINH_HP, l)) {
            lucBinh = l + 1500L;
            return;
        }
        if (nguongMp > 0 && mc.maxmp > 0 && (long) mc.mp * 100L < (long) mc.maxmp * nguongMp && uong(gs, mc, BINH_MP, l)) {
            lucBinh = l + 1500L;
        }
    }

    private static int coBinh(MainChar mc, int[] loai) {
        for (int i = 0; i < loai.length; ++i) {
            int n = loai[i];
            if (n < mc.potions.length && mc.potions[n] > 0) {
                return n;
            }
        }
        return -1;
    }

    private static boolean uong(GameScr gs, MainChar mc, int[] loai, long l) {
        int n = coBinh(mc, loai);
        if (n < 0) {
            return false;
        }
        if (MainChar.listPotion != null && n < MainChar.listPotion.length && MainChar.listPotion[n] != null && l - mc.potionLastTimeUse[n] < MainChar.listPotion[n].delay) {
            return false;
        }
        gs.doUsePotion(n);
        return true;
    }

    /** Về hồi phục: HP thấp mà không có / không được dùng thuốc -> về điểm đầu đứng nghỉ tới 80%. */
    private static boolean hoiPhuc(GameScr gs, MainChar mc, long l) {
        if (!veHoiPhuc || mc.maxhp <= 0) {
            hoiPhuc = false;
            return false;
        }
        int pt = (int) ((long) mc.hp * 100L / mc.maxhp);
        int nguong = nguongHp > 0 ? nguongHp : 40;
        boolean hetThuoc = !choThuoc() || nguongHp <= 0 || coBinh(mc, BINH_HP) < 0;
        if (!hoiPhuc && pt < nguong && hetThuoc) {
            hoiPhuc = true;
            mucTieu = null;
            gs.focusedActor = null;
        }
        if (hoiPhuc && pt >= 80) {
            hoiPhuc = false;
        }
        if (!hoiPhuc) {
            return false;
        }
        if (Util.distance(mc.x, mc.y, neoX, neoY) > 20) {
            diToi(mc, neoX, neoY, l);
            trangThai = "về hồi phục";
        } else {
            nha();
            trangThai = "nghỉ hồi phục " + pt + "%";
        }
        return true;
    }

    // ---------------------------------------------------------------- chọn quái + đánh

    static int vungPx() {
        return banKinh();
    }

    private static boolean quaiSong(GameScr gs, Actor a) {
        return a != null && a.catagory == 1 && !a.isMineral() && a.state != 8 && a.state != 3 && a.state != 5 && ((LiveActor) a).hp > 0 && gs.actors.contains(a);
    }

    private static boolean trongVung(Actor a) {
        return Util.distance(neoX, neoY, a.x, a.y) <= vungPx();
    }

    private static Actor chonQuai(GameScr gs, MainChar mc) {
        Actor best = null;
        int d = Integer.MAX_VALUE;
        for (int i = 0; i < gs.actors.size(); ++i) {
            Actor a = (Actor) gs.actors.elementAt(i);
            if (!quaiSong(gs, a) || !trongVung(a)) {
                continue;
            }
            int k = Util.distance(mc.x, mc.y, a.x, a.y);
            if (k < d) {
                d = k;
                best = a;
            }
        }
        return best;
    }

    private static void danh(GameScr gs, MainChar mc, long l) {
        if (mucTieu != null && !quaiSong(gs, mucTieu)) {
            if (((LiveActor) mucTieu).hp <= 0 || mucTieu.state == 8 || mucTieu.state == 3) {
                ++soHa;
            }
            mucTieu = null;
        }
        if (mucTieu != null && !trongVung(mucTieu)) {
            mucTieu = null;
        }
        // kẹt không tới được con này 4 giây: đổi con khác
        if (mucTieu != null && l - lucDoi > 4000L && Util.distance(mc.x, mc.y, mucTieu.x, mucTieu.y) > 60) {
            mucTieu = null;
        }
        if (mucTieu == null) {
            mucTieu = chonQuai(gs, mc);
            lucDoi = l;
        }
        if (mucTieu == null) {
            gs.focusedActor = null;
            if (Util.distance(mc.x, mc.y, neoX, neoY) > 24) {
                diToi(mc, neoX, neoY, l);
                trangThai = "về chỗ cũ";
            } else {
                nha();
                trangThai = "chờ Pokémon hoang dã";
            }
            return;
        }
        nha();
        gs.focusedActor = mucTieu;
        trangThai = "đang đánh";
        if (l < lucDanh || mc.isDoing || mc.state == Char.A_ATTACK) {
            return;
        }
        byte sk = chonChieu(mc, l);
        if (sk < 0) {
            return;
        }
        gs.doUseSkillToFocusActor(sk);
        lucDanh = l + 450L;
    }

    /** Chiêu theo thứ tự đã chọn (hồi xong, đủ MP); trống thì mọi chiêu đánh đã học (mạnh trước); không có thì 0. */
    static byte chonChieu(MainChar mc, long l) {
        byte[] ds = dsChieu(mc);
        for (int i = 0; i < ds.length; ++i) {
            byte s = ds[i];
            if (s < 0 || s >= Char.skillLevelLearnt.length || Char.skillLevelLearnt[s] <= 0 && s != 0) {
                continue;
            }
            if (l - mc.timeLastUseSkills[s] <= mc.coolDownSkill[s]) {
                continue;
            }
            if (SkillManager.getSkillMP(s, Char.skillLevelLearnt[s], mc.clazz) > mc.mp) {
                continue;
            }
            return s;
        }
        return -1;
    }

    /** Danh sách chiêu sẽ dùng theo thứ tự. */
    static byte[] dsChieu(MainChar mc) {
        int n = 0;
        for (int i = 0; i < thuTu.length; ++i) {
            if (thuTu[i] >= 0) {
                ++n;
            }
        }
        if (n > 0) {
            byte[] r = new byte[n];
            n = 0;
            for (int i = 0; i < thuTu.length; ++i) {
                if (thuTu[i] >= 0) {
                    r[n++] = thuTu[i];
                }
            }
            return r;
        }
        return chieuDanh(mc);
    }

    /** Mọi chiêu đánh đã học, mạnh trước (bỏ chiêu buff). */
    public static byte[] chieuDanh(MainChar mc) {
        int tong = mc == null ? 0 : mc.getnSkill();
        byte[] tam = new byte[tong];
        int n = 0;
        for (int i = tong - 1; i >= 0; --i) {
            if (laBuff(mc, i) || i >= Char.skillLevelLearnt.length || Char.skillLevelLearnt[i] <= 0 && i != 0) {
                continue;
            }
            tam[n++] = (byte) i;
        }
        byte[] r = new byte[n];
        System.arraycopy(tam, 0, r, 0, n);
        return r;
    }

    private static boolean laBuff(MainChar mc, int s) {
        byte[] b = SKILL_BUFF[Math.min(Math.max(0, mc.clazz), SKILL_BUFF.length - 1)];
        for (int i = 0; i < b.length; ++i) {
            if (b[i] == s) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- nhặt đồ

    private static boolean nhatDo(GameScr gs, MainChar mc, long l) {
        if (nhat <= 0) {
            return false;
        }
        if (monNhat != null && (!gs.actors.contains(monNhat) || ((Dropable) monNhat).isSendGet)) {
            monNhat = null;
        }
        if (monNhat == null) {
            monNhat = chonDo(gs, mc);
        }
        if (monNhat == null) {
            return false;
        }
        // đang đánh dở và đồ còn xa: đánh trước
        if (mucTieu != null && quaiSong(gs, mucTieu) && Util.distance(mc.x, mc.y, monNhat.x, monNhat.y) > 40) {
            return false;
        }
        if (Util.distance(mc.x, mc.y, monNhat.x, monNhat.y) <= 30) {
            nha();
            Dropable d = (Dropable) monNhat;
            GameService.gI().getDropableFromGround(d.catagory, d.ID);
            d.isSendGet = true;
            monNhat = null;
            trangThai = "nhặt đồ";
            return true;
        }
        diToi(mc, monNhat.x, monNhat.y, l);
        trangThai = "đi nhặt";
        return true;
    }

    private static Actor chonDo(GameScr gs, MainChar mc) {
        Actor best = null;
        int d = Integer.MAX_VALUE;
        for (int i = 0; i < gs.actors.size(); ++i) {
            Actor a = (Actor) gs.actors.elementAt(i);
            if (!(a instanceof Dropable) || !a.isItemCanGet() || ((Dropable) a).isSendGet) {
                continue;
            }
            int c = a.catagory;
            boolean muon = c == 4 ? nhat >= 1 : (c == 3 ? nhat >= 3 && !mc.isFullInventory() : nhat >= 2);
            if (!muon || !trongVung(a)) {
                continue;
            }
            int k = Util.distance(mc.x, mc.y, a.x, a.y);
            if (k < d) {
                d = k;
                best = a;
            }
        }
        return best;
    }

    // ---------------------------------------------------------------- đi bộ (giữ phím ảo, như người bấm)

    private static void diToi(MainChar mc, int x, int y, long l) {
        int dx = x - mc.x;
        int dy = y - mc.y;
        int ngang = dx < 0 ? 4 : 6;
        int doc = dy < 0 ? 2 : 8;
        int chinh = Math.abs(dx) >= Math.abs(dy) ? ngang : doc;
        int phu = chinh == ngang ? doc : ngang;
        if (tuongTruoc(mc, chinh) && !tuongTruoc(mc, phu)) {
            chinh = phu;
        }
        nha();
        Canvas.keyHold[chinh] = true;
        giuPhim = true;
        // kẹt 1,5 giây: bỏ mục tiêu (lần sau chọn con khác / đứng chờ)
        if (lucKet == 0L || mc.x != ketX || mc.y != ketY) {
            lucKet = l;
            ketX = mc.x;
            ketY = mc.y;
            lanKet = 0;
        } else if (l - lucKet > 1500L) {
            lucKet = 0L;
            nha();
            if (++lanKet >= 2) {
                mucTieu = null;
                monNhat = null;
            }
        }
    }

    private static boolean tuongTruoc(MainChar mc, int phim) {
        int tx = mc.x / 16 + (phim == 4 ? -1 : (phim == 6 ? 1 : 0));
        int ty = mc.y / 16 + (phim == 2 ? -1 : (phim == 8 ? 1 : 0));
        return laTuong(tx, ty);
    }

    /** Ô không đi qua được (luật va chạm của client) hoặc ô cổng chuyển map. */
    static boolean laTuong(int x, int y) {
        if (Tilemap.type == null || x < 0 || y < 0 || x >= Tilemap.w || y >= Tilemap.h) {
            return true;
        }
        int i = y * Tilemap.w + x;
        if (i >= Tilemap.type.length) {
            return true;
        }
        int t = Tilemap.type[i];
        return t >= 2000000000 || (t & 2) == 2;
    }

    private static void nha() {
        if (!giuPhim) {
            return;
        }
        giuPhim = false;
        Canvas.keyHold[2] = false;
        Canvas.keyHold[4] = false;
        Canvas.keyHold[6] = false;
        Canvas.keyHold[8] = false;
    }

    // ================================================================ thanh trạng thái

    /** Chữ thanh trạng thái (null = không vẽ). */
    static String chuThanh() {
        if (choBat) {
            return "Tự động: đang hỏi máy chủ...";
        }
        if (!dang) {
            return null;
        }
        return new StringBuffer("Tự động ").append(gio(conLai())).append(" - ").append(trangThai).toString();
    }

    static void ve(Graphics g, int y) {
        String s = chuThanh();
        if (s != null) {
            Nen.veThanhTuDong(g, s, y);
        }
    }

    // ================================================================ lưu cài đặt

    static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        try {
            String s = RMS.loadRMSString(RMS_TEN);
            if (s == null || s.length() < 9 || s.charAt(0) != '1') {
                return;
            }
            for (int i = 0; i < thuTu.length; ++i) {
                char c = s.charAt(1 + i);
                thuTu[i] = (byte) (c == '-' ? -1 : c - 'a');
            }
            nguongHp = (s.charAt(5) - 'a') * 10;
            nguongMp = (s.charAt(6) - 'a') * 10;
            nhat = s.charAt(7) - '0';
            veHoiPhuc = s.charAt(8) == '1';
            if (s.length() >= 10) {
                vungO = VUNG_O[Math.min(VUNG_O.length - 1, Math.max(0, s.charAt(9) - '0'))];
            }
            if (s.length() >= 11) {
                tuHoiSinh = s.charAt(10) == '1';
            }
        } catch (Throwable e) {
            // mặc định
        }
    }

    static void luu() {
        try {
            StringBuffer b = new StringBuffer("1");
            for (int i = 0; i < thuTu.length; ++i) {
                b.append(thuTu[i] < 0 ? '-' : (char) ('a' + thuTu[i]));
            }
            b.append((char) ('a' + nguongHp / 10)).append((char) ('a' + nguongMp / 10)).append((char) ('0' + nhat)).append(veHoiPhuc ? '1' : '0');
            int k = 0;
            for (int i = 0; i < VUNG_O.length; ++i) {
                if (VUNG_O[i] == vungO) {
                    k = i;
                }
            }
            b.append((char) ('0' + k)).append(tuHoiSinh ? '1' : '0');
            RMS.saveRMSString(RMS_TEN, b.toString());
        } catch (Throwable e) {
            // không lưu được: giữ trong phiên
        }
    }
}
